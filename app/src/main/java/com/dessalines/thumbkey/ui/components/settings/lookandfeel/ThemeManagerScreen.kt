package com.dessalines.thumbkey.ui.components.settings.lookandfeel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dessalines.thumbkey.utils.SimpleTopAppBar

/**
 * Visible front door for Keywi themes.
 *
 * Keep the legacy detailed appearance editor reachable while the compact redesign is in progress,
 * but make theme selection/import/export the first thing users see when entering Advanced look & feel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeManagerScreen(navController: NavController) {
    Scaffold(
        topBar = {
            SimpleTopAppBar(text = "Theme manager", navController = navController)
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ThemeManagerSection(onThemeApplied = {})

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("Appearance editor", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Edit backdrop, toolbar, keys, typography, gradients, and effects for the active theme.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Button(
                    onClick = { navController.navigate("advancedLookAndFeelEditor") },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Edit active theme")
                }
            }
        }
    }
}