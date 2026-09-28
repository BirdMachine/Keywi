package com.dessalines.thumbkey.ui.components.settings.debug

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dessalines.thumbkey.BuildConfig
import com.dessalines.thumbkey.diagnostics.KeywiDiagnostics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedDebugScreen(navController: NavController) {
    val context = LocalContext.current
    val refresh = remember { mutableStateOf(0) }
    val crashes = remember(refresh.value) { KeywiDiagnostics.crashReports(context) }
    val exits = remember(refresh.value) { KeywiDiagnostics.systemExitHistory(context) }
    val log = remember(refresh.value) { KeywiDiagnostics.readLog(context) }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Debug & Diagnostics") }) },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(horizontal = 16.dp).fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Keywi black box ✈", style = MaterialTheme.typography.headlineSmall)
            Text("Local diagnostics for chasing crashes, ANRs, process deaths, and weird keyboard gremlins. Typed text is not intentionally recorded.")
            Text("Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { refresh.value++ }) { Text("Refresh") }
                OutlinedButton(onClick = {
                    val report = KeywiDiagnostics.buildReport(context)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Keywi diagnostic report")
                        putExtra(Intent.EXTRA_TEXT, report)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Keywi diagnostics"))
                }) { Text("Export / share") }
                OutlinedButton(onClick = {
                    KeywiDiagnostics.clear(context)
                    refresh.value++
                }) { Text("Clear") }
            }

            HorizontalDivider()
            Text("Crash reports (${crashes.size})", style = MaterialTheme.typography.titleLarge)
            if (crashes.isEmpty()) Text("No locally captured crashes yet. Tiny victory. 🌱")
            crashes.take(10).forEach { file ->
                Text(
                    "${formatDate(file.lastModified())}\n${runCatching { file.readText() }.getOrDefault("<unreadable>").take(3500)}",
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                )
                HorizontalDivider()
            }

            Text("Android process exits", style = MaterialTheme.typography.titleLarge)
            if (exits.isEmpty()) Text("No process-exit history available.") else exits.forEach { Text(it, fontFamily = FontFamily.Monospace) }

            HorizontalDivider()
            Text("Recent Keywi log", style = MaterialTheme.typography.titleLarge)
            Text(
                if (log.isBlank()) "No events recorded yet." else log.takeLast(40_000),
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall,
            )

            Text("← Back", modifier = Modifier.padding(vertical = 16.dp))
            OutlinedButton(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) { Text("Back to settings") }
        }
    }
}

private fun formatDate(value: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(value))
