package com.dessalines.thumbkey.ui.components.settings.modifykeys

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.effects.TaskerBindingMode
import com.dessalines.thumbkey.effects.TaskerIntegration
import com.dessalines.thumbkey.effects.TaskerKeyBinding
import com.dessalines.thumbkey.effects.TaskerKeyBindings
import com.dessalines.thumbkey.effects.TaskerTask

/** A task is selected explicitly; changing an assignment never executes it. */
@Composable
fun TaskerKeyAssignments(
    keyId: String,
    direction: String,
) {
    val context = LocalContext.current
    var binding by remember(keyId, direction) { mutableStateOf(TaskerKeyBindings.get(context, keyId, direction)) }
    var mode by remember(keyId, direction) { mutableStateOf(binding?.mode ?: TaskerBindingMode.REPLACE) }
    var status by remember(keyId, direction) {
        mutableStateOf(
            TaskerIntegration.status(
                context,
                binding?.task?.packageName ?: TaskerIntegration.installedPackage(context),
            ),
        )
    }
    var feedback by remember { mutableStateOf<String?>(null) }
    var manual by remember { mutableStateOf(false) }
    var taskName by remember { mutableStateOf("") }
    var pendingKey by rememberSaveable { mutableStateOf("") }
    var pendingDirection by rememberSaveable { mutableStateOf("CENTER") }
    var pendingPackage by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingMode by rememberSaveable { mutableStateOf(TaskerBindingMode.REPLACE.name) }

    fun refresh() {
        binding = TaskerKeyBindings.get(context, keyId, direction)
        status = TaskerIntegration.status(context, binding?.task?.packageName ?: TaskerIntegration.installedPackage(context))
    }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { refresh() }
    val preferences = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { refresh() }
    val picker =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val pkg = pendingPackage
                val selected = result.data?.dataString
                if (pkg != null && selected != null) {
                    val task = TaskerTask(selected, pkg)
                    if (TaskerIntegration.valid(task)) {
                        TaskerKeyBindings.save(
                            context,
                            pendingKey,
                            pendingDirection,
                            TaskerKeyBinding(
                                task,
                                TaskerBindingMode.valueOf(pendingMode),
                                TaskerKeyBindings.get(context, pendingKey, pendingDirection)?.display,
                            ),
                        )
                        refresh()
                        feedback = "Task assigned. It will run only when you use that key gesture."
                    } else {
                        feedback = "Tasker returned an invalid task name. Choose the task again."
                    }
                } else {
                    feedback = "No task was returned. You can enter an existing task name instead."
                }
            }
        }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("TASKER // $direction", style = MaterialTheme.typography.titleSmall)
        Text(binding?.let { "Assigned: ${it.task.name}" } ?: "No Tasker task assigned", style = MaterialTheme.typography.bodyMedium)
        Text(
            "This binds the selected cell and gesture across board layers. No typed text is passed to Tasker.",
            style = MaterialTheme.typography.bodySmall,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TaskerBindingMode.entries.forEach { next ->
                FilterChip(selected = mode == next, onClick = {
                    mode = next
                    binding?.let { existing ->
                        val updated = existing.copy(mode = next)
                        TaskerKeyBindings.save(context, keyId, direction, updated)
                        binding = updated
                    }
                }, label = { Text(if (next == TaskerBindingMode.REPLACE) "Replace key" else "Also run task") })
            }
        }
        Text(
            if (mode ==
                TaskerBindingMode.REPLACE
            ) {
                "Runs the task instead of the key action. A failed task will not type the original character. On cursor/delete sliding keys, replacement swipes resolve on release instead of sliding continuously."
            } else {
                "Runs the task and keeps the original key action."
            },
            style = MaterialTheme.typography.bodySmall,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = {
                val pkg = TaskerIntegration.installedPackage(context)
                if (pkg == null) {
                    status = TaskerIntegration.Status.MISSING
                } else {
                    pendingKey = keyId
                    pendingDirection = direction
                    pendingPackage = pkg
                    pendingMode = mode.name
                    runCatching { picker.launch(TaskerIntegration.selectionIntent(pkg)) }
                        .onFailure { feedback = "Tasker's task picker is unavailable. Enter an existing task name below." }
                }
            }) { Text("Choose Tasker task") }
            TextButton(onClick = {
                taskName = binding?.task?.name.orEmpty()
                manual = true
            }) { Text("Enter name") }
        }
        binding?.let { current -> TaskerCommandAppearanceEditor(keyId, direction, current) { binding = it } }
        if (binding != null) {
            TextButton(onClick = {
                TaskerKeyBindings.save(context, keyId, direction, null)
                binding = null
                feedback = "Assignment removed."
            }) { Text("Remove Tasker assignment") }
        }
        Text(status.message, style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { refresh() }) { Text("Check connection") }
            if (status == TaskerIntegration.Status.PERMISSION) {
                TextButton(onClick = {
                    runCatching { permission.launch(TaskerIntegration.PERMISSION) }
                        .onFailure { feedback = TaskerIntegration.Status.PERMISSION.message }
                }) { Text("Grant access") }
            }
            TextButton(onClick = {
                val pkg = binding?.task?.packageName ?: TaskerIntegration.installedPackage(context)
                if (pkg != null) {
                    runCatching { preferences.launch(TaskerIntegration.externalAccessIntent(pkg)) }
                        .onFailure { feedback = "Open Tasker → Preferences → Misc → Allow External Access." }
                } else {
                    status = TaskerIntegration.Status.MISSING
                }
            }) { Text("Tasker preferences") }
        }
        feedback?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
    }
    if (manual) {
        AlertDialog(
            onDismissRequest = { manual = false },
            title = { Text("Assign an existing Tasker task") },
            text = {
                Column {
                    Text("Enter the exact task name from Tasker. This saves the name; it does not run or verify the task.")
                    OutlinedTextField(taskName, { taskName = it }, label = { Text("Task name") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val pkg = TaskerIntegration.installedPackage(context)
                    val task = pkg?.let { TaskerTask(taskName, it) }
                    if (task != null && TaskerIntegration.valid(task)) {
                        val updated = TaskerKeyBinding(task, mode, binding?.display)
                        TaskerKeyBindings.save(context, keyId, direction, updated)
                        binding = updated
                        manual = false
                        feedback = "Task name assigned. If you rename it in Tasker, update this assignment."
                    } else {
                        feedback = if (pkg == null) TaskerIntegration.Status.MISSING.message else TaskerIntegration.Status.INVALID.message
                    }
                }) { Text("Assign") }
            },
            dismissButton = { TextButton(onClick = { manual = false }) { Text("Cancel") } },
        )
    }
}
