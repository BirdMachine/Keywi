package com.dessalines.thumbkey.ui.components.settings

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpCenter
import androidx.compose.material.icons.outlined.AppRegistration
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.InstallMobile
import androidx.compose.material.icons.outlined.KeyboardAlt
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dessalines.thumbkey.R
import com.dessalines.thumbkey.db.AppSettingsViewModel
import com.dessalines.thumbkey.db.DEFAULT_KEYBOARD_LAYOUT
import com.dessalines.thumbkey.ui.components.common.SettingsNoticeBanners
import com.dessalines.thumbkey.ui.components.common.TestOutTextField
import com.dessalines.thumbkey.ui.components.settings.about.SettingsDivider
import com.dessalines.thumbkey.ui.components.settings.about.USER_GUIDE_URL
import com.dessalines.thumbkey.ui.components.settings.gemPreferenceModifier
import com.dessalines.thumbkey.utils.KeyboardLayout
import com.dessalines.thumbkey.utils.TAG
import com.dessalines.thumbkey.utils.getVersionCode
import com.dessalines.thumbkey.utils.keyboardLayoutsSetFromDbIndexString
import com.dessalines.thumbkey.utils.openLink
import com.dessalines.thumbkey.utils.updateLayouts
import me.zhanghai.compose.preference.MultiSelectListPreference
import me.zhanghai.compose.preference.ProvidePreferenceTheme
import com.dessalines.thumbkey.ui.components.settings.GemNavigationPreference as Preference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    appSettingsViewModel: AppSettingsViewModel,
    thumbkeyEnabled: Boolean,
    thumbkeySelected: Boolean,
) {
    Log.d(TAG, "Got to settings activity")

    val snackbarHostState = remember { SnackbarHostState() }
    val ctx = LocalContext.current
    val settings by appSettingsViewModel.appSettings.observeAsState()
    val scrollState = rememberScrollState()
    val layoutsState = keyboardLayoutsSetFromDbIndexString(settings?.keyboardLayouts)

    Scaffold(
        containerColor =
            if (com.dessalines.thumbkey.ui.components.settings.LocalSettingsDrip.current !=
                null
            ) {
                androidx.compose.ui.graphics.Color.Transparent
            } else {
                MaterialTheme.colorScheme.background
            },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            GemSettingsTopAppBar(text = "KEYWI ✦", navController = navController, showBack = false)
        },
        content = { padding ->
            Column(
                modifier =
                    Modifier
                        .padding(padding)
                        .verticalScroll(scrollState)
                        .background(
                            color =
                                if (com.dessalines.thumbkey.ui.components.settings.LocalSettingsDrip.current !=
                                    null
                                ) {
                                    androidx.compose.ui.graphics.Color.Transparent
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                        ).imePadding(),
            ) {
                SettingsThemeBanner()
                SettingsNoticeBanners(
                    versionCode = ctx.getVersionCode(),
                    lastViewedVersion = settings?.lastVersionCodeViewed,
                    onWelcomeViewed = { appSettingsViewModel.updateLastVersionCodeViewed(ctx.getVersionCode()) },
                )
                ProvidePreferenceTheme {
                    if (!(thumbkeyEnabled || thumbkeySelected)) {
                        Preference(
                            title = { Text(stringResource(R.string.setup)) },
                            icon = { Icon(Icons.Outlined.InstallMobile, contentDescription = null) },
                            onClick = { navController.navigate("setup") },
                        )
                    }

                    GemSettingsGroupLabel("◇ ADVANCED", "Shape, surfaces and input tools")
                    Preference(
                        title = { Text("Gem cuts") },
                        summary = { Text("Choose physical keyboard geometry independently from theme and layout") },
                        icon = { Icon(Icons.Outlined.KeyboardAlt, contentDescription = null) },
                        onClick = { navController.navigate("gemCuts") },
                    )
                    Preference(
                        title = { Text("Themes & appearance") },
                        summary = { Text("Advanced theme editor: gradients, GIFs, images, keys, toolbar, fonts and saved themes") },
                        icon = { Icon(Icons.Outlined.AutoAwesome, contentDescription = null) },
                        onClick = { navController.navigate("advancedLookAndFeel") },
                    )
                    Preference(
                        title = { Text("Board management") },
                        summary = { Text("Custom rooms, A/B sides, key assignments, and room options") },
                        icon = { Icon(Icons.Outlined.DashboardCustomize, contentDescription = null) },
                        onClick = { navController.navigate("advancedBoards") },
                    )
                    SettingsAppearancePreference()
                    Preference(
                        title = { Text("Key & Word Selection") },
                        summary = { Text("Matrix remapper, unfinished-word chip, and personal dictionary") },
                        icon = { Icon(Icons.Outlined.KeyboardAlt, contentDescription = null) },
                        onClick = { navController.navigate("advancedKeyWordSelection") },
                    )
                    Preference(
                        title = { Text(stringResource(R.string.advanced_input)) },
                        summary = { Text("Gestures, typing behavior, context, characters and hardware keyboard options") },
                        icon = { Icon(Icons.Outlined.Tune, contentDescription = null) },
                        onClick = { navController.navigate("advancedInput") },
                    )
                    Preference(
                        title = { Text("Keypress effects") },
                        summary = { Text("Sound packs, action assignments and touch-through GIF / PNG overlays") },
                        icon = { Icon(Icons.Outlined.TouchApp, contentDescription = null) },
                        onClick = { navController.navigate("keypressEffects") },
                    )
                    Preference(
                        title = { Text("Power options") },
                        summary = { Text("Balance Keywi's motion, effects, and refresh rate against battery use") },
                        icon = { Icon(Icons.Outlined.BatteryChargingFull, contentDescription = null) },
                        onClick = { navController.navigate("advancedPowerOptions") },
                    )
                    Preference(
                        title = { Text("Debug & diagnostics") },
                        summary = { Text("Keywi logs, crash reports, process exits, and performance breadcrumbs") },
                        icon = { Icon(Icons.Outlined.BugReport, contentDescription = null) },
                        onClick = { navController.navigate("advancedDebug") },
                    )
                    SettingsDivider()

                    GemSettingsGroupLabel("✦ SETTINGS", "Character layouts, clipboard and saved data")
                    MultiSelectListPreference(
                        modifier = gemPreferenceModifier(),
                        value = layoutsState,
                        values = KeyboardLayout.entries.sortedBy { it.keyboardDefinition.title },
                        valueToText = { AnnotatedString(it.keyboardDefinition.title) },
                        onValueChange = {
                            val update = it.ifEmpty { keyboardLayoutsSetFromDbIndexString(DEFAULT_KEYBOARD_LAYOUT.toString()) }
                            updateLayouts(appSettingsViewModel, update)
                        },
                        icon = { Icon(Icons.Outlined.KeyboardAlt, contentDescription = null) },
                        title = { Text("Character layouts") },
                        summary = { Text(layoutsState.joinToString(", ") { it.keyboardDefinition.title }) },
                    )

                    Preference(
                        title = { Text(stringResource(R.string.clipboard_history)) },
                        icon = { Icon(Icons.Outlined.ContentPaste, contentDescription = null) },
                        onClick = { navController.navigate("clipboardSettings") },
                    )

                    Preference(
                        title = { Text(stringResource(R.string.backup_and_restore)) },
                        icon = { Icon(Icons.Outlined.Restore, contentDescription = null) },
                        onClick = { navController.navigate("backupAndRestore") },
                    )

                    GemSettingsGroupLabel("⌘ HELP", "Guides, releases and project links")
                    Preference(
                        title = { Text("Thumb-Key user guide (upstream)") },
                        icon = { Icon(Icons.AutoMirrored.Outlined.HelpCenter, contentDescription = null) },
                        onClick = { openLink(USER_GUIDE_URL, ctx) },
                    )
                    Preference(
                        title = { Text(stringResource(R.string.about)) },
                        icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                        onClick = { navController.navigate("about") },
                    )
                    SettingsDivider()
                    TestOutTextField()
                }
            }
        },
    )
}
