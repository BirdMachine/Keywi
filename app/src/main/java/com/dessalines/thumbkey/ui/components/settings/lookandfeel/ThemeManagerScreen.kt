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
import com.dessalines.thumbkey.ui.components.keyboard.CUSTOM_THEME_ID
import com.dessalines.thumbkey.ui.components.keyboard.ThemeEngine
import com.dessalines.thumbkey.utils.SimpleTopAppBar

/** Visible front door for Keywi themes. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeManagerScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var managerRefresh by remember { mutableIntStateOf(0) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) managerRefresh++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(topBar = { SimpleTopAppBar(text = "Theme manager", navController = navController) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            key(managerRefresh) {
                ThemeManagerSection(onThemeApplied = { managerRefresh++ })
            }

            // Keep a real editor next to theme selection so a theme can be auditioned immediately.
            KeywiThemeTestBench()

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
                    onClick = {
                        val active = ThemeEngine.loadActive(context)
                        if (active.id != CUSTOM_THEME_ID) ThemeEngine.beginEditing(context, active)
                        navController.navigate("advancedLookAndFeelEditor")
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Edit active theme")
                }
            }
        }
    }
}
