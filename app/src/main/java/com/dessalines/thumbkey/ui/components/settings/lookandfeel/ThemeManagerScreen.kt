package com.dessalines.thumbkey.ui.components.settings.lookandfeel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.dessalines.thumbkey.automation.TaskerThemeAutomation
import com.dessalines.thumbkey.ui.components.keyboard.CUSTOM_THEME_ID
import com.dessalines.thumbkey.ui.components.keyboard.ThemeEngine
import com.dessalines.thumbkey.ui.components.settings.GemNavigationPreference
import com.dessalines.thumbkey.ui.components.settings.GemSettingsTopAppBar as SimpleTopAppBar

/** Visible front door for Keywi themes. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeManagerScreen(navController: NavController) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var managerRefresh by remember { mutableIntStateOf(0) }

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) managerRefresh++
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = if (com.dessalines.thumbkey.ui.components.settings.LocalSettingsDrip.current != null) {
            androidx.compose.ui.graphics.Color.Transparent
        } else {
            androidx.compose.material3.MaterialTheme.colorScheme.background
        },
        topBar = { SimpleTopAppBar(text = "Theme manager", navController = navController) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AdvancedThemeEditorEntry(navController)

            key(managerRefresh, TaskerThemeAutomation.revision) {
                ThemeManagerSection(
                    onThemeApplied = { managerRefresh++ },
                    onEditTheme = { navController.navigate("advancedLookAndFeelEditor") },
                )
            }

            GemNavigationPreference(
                title = { Text("Tasker theme updates") },
                summary = { Text("Let your tasks change backgrounds, gradients and border colors.") },
                onClick = { navController.navigate("taskerThemeUpdates") },
            )

            KeywiThemeTestBench()

            GemNavigationPreference(
                title = { Text("Keyboard tuning") },
                summary = {
                    Text(
                        "Size, position, base palette and feedback. For gradients, GIFs and surfaces, use Advanced theme editor above.",
                    )
                },
                onClick = { navController.navigate("lookAndFeel") },
            )
        }
    }
}

/** Opens the full surface editor, forking saved themes into Custom before editing. */
@Composable
fun AdvancedThemeEditorEntry(navController: NavController) {
    val context = LocalContext.current
    GemNavigationPreference(
        title = { Text("Advanced theme editor") },
        summary = { Text("Edit gradients, GIFs, images, key surfaces, borders, toolbar and fonts.") },
        onClick = {
            val active = ThemeEngine.loadActive(context)
            if (active.id != CUSTOM_THEME_ID) ThemeEngine.beginEditing(context, active)
            navController.navigate("advancedLookAndFeelEditor")
        },
    )
}
