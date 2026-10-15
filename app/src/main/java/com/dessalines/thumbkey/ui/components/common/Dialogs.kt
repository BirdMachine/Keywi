package com.dessalines.thumbkey.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dessalines.thumbkey.ui.components.settings.GemButton
import com.dessalines.thumbkey.utils.openLink
import dev.jeziellago.compose.markdowntext.MarkdownText

const val KEYWI_GITHUB_URL = "https://github.com/BirdMachine/Keywi"
const val THUMBKEY_GITHUB_URL = "https://github.com/dessalines/thumb-key"
const val THUMBKEY_PATREON_URL = "https://www.patreon.com/dessalines"
const val THUMBKEY_LIBERAPAY_URL = "https://liberapay.com/dessalines"
const val GOOGLE_POLICY_URL = "https://developer.android.com/developer-verification"

val UPSTREAM_MARKDOWN = """
    ## Thumb-Key — our upstream project
    Keywi is a separate fork of Thumb-Key. Thank you to Thumb-Key's developers and contributors for the foundation.

    **These links belong to Thumb-Key, NOT Keywi. Patreon and Liberapay donations support Thumb-Key's developer, not Keywi.**

    - [Thumb-Key GitHub — upstream, not Keywi]($THUMBKEY_GITHUB_URL)
    - [Thumb-Key Patreon — supports Thumb-Key, not Keywi]($THUMBKEY_PATREON_URL)
    - [Thumb-Key Liberapay — supports Thumb-Key, not Keywi]($THUMBKEY_LIBERAPAY_URL)
""".trimIndent()

val GOOGLE_POLICY_MARKDOWN = """
    ## Android installation policy
    Google is introducing developer identity verification and app registration for certified Android devices, including apps distributed outside Google Play.

    ### Rollout
    Google's published timeline starts protections on September 30, 2026 for participating stores in Brazil, Indonesia, Singapore and Thailand, with broader expansion in 2027. Availability depends on region, device and distribution channel.

    ### What this means for Keywi
    Installation or updates may require additional steps as this policy reaches your device. This notice does **not** mean Keywi will automatically stop working, and it does **not** announce a Keywi developer verification decision.

    Google describes an advanced installation flow for users choosing unverified apps. Consult the official documentation for current requirements and available options.

    - [Google's developer-verification documentation]($GOOGLE_POLICY_URL)
    - [Google's advanced-flow information](https://android-developers.googleblog.com/2026/03/android-developer-verification.html)
    - [Keep Android Open — independent advocacy](https://keepandroidopen.org/)

    Policy information reviewed October 12, 2026. This banner remains available so you can revisit the full notice.
""".trimIndent()

/** Two permanent banners, with sequential, once-per-build update notices. */
@Composable
fun SettingsNoticeBanners(versionCode: Int, lastViewedVersion: Int?, onWelcomeViewed: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("keywi_notices", 0) }
    var openNotice by rememberSaveable(versionCode) { mutableStateOf<String?>(null) }
    fun openPolicy() { openNotice = "policy" }
    fun closeNotice() {
        if (openNotice == "welcome") {
            onWelcomeViewed()
        } else if (openNotice == "policy") {
            prefs.edit().putInt("policy_seen_version", versionCode).apply()
        }
        openNotice = null
    }


    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NoticeBanner("Welcome to Keywi", "Build $versionCode · tap for what's changed", Icons.Outlined.Diamond) { openNotice = "welcome" }
        NoticeBanner("Android installation policy", "Google verification notice · tap to read in full", Icons.Outlined.Security) { openPolicy() }
    }

    when (openNotice) {
        "welcome" -> {
            val notes = remember(context, versionCode) {
                runCatching { context.assets.open("KEYWI_RELEASES.md").bufferedReader().use { it.readText() } }
                    .getOrDefault("## What's changed\nSee Keywi's release history for this build's notes.")
            }
            NoticeOverlay("Welcome to Keywi", "BUILD $versionCode · WHAT'S NEW", Icons.Outlined.Diamond, notes + "\n\n" + UPSTREAM_MARKDOWN, ::closeNotice)
        }
        "policy" -> NoticeOverlay("Android installation policy", "GOOGLE POLICY · INFORMATION", Icons.Outlined.Security, GOOGLE_POLICY_MARKDOWN, ::closeNotice)
    }
}

@Composable
private fun NoticeBanner(title: String, summary: String, icon: ImageVector, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val shape = CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp)
    val glint = Brush.linearGradient(listOf(scheme.primary, scheme.secondary, scheme.tertiary))
    Row(
        Modifier.fillMaxWidth().clip(shape)
            .background(Brush.linearGradient(listOf(scheme.primaryContainer, scheme.surfaceVariant)))
            .border(1.dp, glint, shape).clickable(role = Role.Button, onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, null, Modifier.size(30.dp), tint = scheme.primary)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = scheme.onSurface)
            Text(summary, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = scheme.tertiary)
    }
}

@Composable
private fun NoticeOverlay(title: String, eyebrow: String, icon: ImageVector, markdown: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val shape = CutCornerShape(topStart = 22.dp, bottomEnd = 22.dp)
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier.padding(16.dp).widthIn(max = 560.dp).fillMaxWidth().fillMaxHeight(.88f)
                .clip(shape).background(scheme.surface)
                .border(1.dp, Brush.linearGradient(listOf(scheme.primary, scheme.secondary, scheme.tertiary)), shape),
        ) {
            Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(scheme.primaryContainer, scheme.surfaceVariant))).padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(eyebrow, style = MaterialTheme.typography.labelSmall, color = scheme.tertiary)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(icon, null, Modifier.size(32.dp), tint = scheme.primary)
                    Text(title, style = MaterialTheme.typography.headlineSmall, color = scheme.onSurface)
                }
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp)) {
                MarkdownText(markdown = markdown, style = MaterialTheme.typography.bodyLarge.copy(color = scheme.onSurface), linkColor = scheme.primary)
            }
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { openLink("$KEYWI_GITHUB_URL/releases", context) }) { Text("Keywi releases") }
                GemButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Done") }
            }
        }
    }
}
