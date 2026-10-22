package com.dessalines.thumbkey.ui.components.settings.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dessalines.thumbkey.ui.components.common.KEYWI_GITHUB_URL
import com.dessalines.thumbkey.ui.components.common.THUMBKEY_GITHUB_URL
import com.dessalines.thumbkey.ui.components.common.THUMBKEY_LIBERAPAY_URL
import com.dessalines.thumbkey.ui.components.common.THUMBKEY_PATREON_URL
import com.dessalines.thumbkey.ui.components.settings.GemNavigationPreference
import com.dessalines.thumbkey.ui.components.settings.GemSettingsTopAppBar
import com.dessalines.thumbkey.ui.components.settings.gemSectionModifier
import com.dessalines.thumbkey.utils.getVersionCode
import com.dessalines.thumbkey.utils.openLink

const val USER_GUIDE_URL = "https://github.com/dessalines/thumb-key#user-guide"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    val context = LocalContext.current
    Scaffold(
        containerColor =
            if (com.dessalines.thumbkey.ui.components.settings.LocalSettingsDrip.current !=
                null
            ) {
                androidx.compose.ui.graphics.Color.Transparent
            } else {
                MaterialTheme.colorScheme.background
            },
        topBar = { GemSettingsTopAppBar(text = "About Keywi", navController = navController) },
    ) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Column(gemSectionModifier(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("◇ KEYWI", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                Text("Build ${context.getVersionCode()}", style = MaterialTheme.typography.labelMedium)
                Text(
                    "A colorful keyboard fork grown from Thumb-Key. Keywi's changes, releases and issue tracker live in its own repository.",
                )
            }
            AboutLink("Keywi release history", "Keywi builds and patch notes", "$KEYWI_GITHUB_URL/releases")
            AboutLink("Keywi issue tracker", "Report Keywi bugs and requests here", "$KEYWI_GITHUB_URL/issues")
            AboutLink("Keywi GitHub", "Source code for this Keywi fork", KEYWI_GITHUB_URL)
            SettingsDivider()
            Column(gemSectionModifier(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Thumb-Key · upstream project", style = MaterialTheme.typography.titleLarge)
                Text("Thank you to Thumb-Key's developers and contributors for the foundation.")
                Text(
                    "These links are for THUMB-KEY, NOT KEYWI. Patreon and Liberapay donations support Thumb-Key's developer, not Keywi.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            AboutLink("Thumb-Key GitHub (upstream)", "Thumb-Key source code; separate from Keywi", THUMBKEY_GITHUB_URL)
            AboutLink("Thumb-Key Patreon", "Supports THUMB-KEY, NOT KEYWI", THUMBKEY_PATREON_URL)
            AboutLink("Thumb-Key Liberapay", "Supports THUMB-KEY, NOT KEYWI", THUMBKEY_LIBERAPAY_URL)
        }
    }
}

@Composable
private fun AboutLink(
    title: String,
    summary: String,
    url: String,
) {
    val context = LocalContext.current
    GemNavigationPreference(title = { Text(title) }, summary = { Text(summary) }, onClick = { openLink(url, context) })
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
}
