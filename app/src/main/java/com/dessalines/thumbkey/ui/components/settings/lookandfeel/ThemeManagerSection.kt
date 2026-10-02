package com.dessalines.thumbkey.ui.components.settings.lookandfeel

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.ui.components.keyboard.CUSTOM_THEME_ID
import com.dessalines.thumbkey.ui.components.keyboard.ThemeDocument
import com.dessalines.thumbkey.ui.components.keyboard.ThemeEngine

/** Compact front door for the theme engine. Detailed controls remain below for now. */
@Composable
fun ThemeManagerSection(onThemeApplied: () -> Unit) {
    val context = LocalContext.current
    ThemeEngine.ensureMigrated(context)

    var refresh by remember { mutableStateOf(0) }
    var expanded by remember { mutableStateOf(false) }
    var naming by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    fun active(): ThemeDocument = ThemeEngine.loadActive(context)
    fun themes(): List<ThemeDocument> = ThemeEngine.listThemes(context)
    fun changed(text: String? = null) {
        refresh++
        message = text
        onThemeApplied()
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: error("Could not read theme")
            }.fold(
                onSuccess = { json ->
                    ThemeEngine.importIntoCustom(context, json).fold(
                        onSuccess = { changed("Imported into Custom") },
                        onFailure = { message = "Import failed: ${it.message ?: "invalid theme"}" },
                    )
                },
                onFailure = { message = "Import failed: ${it.message ?: "could not read file"}" },
            )
        }
    }

    var pendingExport by remember { mutableStateOf<ThemeDocument?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val theme = pendingExport
        if (uri != null && theme != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use {
                    it.write(ThemeEngine.export(theme))
                } ?: error("Could not open destination")
            }.fold(
                onSuccess = { message = "Theme exported" },
                onFailure = { message = "Export failed: ${it.message ?: "could not write file"}" },
            )
        }
        pendingExport = null
    }

    // refresh is intentionally read here so mutations rebuild the menu/header.
    @Suppress("UNUSED_VARIABLE") val refreshToken = refresh
    val current = active()

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Theme manager", style = MaterialTheme.typography.titleMedium)
        Text(
            if (current.id == CUSTOM_THEME_ID) "Custom is your scratch theme — edits are safe here."
            else "Saved themes stay intact until you choose Edit.",
            style = MaterialTheme.typography.bodySmall,
        )

        Column {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Theme: ${current.name}  ▾")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("Custom") },
                    onClick = {
                        expanded = false
                        ThemeEngine.apply(context, ThemeEngine.loadCustom(context))
                        changed()
                    },
                )
                themes().forEach { theme ->
                    DropdownMenuItem(
                        text = { Text(theme.name) },
                        onClick = {
                            expanded = false
                            ThemeEngine.apply(context, theme)
                            changed()
                        },
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            if (current.id != CUSTOM_THEME_ID) {
                Button(
                    onClick = {
                        ThemeEngine.beginEditing(context, current)
                        changed("Copied ${current.name} into Custom")
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("Edit") }
            } else {
                Button(onClick = { naming = true }, modifier = Modifier.weight(1f)) { Text("Save as…") }
            }
            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/json", "text/json", "text/plain")) },
                modifier = Modifier.weight(1f),
            ) { Text("Import") }
            OutlinedButton(
                onClick = {
                    val exportTheme = ThemeEngine.snapshotForExport(context)
                    pendingExport = exportTheme
                    exportLauncher.launch("${ThemeEngine.safeFileName(exportTheme.name)}.keywi-theme.json")
                },
                modifier = Modifier.weight(1f),
            ) { Text("Export") }
        }

        if (current.id == CUSTOM_THEME_ID) {
            TextButton(onClick = { ThemeEngine.resetCustom(context); changed("Custom reset to defaults") }) {
                Text("Reset Custom")
            }
        }
        message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
    }

    if (naming) {
        AlertDialog(
            onDismissRequest = { naming = false },
            title = { Text("Save Custom as theme") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text("Theme name") },
                )
            },
            confirmButton = {
                TextButton(
                    enabled = name.isNotBlank(),
                    onClick = {
                        val saved = ThemeEngine.saveCustomAs(context, name)
                        name = ""
                        naming = false
                        changed("Saved ${saved.name}")
                    },
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { naming = false }) { Text("Cancel") } },
        )
    }
}
