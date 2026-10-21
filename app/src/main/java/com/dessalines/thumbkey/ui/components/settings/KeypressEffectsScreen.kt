package com.dessalines.thumbkey.ui.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dessalines.thumbkey.ui.components.settings.advancedsound.AdvancedSoundScreen
import com.dessalines.thumbkey.ui.components.settings.lookandfeel.KeywiThemeTestBench

/** One front door for action feedback, kept separate from appearance and input behavior. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeypressEffectsScreen(navController: NavController) {
    Scaffold(
        containerColor = if (LocalSettingsDrip.current != null) Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = { GemSettingsTopAppBar("Keypress effects", navController) },
    ) { padding ->
        Column(
            Modifier.padding(padding).verticalScroll(rememberScrollState()).imePadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GemSettingsGroupLabel("✦ ACTION FEEDBACK", "Give taps, swipes and holds their own character")
            GemNavigationPreference(
                title = { Text("Sounds & sound packs") },
                summary = { Text("Built-in and imported clips, direction / key assignments, playback order and timing") },
                icon = { Icon(Icons.Outlined.MusicNote, contentDescription = null) },
                onClick = { navController.navigate("keypressSounds") },
            )
            GemNavigationPreference(
                title = { Text("GIF / PNG overlays") },
                summary = { Text("Animated action accents, key or center placement, layers and playback rules") },
                icon = { Icon(Icons.Outlined.AutoAwesome, contentDescription = null) },
                onClick = { navController.navigate("typingOverlay") },
            )
            KeywiThemeTestBench()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeypressSoundSettingsScreen(navController: NavController) {
    Scaffold(topBar = { GemSettingsTopAppBar("Sounds & sound packs", navController) }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AdvancedSoundScreen()
            KeywiThemeTestBench()
        }
    }
}
