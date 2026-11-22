package com.dessalines.thumbkey.ui.components.settings.lookandfeel

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.ui.components.keyboard.BackdropVisualLayer
import com.dessalines.thumbkey.ui.components.keyboard.CUSTOM_THEME_ID
import com.dessalines.thumbkey.ui.components.keyboard.ThemeAssetStore
import com.dessalines.thumbkey.ui.components.keyboard.ThemeDocument
import com.dessalines.thumbkey.ui.components.keyboard.ThemeEngine
import com.dessalines.thumbkey.ui.components.keyboard.ThemeLibraryStrip
import com.dessalines.thumbkey.ui.components.keyboard.ThemeLibraryTile
import com.dessalines.thumbkey.ui.components.settings.gemSectionModifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import com.dessalines.thumbkey.ui.components.settings.GemButton as Button
import com.dessalines.thumbkey.ui.components.settings.GemOutlinedButton as OutlinedButton

/** Saved-theme selection and import/export; editing opens the advanced surface editor. */
@Composable
fun ThemeManagerSection(
    onThemeApplied: () -> Unit,
    onEditTheme: () -> Unit,
) {
    val context = LocalContext.current
    ThemeEngine.ensureMigrated(context)

    var refresh by remember { mutableStateOf(0) }
    var expanded by remember { mutableStateOf(false) }
    var naming by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var overwriteTarget by remember { mutableStateOf<ThemeDocument?>(null) }
    var updateTarget by remember { mutableStateOf<ThemeDocument?>(null) }
    var deleteTarget by remember { mutableStateOf<ThemeDocument?>(null) }
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

    fun saveNamed(selectedName: String, overwriteId: String? = null) {
        scope.launch {
            busy = true
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    snapshotCustom()
                    ThemeEngine.saveCustomAs(context, selectedName, overwriteId)
                }
            }
            busy = false
            result.fold(
                onSuccess = { saved ->
                    name = ""
                    nameError = null
                    naming = false
                    changed(if (overwriteId == null) "Saved ${saved.name}" else "Updated ${saved.name}")
                },
                onFailure = { nameError = "Save failed: ${it.message}" },
            )
        }
    }

    val importLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                scope.launch {
                    busy = true
                    val result =
                        withContext(Dispatchers.IO) {
                            runCatching {
                                val stream = BufferedInputStream(requireNotNull(context.contentResolver.openInputStream(uri)))
                                stream.use {
                                    it.mark(4)
                                    val first = it.read()
                                    val second = it.read()
                                    it.reset()
                                    if (first == 'P'.code && second == 'K'.code) {
                                        ThemeEngine.importPackage(context, it).getOrThrow()
                                    } else {
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
        }

    var pendingExport by remember { mutableStateOf<ThemeDocument?>(null) }
    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
            val theme = pendingExport
            pendingExport = null
            if (uri != null && theme != null) {
                scope.launch {
                    busy = true
                    val result =
                        withContext(Dispatchers.IO) {
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
        }

    @Suppress("UNUSED_VARIABLE")
    val refreshToken = refresh
    val current = active()

    Column(
        modifier = gemSectionModifier(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("✦ Saved themes", style = MaterialTheme.typography.titleMedium)
        Text(
            "Themes include images, GIFs, fonts and key-flash colors, fade duration and drag-trail settings.",
            style = MaterialTheme.typography.bodySmall,
        )
        if (busy) Text("Copying theme files…", style = MaterialTheme.typography.bodySmall)
        if (ThemeEngine.missingAssets(context, current).isNotEmpty()) {
            Text(
                "Some original files are unavailable. Open Advanced theme editor and re-select the missing image, GIF or font.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Text(
            if (current.id == CUSTOM_THEME_ID) {
                "Custom is your scratch theme — edits are safe here."
            } else {
                "Edit works in Custom; your saved theme stays intact."
            },
            style = MaterialTheme.typography.bodySmall,
        )

        // Library tiles are rendered with each theme's actual saved backdrop.
        // The renderer resolves app-owned assets instead of depending on source URIs.
        val libraryThemes = listOf(ThemeEngine.customPreview(context)) + themes()
        ThemeLibraryStrip(
            themes =
                libraryThemes.map { theme ->
                    ThemeLibraryTile(id = theme.id, name = theme.name) {
                        Box(Modifier.fillMaxSize()) {
                            BackdropVisualLayer(
                                state =
                                    theme.backdrop.copy(
                                        mediaUri = ThemeAssetStore.resolve(context, theme.id, theme.backdrop.mediaUri),
                                    ),
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                },
            selectedId = current.id,
            onSelect = { id ->
                if (!busy && id != current.id) {
                    libraryThemes.firstOrNull { it.id == id }?.let {
                        ThemeEngine.apply(context, it)
                        changed()
                    }
                }
            },
        )

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
                onClick = {
                    importLauncher.launch(
                        arrayOf(
                            "application/zip",
                            "application/x-zip-compressed",
                            "application/json",
                            "text/json",
                            "text/plain",
                            "application/octet-stream",
                        ),
                    )
                },
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

        if (ThemeEngine.isSavedTheme(context, current.id)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(enabled = !busy, onClick = { updateTarget = current }, modifier = Modifier.weight(1f)) {
                    Text("Update saved theme")
                }
                OutlinedButton(enabled = !busy, onClick = { deleteTarget = current }, modifier = Modifier.weight(1f)) {
                    Text("Delete theme…")
                }
            }
        }

        if (current.id == CUSTOM_THEME_ID) {
            TextButton(enabled = !busy, onClick = {
                ThemeEngine.resetCustom(context)
                changed("Custom reset to defaults")
            }) {
                Text("Reset Custom")
            }
        }
        message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
    }

    if (naming && overwriteTarget == null) {
        AlertDialog(
            onDismissRequest = { if (!busy) naming = false },
            title = { Text("Save Custom as theme") },
            text = {
                Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    enabled = !busy,
                    isError = nameError != null,
                    singleLine = true,
                    label = { Text("Theme name") },
                )
                nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = name.isNotBlank() && !busy,
                    onClick = {
                        val selectedName = name.trim()
                        val matches = ThemeEngine.themesNamed(context, selectedName)
                        when {
                            matches.isEmpty() -> saveNamed(selectedName)
                            matches.size > 1 -> nameError = "Several themes have this name. Choose a unique name, or select one and use Update saved theme."
                            !ThemeEngine.isSavedTheme(context, matches.single().id) -> nameError = "Built-in themes cannot be overwritten. Choose another name."
                            else -> overwriteTarget = matches.single()
                        }
                    },
                ) { Text("Save") }
            },
            dismissButton = { TextButton(enabled = !busy, onClick = { naming = false }) { Text("Cancel") } },
        )
    }

    overwriteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { overwriteTarget = null },
            title = { Text("Name exists — overwrite?") },
            text = { Text("Replace “${target.name}” with your current Custom theme? The saved theme keeps its identity.") },
            confirmButton = {
                TextButton(onClick = {
                    overwriteTarget = null
                    saveNamed(name.trim(), target.id)
                }) { Text("Overwrite") }
            },
            dismissButton = { TextButton(onClick = { overwriteTarget = null }) { Text("Cancel") } },
        )
    }

    updateTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { updateTarget = null },
            title = { Text("Update saved theme?") },
            text = { Text("Save the current appearance, including key-flash settings, over “${target.name}”? Custom is kept intact.") },
            confirmButton = {
                TextButton(onClick = {
                    updateTarget = null
                    scope.launch {
                        busy = true
                        val result = withContext(Dispatchers.IO) { runCatching { ThemeEngine.updateSavedTheme(context, target.id) } }
                        busy = false
                        result.fold(
                            onSuccess = { changed("Updated ${it.name}") },
                            onFailure = { message = "Update failed: ${it.message}" },
                        )
                    }
                }) { Text("Update") }
            },
            dismissButton = { TextButton(onClick = { updateTarget = null }) { Text("Cancel") } },
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete saved theme?") },
            text = { Text("Delete “${target.name}” and its saved media? If active, the keyboard switches to Custom. Your Custom edits are kept.") },
            confirmButton = {
                TextButton(onClick = {
                    deleteTarget = null
                    scope.launch {
                        busy = true
                        val result = withContext(Dispatchers.IO) { runCatching { ThemeEngine.deleteSavedTheme(context, target.id) } }
                        busy = false
                        result.fold(
                            onSuccess = { changed("Deleted ${target.name}") },
                            onFailure = { message = "Delete failed: ${it.message}" },
                        )
                    }
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } },
        )
    }
}

private fun safeThemeFileName(name: String): String =
    name
        .trim()
        .ifBlank { "keywi-theme" }
        .replace(Regex("[^A-Za-z0-9._-]+"), "-")
        .trim('-')
