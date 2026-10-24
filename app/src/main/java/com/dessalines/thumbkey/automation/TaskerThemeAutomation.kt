package com.dessalines.thumbkey.automation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.dessalines.thumbkey.ui.components.keyboard.BackdropMode
import com.dessalines.thumbkey.ui.components.keyboard.BackdropPreset
import com.dessalines.thumbkey.ui.components.keyboard.BackdropThemeState
import com.dessalines.thumbkey.ui.components.keyboard.CUSTOM_THEME_ID
import com.dessalines.thumbkey.ui.components.keyboard.KeyBorderStyle
import com.dessalines.thumbkey.ui.components.keyboard.KeySurfaceStyle
import com.dessalines.thumbkey.ui.components.keyboard.KeyboardBackdrop
import com.dessalines.thumbkey.ui.components.keyboard.KeyboardGradientStop
import com.dessalines.thumbkey.ui.components.keyboard.ThemeAssetStore
import com.dessalines.thumbkey.ui.components.keyboard.ThemeDocument
import com.dessalines.thumbkey.ui.components.keyboard.ThemeEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom

const val TASKER_THEME_ACTION = "com.birdmachine.keywi.action.APPLY_THEME_COMMAND"

enum class ThemeCommandMode { TEMPORARY, CUSTOM }

enum class ThemeCommandTarget { BACKGROUND, TOOLBAR, KEY_BORDER, KEY_SURFACE, TOOLBAR_BORDER }

sealed interface ThemeCommandValue {
    data class Solid(
        val color: Color,
    ) : ThemeCommandValue

    data class Gradient(
        val backdrop: KeyboardBackdrop,
    ) : ThemeCommandValue

    data class Image(
        val uri: Uri,
    ) : ThemeCommandValue
}

data class ThemeCommandUpdate(
    val target: ThemeCommandTarget,
    val value: ThemeCommandValue,
    val opacity: Float = 1f,
)

data class ThemeCommand(
    val clear: Boolean,
    val mode: ThemeCommandMode,
    val updates: List<ThemeCommandUpdate>,
)

/** Versioned public contract; no preference names, filesystem paths, or executable values. */
object ThemeCommandParser {
    fun parse(json: String): ThemeCommand {
        require(json.toByteArray(Charsets.UTF_8).size <= 32 * 1024) { "Command exceeds 32 KB" }
        val root = JSONObject(json)
        fields(root, setOf("version", "operation", "mode", "updates"))
        require(number(root, "version") == 1.0) { "Unsupported version" }
        val operation = string(root, "operation")
        if (operation == "clear") {
            require(!root.has("mode") && !root.has("updates")) { "Clear takes no updates" }
            return ThemeCommand(true, ThemeCommandMode.TEMPORARY, emptyList())
        }
        require(operation == "apply") { "Unknown operation" }
        val mode =
            when (string(root, "mode")) {
                "temporary" -> ThemeCommandMode.TEMPORARY
                "custom" -> ThemeCommandMode.CUSTOM
                else -> error("Unknown mode")
            }
        val array = root.getJSONArray("updates")
        require(array.length() in 1..5) { "Choose one to five targets" }
        val updates =
            (0 until array.length()).map { index ->
                val item = array.getJSONObject(index)
                val target =
                    when (string(item, "target")) {
                        "background" -> ThemeCommandTarget.BACKGROUND
                        "toolbar" -> ThemeCommandTarget.TOOLBAR
                        "key_border" -> ThemeCommandTarget.KEY_BORDER
                        "key_surface" -> ThemeCommandTarget.KEY_SURFACE
                        "toolbar_border" -> ThemeCommandTarget.TOOLBAR_BORDER
                        else -> error("Unknown target")
                    }
                val opacity = if (item.has("opacity")) number(item, "opacity").toFloat() else 1f
                require(opacity in 0f..1f) { "Invalid opacity" }
                require(!item.has("opacity") || target == ThemeCommandTarget.BACKGROUND || target == ThemeCommandTarget.TOOLBAR) {
                    "Opacity applies to backgrounds"
                }
                val value =
                    when (string(item, "style")) {
                        "color" -> {
                            fields(item, setOf("target", "style", "color", "opacity"))
                            ThemeCommandValue.Solid(color(string(item, "color")))
                        }

                        "gradient" -> {
                            fields(item, setOf("target", "style", "angle", "stops", "opacity"))
                            require(target != ThemeCommandTarget.TOOLBAR_BORDER) { "Toolbar outline supports one color" }
                            val angle = number(item, "angle")
                            require(angle in -360.0..360.0) { "Angle must be -360 to 360" }
                            val stops = item.getJSONArray("stops")
                            require(stops.length() in 2..64) { "Use two to 64 stops" }
                            val positions =
                                (0 until stops.length()).map { i ->
                                    val stop = stops.getJSONObject(i)
                                    fields(stop, setOf("position", "color"))
                                    KeyboardGradientStop(number(stop, "position").toFloat(), color(string(stop, "color")))
                                }
                            require(
                                positions.first().position == 0f && positions.last().position == 1f &&
                                    positions.all { it.position in 0f..1f } &&
                                    positions.zipWithNext().all { (a, b) -> a.position < b.position },
                            ) { "Stops must increase from 0 to 1" }
                            ThemeCommandValue.Gradient(KeyboardBackdrop(positions, angle.toFloat()))
                        }

                        "image" -> {
                            fields(item, setOf("target", "style", "uri", "opacity"))
                            require(
                                target == ThemeCommandTarget.BACKGROUND || target == ThemeCommandTarget.TOOLBAR,
                            ) { "Images require a background target" }
                            val uri = Uri.parse(string(item, "uri"))
                            require(
                                uri.scheme == "content" && !uri.authority.isNullOrBlank() && uri.fragment == null,
                            ) { "Use a granted content URI" }
                            ThemeCommandValue.Image(uri)
                        }

                        else -> {
                            error("Unknown style")
                        }
                    }
                ThemeCommandUpdate(target, value, opacity)
            }
        require(updates.map { it.target }.distinct().size == updates.size) { "Duplicate target" }
        return ThemeCommand(false, mode, updates)
    }

    private fun fields(
        value: JSONObject,
        allowed: Set<String>,
    ) {
        require(value.keys().asSequence().all { it in allowed }) { "Unknown command field" }
    }

    private fun string(
        value: JSONObject,
        key: String,
    ): String = (value.get(key) as? String)?.takeIf { it.isNotEmpty() } ?: error("Expected text")

    private fun number(
        value: JSONObject,
        key: String,
    ): Double = (value.get(key) as? Number)?.toDouble()?.takeIf { it.isFinite() } ?: error("Expected finite number")

    private fun color(value: String): Color {
        require(value.matches(Regex("#(?:[0-9a-fA-F]{6}|[0-9a-fA-F]{8})"))) { "Use #RRGGBB or #AARRGGBB" }
        return Color(android.graphics.Color.parseColor(value))
    }
}

data class ThemeCommandResult(
    val accepted: Boolean,
    val message: String,
)

/** All entrypoints run serially; the receiver launches on Main and only asset IO leaves it. */
object TaskerThemeAutomation {
    private const val PREFS = "keywi_tasker_themes"
    private const val BASE = "temporary_base"
    private const val LAST = "temporary_last"
    private const val TARGETS = "temporary_targets"
    private val lock = Mutex()
    var revision by mutableIntStateOf(0)
        private set

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun enabled(context: Context): Boolean = prefs(context).getBoolean("enabled", false)

    fun temporaryActive(context: Context): Boolean = prefs(context).contains(BASE)

    fun lastResult(context: Context): String = prefs(context).getString("last_result", "No commands received").orEmpty()

    /** Kept out of Android backups; no account credentials are used by this feature. */
    fun token(context: Context): String {
        val file = File(context.noBackupFilesDir, "tasker-theme-token")
        val existing = runCatching { file.readText() }.getOrNull()
        if (existing != null && existing.matches(Regex("[0-9a-f]{64}"))) return existing
        return rotateToken(context)
    }

    fun rotateToken(context: Context): String {
        val bytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val value = bytes.joinToString("") { "%02x".format(it) }
        File(context.noBackupFilesDir, "tasker-theme-token").apply {
            parentFile?.mkdirs()
            writeText(value)
        }
        return value
    }

    suspend fun setEnabled(
        context: Context,
        value: Boolean,
    ) = lock.withLock {
        if (!value) restoreBase(context)
        if (value) token(context)
        prefs(context).edit().putBoolean("enabled", value).apply()
        revision++
    }

    suspend fun handle(
        context: Context,
        intent: Intent,
    ): ThemeCommandResult =
        lock.withLock {
            val result =
                when {
                    intent.action != TASKER_THEME_ACTION -> {
                        ThemeCommandResult(false, "Rejected: unknown action")
                    }

                    !enabled(context) -> {
                        ThemeCommandResult(false, "Rejected: theme updates are disabled")
                    }

                    !authorized(context, intent) -> {
                        ThemeCommandResult(false, "Rejected: invalid token")
                    }

                    else -> {
                        try {
                            val json = intent.getStringExtra("command") ?: error("Missing command")
                            execute(context, ThemeCommandParser.parse(json))
                            ThemeCommandResult(true, "Theme command applied")
                        } catch (cancelled: kotlinx.coroutines.CancellationException) {
                            throw cancelled
                        } catch (_: Exception) {
                            ThemeCommandResult(false, "Rejected: invalid command or unreadable image")
                        }
                    }
                }
            prefs(context).edit().putString("last_result", result.message).apply()
            result
        }

    private fun authorized(
        context: Context,
        intent: Intent,
    ): Boolean {
        val supplied = runCatching { intent.getStringExtra("token") }.getOrNull() ?: return false
        if (supplied.length != 64) return false
        return MessageDigest.isEqual(token(context).toByteArray(Charsets.UTF_8), supplied.toByteArray(Charsets.UTF_8))
    }

    suspend fun clear(context: Context) = lock.withLock { restoreBase(context) }

    private fun trackedTargets(context: Context): Set<ThemeCommandTarget> =
        prefs(context)
            .getStringSet(TARGETS, emptySet())
            .orEmpty()
            .map { ThemeCommandTarget.valueOf(it) }
            .toSet()

    /** Copy only the addressed surface, leaving unrelated theme controls and key overrides alone. */
    private fun copyTarget(
        destination: ThemeDocument,
        source: ThemeDocument,
        target: ThemeCommandTarget,
    ): ThemeDocument =
        when (target) {
            ThemeCommandTarget.BACKGROUND -> {
                destination.copy(backdrop = source.backdrop)
            }

            ThemeCommandTarget.TOOLBAR -> {
                destination.copy(toolbar = source.toolbar)
            }

            ThemeCommandTarget.TOOLBAR_BORDER -> {
                destination.copy(toolbarBorderColor = source.toolbarBorderColor)
            }

            ThemeCommandTarget.KEY_BORDER -> {
                destination.copy(
                    keys =
                        destination.keys.copy(
                            borderStyle = source.keys.borderStyle,
                            borderColor = source.keys.borderColor,
                            borderGradient = source.keys.borderGradient,
                        ),
                )
            }

            ThemeCommandTarget.KEY_SURFACE -> {
                destination.copy(
                    keys =
                        destination.keys.copy(
                            surfaceStyle = source.keys.surfaceStyle,
                            surfaceColor = source.keys.surfaceColor,
                            surfaceGradient = source.keys.surfaceGradient,
                        ),
                )
            }
        }

    private fun restoredCandidate(context: Context): ThemeDocument? {
        val raw = prefs(context).getString(BASE, null) ?: return null
        // Selecting another saved theme supersedes the override instead of being overwritten.
        if (ThemeEngine.activeThemeId(context) != CUSTOM_THEME_ID) return null
        val base = ThemeEngine.decode(raw).getOrThrow()
        val last = ThemeEngine.decode(requireNotNull(prefs(context).getString(LAST, null))).getOrThrow()
        val current = ThemeEngine.captureCurrent(context, "Custom", CUSTOM_THEME_ID)
        var restored = current
        trackedTargets(context).forEach { target ->
            if (copyTarget(current, last, target) == current) restored = copyTarget(restored, base, target)
        }
        return if (restored.copy(id = base.id, name = base.name) == base) base else restored
    }

    private fun forgetOverride(context: Context) {
        prefs(context)
            .edit()
            .remove(BASE)
            .remove(LAST)
            .remove(TARGETS)
            .apply()
    }

    private fun restoreBase(context: Context) {
        if (!temporaryActive(context)) return
        restoredCandidate(context)?.let { ThemeEngine.apply(context, it) }
        forgetOverride(context)
        revision++
    }

    private suspend fun execute(
        context: Context,
        command: ThemeCommand,
    ) {
        if (command.clear) {
            restoreBase(context)
            return
        }
        ThemeEngine.ensureMigrated(context)
        // Decode/copy all media before changing any live preferences.
        val images =
            withContext(Dispatchers.IO) {
                command.updates
                    .mapNotNull { update ->
                        (update.value as? ThemeCommandValue.Image)?.let { update.target to importImage(context, it.uri) }
                    }.toMap()
            }
        val originalId = ThemeEngine.activeThemeId(context)
        val original = ThemeEngine.captureCurrent(context, "Custom", originalId)
        val storedBase = prefs(context).getString(BASE, null)
        val base =
            if (command.mode == ThemeCommandMode.CUSTOM && storedBase != null) {
                restoredCandidate(context) ?: original
            } else {
                original
            }
        var next = base.copy(id = CUSTOM_THEME_ID, name = "Custom")
        command.updates.forEach { update ->
            fun backdrop(previous: BackdropThemeState): BackdropThemeState =
                when (val value = update.value) {
                    is ThemeCommandValue.Solid -> {
                        previous.copy(
                            preset = BackdropPreset.CUSTOM,
                            mode = BackdropMode.COLORFUL,
                            opacity = update.opacity,
                            mediaUri = null,
                            stops = listOf(KeyboardGradientStop(0f, value.color), KeyboardGradientStop(1f, value.color)),
                        )
                    }

                    is ThemeCommandValue.Gradient -> {
                        previous.copy(
                            preset = BackdropPreset.CUSTOM,
                            mode = BackdropMode.COLORFUL,
                            opacity = update.opacity,
                            mediaUri = null,
                            angleDegrees = value.backdrop.angleDegrees,
                            stops = value.backdrop.stops,
                        )
                    }

                    is ThemeCommandValue.Image -> {
                        previous.copy(mode = BackdropMode.IMAGE, opacity = update.opacity, mediaUri = images.getValue(update.target))
                    }
                }
            next =
                when (update.target) {
                    ThemeCommandTarget.BACKGROUND -> {
                        next.copy(backdrop = backdrop(next.backdrop))
                    }

                    ThemeCommandTarget.TOOLBAR -> {
                        next.copy(toolbar = backdrop(next.toolbar))
                    }

                    ThemeCommandTarget.TOOLBAR_BORDER -> {
                        next.copy(toolbarBorderColor = (update.value as ThemeCommandValue.Solid).color)
                    }

                    ThemeCommandTarget.KEY_BORDER -> {
                        when (val value = update.value) {
                            is ThemeCommandValue.Solid -> {
                                next.copy(
                                    keys = next.keys.copy(borderStyle = KeyBorderStyle.SOLID, borderColor = value.color),
                                )
                            }

                            is ThemeCommandValue.Gradient -> {
                                next.copy(
                                    keys = next.keys.copy(borderStyle = KeyBorderStyle.GRADIENT, borderGradient = value.backdrop),
                                )
                            }

                            else -> {
                                error("Unsupported key border")
                            }
                        }
                    }

                    ThemeCommandTarget.KEY_SURFACE -> {
                        when (val value = update.value) {
                            is ThemeCommandValue.Solid -> {
                                next.copy(
                                    keys = next.keys.copy(surfaceStyle = KeySurfaceStyle.SOLID, surfaceColor = value.color),
                                )
                            }

                            is ThemeCommandValue.Gradient -> {
                                next.copy(
                                    keys = next.keys.copy(surfaceStyle = KeySurfaceStyle.GRADIENT, surfaceGradient = value.backdrop),
                                )
                            }

                            else -> {
                                error("Unsupported key surface")
                            }
                        }
                    }
                }
        }
        if (command.mode == ThemeCommandMode.TEMPORARY) {
            val continuing = storedBase != null && originalId == CUSTOM_THEME_ID
            val previousTargets = if (continuing) trackedTargets(context) else emptySet()
            var restore = if (continuing) ThemeEngine.decode(requireNotNull(storedBase)).getOrThrow() else original
            var last = if (continuing) ThemeEngine.decode(requireNotNull(prefs(context).getString(LAST, null))).getOrThrow() else original
            command.updates.forEach { update ->
                if (update.target !in previousTargets) restore = copyTarget(restore, original, update.target)
                last = copyTarget(last, next, update.target)
            }
            check(
                prefs(context)
                    .edit()
                    .putString(BASE, ThemeEngine.export(restore))
                    .putString(LAST, ThemeEngine.export(last))
                    .putStringSet(TARGETS, (previousTargets + command.updates.map { it.target }).map { it.name }.toSet())
                    .commit(),
            )
            ThemeEngine.apply(context, next)
        } else {
            // Save to the same Custom manifest, copying assets through the normal theme engine.
            ThemeEngine.saveCustom(context, next)
            ThemeEngine.apply(context, ThemeEngine.loadCustom(context))
            forgetOverride(context)
        }
        revision++
    }

    private fun importImage(
        context: Context,
        uri: Uri,
    ): String {
        val mime = context.contentResolver.getType(uri)
        require(mime == null || mime in setOf("image/png", "image/jpeg", "image/webp", "image/gif")) { "Unsupported image" }
        val temporary = File.createTempFile("tasker-image-", ".img", context.cacheDir)
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                temporary.outputStream().use { output ->
                    val bytes = ByteArray(8192)
                    var total = 0
                    while (true) {
                        val count = input.read(bytes)
                        if (count < 0) break
                        total += count
                        require(total <= 8 * 1024 * 1024) { "Image exceeds 8 MB" }
                        output.write(bytes, 0, count)
                    }
                    require(total > 0) { "Empty image" }
                }
            } ?: error("Image unavailable")
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(temporary.path, bounds)
            require(
                bounds.outWidth in 1..8192 && bounds.outHeight in 1..8192 &&
                    bounds.outWidth.toLong() * bounds.outHeight <= 32_000_000L,
            ) { "Invalid image dimensions" }
            return ThemeAssetStore.copySelected(context, Uri.fromFile(temporary), "img")
        } finally {
            temporary.delete()
        }
    }

    fun exampleCommand(): String =
        """{"version":1,"operation":"apply","mode":"temporary","updates":[{"target":"toolbar","style":"color","color":"#FFFF3030"}]}"""

    fun clearCommand(): String = """{"version":1,"operation":"clear"}"""

    fun taskerExample(
        context: Context,
        clear: Boolean = false,
    ): String =
        "Action: $TASKER_THEME_ACTION\nPackage: ${context.packageName}\nTarget: Broadcast Receiver\nExtra: token:${token(
            context,
        )}\nExtra: command:${if (clear) clearCommand() else exampleCommand()}"
}

class TaskerThemeCommandReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val pending = goAsync()
        scope.launch {
            try {
                TaskerThemeAutomation.handle(context.applicationContext, intent)
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }
}
