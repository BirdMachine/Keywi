package com.dessalines.thumbkey.effects

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import org.json.JSONObject

enum class TaskerBindingMode { REPLACE, SECONDARY }

data class TaskerKeyBinding(
    val task: TaskerTask,
    val mode: TaskerBindingMode = TaskerBindingMode.REPLACE,
    val display: TaskerCommandDisplay? = null,
)

/** Position and gesture bindings share the sound/GIF matrix IDs across board layers. */
object TaskerKeyBindings {
    var revision by mutableIntStateOf(0)
        private set
    private val keyPattern = Regex("(key|hk)[0-9]+_[0-9]+")
    private val directions =
        setOf("CENTER", "TOP_LEFT", "TOP", "TOP_RIGHT", "LEFT", "RIGHT", "BOTTOM_LEFT", "BOTTOM", "BOTTOM_RIGHT", "HOLD")

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences("keywi_tasker_keys", Context.MODE_PRIVATE)

    private fun slot(
        keyId: String,
        direction: String,
    ): String {
        require(keyPattern.matches(keyId) && direction in directions)
        return "$keyId:$direction"
    }

    fun get(
        context: Context,
        keyId: String,
        direction: String,
    ): TaskerKeyBinding? =
        runCatching {
            val json = prefs(context).getString(slot(keyId, direction), null) ?: return null
            val value = JSONObject(json)
            TaskerKeyBinding(
                TaskerTask(value.getString("name"), value.getString("package")),
                TaskerBindingMode.valueOf(value.getString("mode")),
                TaskerCommandDisplay.fromJson(value.optJSONObject("display")),
            ).takeIf { TaskerIntegration.valid(it.task) }
        }.getOrNull()

    /** Reserve sliding cells until resolution so replacements cannot move/delete text first. */
    fun reservesSwipe(
        context: Context,
        keyId: String,
    ): Boolean =
        directions
            .asSequence()
            .filter { it != "CENTER" && it != "HOLD" }
            .any { get(context, keyId, it)?.mode == TaskerBindingMode.REPLACE }

    fun save(
        context: Context,
        keyId: String,
        direction: String,
        binding: TaskerKeyBinding?,
    ) {
        val previous = get(context, keyId, direction)
        val editor = prefs(context).edit()
        val key = slot(keyId, direction)
        if (binding == null) {
            editor.remove(key)
        } else {
            require(TaskerIntegration.valid(binding.task))
            require(binding.display?.valid() != false)
            editor.putString(
                key,
                JSONObject()
                    .put("name", binding.task.name)
                    .put("package", binding.task.packageName)
                    .put("mode", binding.mode.name)
                    .put("display", binding.display?.toJson())
                    .toString(),
            )
        }
        editor.apply()
        revision++
        val old = previous?.display
        if (old?.kind == TaskerCommandDisplayKind.IMAGE && old != binding?.display) {
            val stillUsed =
                prefs(context).all.values.filterIsInstance<String>().any { raw ->
                    runCatching { TaskerCommandDisplay.fromJson(JSONObject(raw).optJSONObject("display")) == old }.getOrDefault(false)
                }
            if (!stillUsed) TaskerCommandImages.delete(context, old.value)
        }
    }

    /**
     * Call once when a tap/swipe/hold resolves, before the original key action.
     * A replacement consumes that action even if Tasker cannot run: never type an unexpected character.
     * A secondary task returns false so the original action proceeds exactly once.
     */
    fun dispatch(
        context: Context,
        keyId: String,
        direction: String,
    ): Boolean =
        dispatchBinding(context, keyId, direction) { binding ->
            val result = TaskerIntegration.execute(context, binding.task)
            if (result != TaskerIntegration.Status.READY) Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
        }

    internal fun dispatchBinding(
        context: Context,
        keyId: String,
        direction: String,
        run: (TaskerKeyBinding) -> Unit,
    ): Boolean {
        val binding = get(context, keyId, direction) ?: return false
        run(binding)
        return binding.mode == TaskerBindingMode.REPLACE
    }
}
