package com.dessalines.thumbkey.ui.components.settings.lookandfeel

import com.dessalines.thumbkey.ui.components.settings.gemSectionModifier
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import com.dessalines.thumbkey.ui.components.settings.GemButton as Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import com.dessalines.thumbkey.ui.components.settings.GemOutlinedButton as OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.ui.components.keyboard.CUSTOM_THEME_ID
import com.dessalines.thumbkey.ui.components.keyboard.ThemeDocument
import com.dessalines.thumbkey.ui.components.keyboard.ThemeEngine

/** Saved-theme selection and import/export; editing opens the advanced surface editor. */
@Composable
fun ThemeManagerSection(onThemeApplied: () -> Unit, onEditTheme: () -> Unit) {
    val context = LocalContext.current
    ThemeEngine.ensureMigrated(context)

    var refresh by remember { mutableStateOf(0) }
    var expanded by remember { mutableStateOf(false) }
    var naming by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }

    fun active(): ThemeDocument = ThemeEngine.loadActive(context)
    fun themes(): List<ThemeDocument> = ThemeEngine.listThemes(context)
    fun snapshotCustom(): ThemeDocument {
        ThemeEngine.saveCustom(context, ThemeEngine.captureCurrent(context, "Custom", CUSTOM_THEME_ID))
        return ThemeEngine.loadCustom(context)
    }
    fun changed(text: String? = null) {
        refresh++
        message = text
        onThemeApplied()
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            busy = true
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val stream = BufferedInputStream(requireNotNull(context.contentResolver.openInputStream(uri)))
                    stream.use {
                        it.mark(4)
                        val first = it.read(); val second = it.read(); it.reset()
                        if (first == 'P'.code && second == 'K'.code) ThemeEngine.importPackage(context, it).getOrThrow()
                        else {
                            val output = ByteArrayOutputStream()
                            val buffer = ByteArray(8192)
                            while (true) {
                                val count = it.read(buffer)
                                if (count < 0) break
                                require(output.size() + count <= 1024 * 1024) { "Theme document is too large" }
                                output.write(buffer, 0, count)
                            }
                            ThemeEngine.importIntoCustom(context, output.toString("UTF-8")).getOrThrow()
                        }
                    }
                }
            }
            busy = false
            result.fold(
                onSuccess = { changed("Imported into Custom, including assets") },
                onFailure = { message = "Import failed: ${it.message ?: "invalid theme"}" },
            )
        }
    }

    var pendingExport by remember { mutableStateOf<ThemeDocument?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        val theme = pendingExport
        pendingExport = null
        if (uri != null && theme != null) scope.launch {
            busy = true
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { ThemeEngine.exportPackage(context, theme, it) }
                        ?: error("Could not open destination")
                }
            }
            busy = false
            result.fold(
                onSuccess = { message = "Theme exported with its assets" },
                onFailure = { message = "Export failed: ${it.message ?: "could not write file"}" },
            )
        }
    }

    @Suppress("UNUSED_VARIABLE") val refreshToken = refresh
    val current = active()

    Column(
        modifier = gemSectionModifier(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("✦ Saved themes", style = MaterialTheme.typography.titleMedium)
        Text("Images, GIFs and fonts live in Keywi's theme folders. Exports include these assets.", style = MaterialTheme.typography.bodySmall)
        if (busy) Text("Copying theme files…", style = MaterialTheme.typography.bodySmall)
        if (ThemeEngine.missingAssets(context, current).isNotEmpty()) {
            Text("Some original files are unavailable. Open Advanced theme editor and re-select the missing image, GIF or font.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Text(
            if (current.id == CUSTOM_THEME_ID) "Custom is your scratch theme — edits are safe here."
            else "Edit works in Custom; your saved theme stays intact.",
            style = MaterialTheme.typography.bodySmall,
        )

        Column {
            OutlinedButton(enabled = !busy, onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
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
                    enabled = !busy,
                    onClick = {
                        ThemeEngine.beginEditing(context, current)
                        changed("Copied ${current.name} into Custom")
                        onEditTheme()
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("Edit") }
            } else {
                Button(enabled = !busy, onClick = { naming = true }, modifier = Modifier.weight(1f)) { Text("Save as…") }
            }
            OutlinedButton(
                enabled = !busy,
                onClick = { importLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed", "application/json", "text/json", "text/plain", "application/octet-stream")) },
                modifier = Modifier.weight(1f),
            ) { Text("Import") }
            OutlinedButton(
                enabled = !busy,
                onClick = {
                    runCatching { if (current.id == CUSTOM_THEME_ID) snapshotCustom() else current }.fold(
                        onSuccess = { exportTheme ->
                            pendingExport = exportTheme
                            exportLauncher.launch("${safeThemeFileName(exportTheme.name)}.keywi-theme.zip")
                        },
                        onFailure = { message = "Export failed: ${it.message}" },
                    )
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
                    enabled = name.isNotBlank() && !busy,
                    onClick = {
                        val selectedName = name
                        scope.launch {
                            busy = true
                            val result = withContext(Dispatchers.IO) { runCatching { snapshotCustom(); ThemeEngine.saveCustomAs(context, selectedName) } }
                            busy = false
                            result.fold(
                                onSuccess = { saved -> name = ""; naming = false; changed("Saved ${saved.name}") },
                                onFailure = { message = "Save failed: ${it.message}"; naming = false },
                            )
                        }
                    },
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { naming = false }) { Text("Cancel") } },
        )
    }
}

private fun safeThemeFileName(name: String): String =
    name.trim().ifBlank { "keywi-theme" }.replace(Regex("[^A-Za-z0-9._-]+"), "-").trim('-')
