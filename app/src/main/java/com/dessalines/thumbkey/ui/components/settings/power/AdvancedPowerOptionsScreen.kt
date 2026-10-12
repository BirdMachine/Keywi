package com.dessalines.thumbkey.ui.components.settings.power

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.dessalines.thumbkey.ui.components.keyboard.KeywiPowerMode
import com.dessalines.thumbkey.ui.components.keyboard.KeywiPowerPreferences
import com.dessalines.thumbkey.ui.components.settings.GemSettingsTopAppBar as SimpleTopAppBar
import com.dessalines.thumbkey.ui.components.settings.GemNavigationPreference as Preference
import me.zhanghai.compose.preference.ProvidePreferenceTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedPowerOptionsScreen(navController: NavController) {
    val context = LocalContext.current
    var selected by remember { mutableStateOf(KeywiPowerPreferences.load(context)) }

    fun choose(mode: KeywiPowerMode) {
        selected = mode
        KeywiPowerPreferences.save(context, mode)
    }

    Scaffold(
        topBar = {
            SimpleTopAppBar(text = "Advanced Power Options", navController = navController)
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
        ) {
            ProvidePreferenceTheme {
                Preference(
                    selected = selected == KeywiPowerMode.UNBRIDLED,
                    showChevron = false,
                    title = { Text("Unbridled") },
                    summary = {
                        Text("No Keywi power limits. Full motion, GIF playback, and fastest suggestion refresh.")
                    },
                    icon = {
                        RadioButton(
                            selected = selected == KeywiPowerMode.UNBRIDLED,
                            onClick = null,
                        )
                    },
                    onClick = { choose(KeywiPowerMode.UNBRIDLED) },
                )
                Preference(
                    selected = selected == KeywiPowerMode.CONSERVED,
                    showChevron = false,
                    title = { Text("Conserved") },
                    summary = {
                        Text("Keeps the pretty stuff while reducing background polling and avoiding needless redraw work.")
                    },
                    icon = {
                        RadioButton(
                            selected = selected == KeywiPowerMode.CONSERVED,
                            onClick = null,
                        )
                    },
                    onClick = { choose(KeywiPowerMode.CONSERVED) },
                )
                Preference(
                    selected = selected == KeywiPowerMode.RESTRICTED,
                    showChevron = false,
                    title = { Text("Restricted") },
                    summary = {
                        Text(
                            "Super low power: GIFs become still images, Keywi animations are disabled, " +
                                "and suggestion polling slows down.",
                        )
                    },
                    icon = {
                        RadioButton(
                            selected = selected == KeywiPowerMode.RESTRICTED,
                            onClick = null,
                        )
                    },
                    onClick = { choose(KeywiPowerMode.RESTRICTED) },
                )
            }
        }
    }
}
