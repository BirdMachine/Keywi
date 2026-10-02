package com.dessalines.thumbkey.ui.components.settings.debug

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.dessalines.thumbkey.BuildConfig
import com.dessalines.thumbkey.diagnostics.KeywiDiagnostics
import com.dessalines.thumbkey.diagnostics.writeDiagnosticExport
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class DiagnosticSnapshot(
    val crashes: List<File>,
    val exits: List<String>,
    val log: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedDebugScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var refresh by remember { mutableStateOf(0) }
    var exporting by remember { mutableStateOf(false) }
    var clearing by remember { mutableStateOf(false) }
    val snapshot by produceState<DiagnosticSnapshot?>(null, refresh) {
        value = withContext(Dispatchers.IO) {
            DiagnosticSnapshot(
                KeywiDiagnostics.crashReports(context),
                KeywiDiagnostics.systemExitHistory(context),
                KeywiDiagnostics.readLog(context),
            )
        }
    }

    fun export(crash: File? = null) {
        if (exporting) return
        exporting = true
        scope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    writeDiagnosticExport(
                        File(context.cacheDir, "diagnostic-exports"),
                        crash?.nameWithoutExtension ?: "keywi-diagnostics",
                        crash?.readText() ?: KeywiDiagnostics.buildReport(context),
                    )
                }
                shareDiagnosticFile(context, file, crash != null)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                Toast.makeText(context, "Couldn't export this report. Please try again.", Toast.LENGTH_LONG).show()
            } finally {
                exporting = false
            }
        }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Debug & Diagnostics") }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(horizontal = 16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Keywi black box ✈", style = MaterialTheme.typography.headlineSmall)
                    Text("Local diagnostics for crashes, ANRs, process deaths, and keyboard performance. Typed text is not intentionally recorded.")
                    Text("Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { refresh++ }, enabled = !clearing) { Text("Refresh") }
                        OutlinedButton(
                            onClick = {
                                clearing = true
                                scope.launch {
                                    try {
                                        withContext(Dispatchers.IO) { KeywiDiagnostics.clear(context) }
                                        refresh++
                                    } catch (error: CancellationException) {
                                        throw error
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "Couldn't clear diagnostics.", Toast.LENGTH_LONG).show()
                                    } finally {
                                        clearing = false
                                    }
                                }
                            },
                            enabled = !exporting && !clearing,
                        ) { Text(if (clearing) "Clearing…" else "Clear") }
                    }
                    OutlinedButton(
                        onClick = { export() },
                        enabled = !exporting && !clearing,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(if (exporting) "Preparing export…" else "Export / share all diagnostics") }
                    HorizontalDivider()
                    Text("Crash reports (${snapshot?.crashes?.size ?: 0})", style = MaterialTheme.typography.titleLarge)
                    Text("Expand a report to read or select its full text. Swipe inside the report to scroll in either direction.")
                    if (snapshot == null) Text("Loading diagnostics…")
                    else if (snapshot?.crashes?.isEmpty() == true) Text("No locally captured crashes yet. Tiny victory. 🌱")
                }
            }
            items(snapshot?.crashes.orEmpty(), key = { it.name }) { file ->
                CrashReportCard(file, refresh, !exporting && !clearing) { export(file) }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HorizontalDivider()
                    Text("Android process exits", style = MaterialTheme.typography.titleLarge)
                    val exits = snapshot?.exits.orEmpty()
                    if (exits.isEmpty()) Text("No process-exit history available.")
                    else exits.forEach { Text(it, fontFamily = FontFamily.Monospace) }
                    HorizontalDivider()
                    Text("Recent Keywi log", style = MaterialTheme.typography.titleLarge)
                    val log = snapshot?.log.orEmpty()
                    if (log.isBlank()) Text("No events recorded yet.")
                    else ReportTextViewport(log.takeLast(40_000))
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    ) { Text("Back to settings") }
                }
            }
        }
    }
}

@Composable
private fun CrashReportCard(file: File, refresh: Int, exportEnabled: Boolean, onExport: () -> Unit) {
    var expanded by rememberSaveable(file.name) { mutableStateOf(false) }
    val report by produceState<String?>(null, file.path, refresh, expanded) {
        value = if (expanded) {
            withContext(Dispatchers.IO) {
                runCatching { file.readText() }.getOrDefault("Couldn't read this report. Refresh and try again.")
            }
        } else null
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(formatDate(file.lastModified()), style = MaterialTheme.typography.titleMedium)
            Text(file.name, style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Collapse" else "Expand") }
                OutlinedButton(onClick = onExport, enabled = exportEnabled) { Text("Export .txt") }
            }
            if (expanded) {
                if (report == null) Text("Loading report…") else ReportTextViewport(report.orEmpty())
            }
        }
    }
}

@Composable
private fun ReportTextViewport(text: String) {
    Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
        SelectionContainer {
            Text(
                text = text,
                modifier = Modifier.verticalScroll(rememberScrollState()).horizontalScroll(rememberScrollState()),
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall,
                softWrap = false,
            )
        }
    }
}

private fun shareDiagnosticFile(context: Context, file: File, individualCrash: Boolean) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.diagnostics", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, if (individualCrash) "Keywi crash report" else "Keywi diagnostic report")
        putExtra(Intent.EXTRA_STREAM, uri)
        clipData = ClipData.newRawUri("Keywi diagnostic report", uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Keywi diagnostics"))
}

private fun formatDate(value: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(value))
