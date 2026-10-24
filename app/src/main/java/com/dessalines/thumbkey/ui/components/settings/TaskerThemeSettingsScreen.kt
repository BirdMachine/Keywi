package com.dessalines.thumbkey.ui.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dessalines.thumbkey.automation.TASKER_THEME_ACTION
import com.dessalines.thumbkey.automation.TaskerThemeAutomation
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskerThemeSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var refresh by remember { mutableIntStateOf(0) }
    val revision = TaskerThemeAutomation.revision + refresh
    val enabled = remember(revision) { TaskerThemeAutomation.enabled(context) }

    fun copy(value: String) {
        clipboard.setText(AnnotatedString(value))
    }

    Scaffold(topBar = { GemSettingsTopAppBar("Tasker theme updates", navController) }) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Let your routines dress Keywi", style = MaterialTheme.typography.titleLarge)
            Text(
                "Tasker can set keyboard artwork, toolbar colors, key surfaces and borders with a Send Intent action. Theme updates are off until you enable them.",
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Allow incoming theme commands", Modifier.weight(1f).padding(top = 12.dp))
                Switch(enabled, { value ->
                    scope.launch {
                        TaskerThemeAutomation.setEnabled(context, value)
                        refresh++
                    }
                })
            }
            Text(
                "Commands require your private pairing token. Turning this off also clears any temporary override.",
                style = MaterialTheme.typography.bodySmall,
            )
            GemOutlinedButton(onClick = {
                copy(TaskerThemeAutomation.token(context))
            }, modifier = Modifier.fillMaxWidth()) { Text("Copy pairing token") }
            GemOutlinedButton(onClick = {
                TaskerThemeAutomation.rotateToken(context)
                refresh++
            }, modifier = Modifier.fillMaxWidth()) { Text("Replace token · unpair old routines") }
            Text("Quick start · red toolbar on low battery", style = MaterialTheme.typography.titleMedium)
            Text("In Tasker, add a Battery Level profile and a System → Send Intent task. Use these fields:")
            Text(
                "Action: $TASKER_THEME_ACTION\nPackage: ${context.packageName}\nTarget: Broadcast Receiver\nExtra 1: token:<your pairing token>\nExtra 2: command:<copied JSON>",
                style = MaterialTheme.typography.bodySmall,
            )
            GemButton(onClick = {
                copy(TaskerThemeAutomation.taskerExample(context))
            }, modifier = Modifier.fillMaxWidth()) { Text("Copy complete red-toolbar setup") }
            GemOutlinedButton(onClick = {
                copy(TaskerThemeAutomation.exampleCommand())
            }, modifier = Modifier.fillMaxWidth()) { Text("Copy red-toolbar JSON") }
            GemOutlinedButton(onClick = {
                copy(TaskerThemeAutomation.clearCommand())
            }, modifier = Modifier.fillMaxWidth()) { Text("Copy clear-override JSON") }
            GemOutlinedButton(onClick = {
                copy(TaskerThemeAutomation.taskerExample(context, clear = true))
            }, modifier = Modifier.fillMaxWidth()) { Text("Copy complete clear setup") }
            Text(
                "Use the clear command in the profile's Exit Task to restore the theme from before the override.",
                style = MaterialTheme.typography.bodySmall,
            )
            Text("Temporary or Custom", style = MaterialTheme.typography.titleMedium)
            Text(
                "Mode temporary keeps one restore snapshot and layers later commands over it. Clear restores only automated surfaces that you have not edited afterward. Manual edits and other controls are preserved. Selecting another saved theme supersedes the override.",
            )
            Text(
                "Mode custom changes the current unsaved Custom theme permanently. It removes untouched temporary values first, preserves manual edits and other controls, and leaves saved themes intact.",
            )
            if (TaskerThemeAutomation.temporaryActive(context)) {
                Text("Temporary override active", color = MaterialTheme.colorScheme.primary)
                GemButton(onClick = {
                    scope.launch {
                        TaskerThemeAutomation.clear(context)
                        refresh++
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text("Restore theme now") }
            }
            Text(
                "Images use granted content:// URIs and are copied into Keywi; the original can then move. Include the URI in Intent Data or ClipData and grant read permission. File paths and web URLs are not accepted.",
                style = MaterialTheme.typography.bodySmall,
            )
            Text("Last result: ${TaskerThemeAutomation.lastResult(context)}", style = MaterialTheme.typography.bodySmall)
            GemOutlinedButton(onClick = { refresh++ }, modifier = Modifier.fillMaxWidth()) { Text("Refresh command status") }
        }
    }
}
