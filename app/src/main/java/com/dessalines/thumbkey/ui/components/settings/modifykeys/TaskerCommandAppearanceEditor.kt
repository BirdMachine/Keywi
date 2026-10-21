package com.dessalines.thumbkey.ui.components.settings.modifykeys

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.effects.TaskerCommandDisplay
import com.dessalines.thumbkey.effects.TaskerCommandDisplayKind
import com.dessalines.thumbkey.effects.TaskerCommandImages
import com.dessalines.thumbkey.effects.TaskerKeyBinding
import com.dessalines.thumbkey.effects.TaskerKeyBindings
import com.dessalines.thumbkey.ui.components.keyboard.TaskerCommandSymbol
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun TaskerCommandAppearanceEditor(
    keyId: String,
    direction: String,
    binding: TaskerKeyBinding,
    onChange: (TaskerKeyBinding) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var label by remember(keyId, direction, binding.display) {
        mutableStateOf(
            binding.display
                ?.takeIf { it.kind == TaskerCommandDisplayKind.TEXT }
                ?.value
                .orEmpty(),
        )
    }
    var feedback by remember(keyId, direction) { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var pendingKey by remember { mutableStateOf(keyId) }
    var pendingDirection by remember { mutableStateOf(direction) }

    fun update(display: TaskerCommandDisplay?) {
        val latest = TaskerKeyBindings.get(context, keyId, direction) ?: return
        val next = latest.copy(display = display)
        TaskerKeyBindings.save(context, keyId, direction, next)
        onChange(next)
        feedback = null
    }

    val imagePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                val targetKey = pendingKey
                val targetDirection = pendingDirection
                scope.launch {
                    loading = true
                    try {
                        val display = TaskerCommandImages.importImage(context.applicationContext, uri)
                        val latest = TaskerKeyBindings.get(context, targetKey, targetDirection)
                        if (latest != null) {
                            val next = latest.copy(display = display)
                            TaskerKeyBindings.save(context, targetKey, targetDirection, next)
                            if (targetKey == keyId && targetDirection == direction) onChange(next)
                            feedback = "Image copied into Keywi."
                        } else {
                            TaskerCommandImages.delete(context, display.value)
                            feedback = "The task assignment was removed; the image was discarded."
                        }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (failure: Exception) {
                        feedback = failure.message ?: "Unable to import this image."
                    } finally {
                        loading = false
                    }
                }
            }
        }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Command symbol", style = MaterialTheme.typography.titleSmall)
        Text(
            "Only the selected cell's $direction label changes. The task and original key action stay the same.",
            style = MaterialTheme.typography.bodySmall,
        )
        Box(Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(8.dp), contentAlignment = Alignment.Center) {
            binding.display?.let { TaskerCommandSymbol(it, 32.dp, fallback = { Text("Original key label") }) }
                ?: Text("Original key label")
        }
        OutlinedTextField(
            label,
            { label = it },
            label = { Text("Text / Unicode symbol") },
            supportingText = { Text("Up to 16 characters; emoji and Unicode are supported.") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = {
            val display = TaskerCommandDisplay(TaskerCommandDisplayKind.TEXT, label)
            if (display.valid()) update(display) else feedback = "Use 1–16 characters without line breaks."
        }) { Text("Apply label") }
        TaskerCommandImages.icons.entries.chunked(3).forEach { icons ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                icons.forEach { (id, title) ->
                    FilterChip(selected = binding.display == TaskerCommandDisplay(TaskerCommandDisplayKind.ICON, id), onClick = {
                        update(TaskerCommandDisplay(TaskerCommandDisplayKind.ICON, id))
                    }, label = { Text(title) })
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(enabled = !loading, onClick = {
                pendingKey = keyId
                pendingDirection = direction
                imagePicker.launch("image/*")
            }) { Text(if (loading) "Copying image…" else "Import image") }
            TextButton(enabled = binding.display != null, onClick = { update(null) }) { Text("Use original label") }
        }
        Text(
            "Images are copied and reduced to 256 px; choose a bitmap up to 4 MB / 16 megapixels. Hold symbols appear as a small badge.",
            style = MaterialTheme.typography.bodySmall,
        )
        feedback?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
    }
}
