package com.dessalines.thumbkey

import android.app.Application
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dessalines.thumbkey.db.AppDB
import com.dessalines.thumbkey.db.AppSettingsRepository
import com.dessalines.thumbkey.db.AppSettingsViewModel
import com.dessalines.thumbkey.db.AppSettingsViewModelFactory
import com.dessalines.thumbkey.db.ClipboardDB
import com.dessalines.thumbkey.db.ClipboardRepository
import com.dessalines.thumbkey.diagnostics.KeywiDiagnostics
import com.dessalines.thumbkey.ui.components.common.ShowChangelog
import com.dessalines.thumbkey.ui.components.settings.SettingsScreen
import com.dessalines.thumbkey.ui.components.settings.about.AboutScreen
import com.dessalines.thumbkey.ui.components.settings.advancedinput.AdvancedCharactersScreen
import com.dessalines.thumbkey.ui.components.settings.advancedinput.AdvancedInputScreen
import com.dessalines.thumbkey.ui.components.settings.advancedinput.ContextEngineScreen
import com.dessalines.thumbkey.ui.components.settings.backupandrestore.BackupAndRestoreScreen
import com.dessalines.thumbkey.ui.components.settings.behavior.BehaviorScreen
import com.dessalines.thumbkey.ui.components.settings.clipboard.ClipboardSettingsScreen
import com.dessalines.thumbkey.ui.components.settings.debug.AdvancedDebugScreen
import com.dessalines.thumbkey.ui.components.settings.lookandfeel.AdvancedLookAndFeelScreen
import com.dessalines.thumbkey.ui.components.settings.lookandfeel.LookAndFeelScreen
import com.dessalines.thumbkey.ui.components.settings.modifykeys.AdvancedKeyWordSelectionScreen
import com.dessalines.thumbkey.ui.components.settings.modifykeys.ModifyKeysScreen
import com.dessalines.thumbkey.ui.components.settings.other.OtherSettingsScreen
import com.dessalines.thumbkey.ui.components.settings.power.AdvancedPowerOptionsScreen
import com.dessalines.thumbkey.ui.components.setup.SetupScreen
import com.dessalines.thumbkey.ui.theme.ThumbkeyTheme
import com.dessalines.thumbkey.utils.ANIMATION_SPEED
import com.dessalines.thumbkey.utils.getImeNames
import com.dessalines.thumbkey.utils.getVersionCode
import org.woheller69.freeDroidWarn.FreeDroidWarn
import splitties.systemservices.inputMethodManager

class ThumbkeyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KeywiDiagnostics.install(this)
    }

    private val database by lazy { AppDB.getDatabase(this) }
    private val clipboardDatabase by lazy { ClipboardDB.getDatabase(this) }
    val appSettingsRepository by lazy { AppSettingsRepository(database.appSettingsDao()) }
    val clipboardRepository by lazy {
        ClipboardRepository(clipboardDatabase.clipboardItemDao(), database.appSettingsDao())
    }
}

class MainActivity : AppCompatActivity() {
    private val appSettingsViewModel: AppSettingsViewModel by viewModels {
        AppSettingsViewModelFactory((application as ThumbkeyApplication).appSettingsRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        KeywiDiagnostics.event("UI", "MainActivity created")
        FreeDroidWarn.showWarningOnUpgrade(this, getVersionCode())
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val settings by appSettingsViewModel.appSettings.observeAsState()
            val ctx = LocalContext.current
            val imeNames = ctx.getImeNames()
            val thumbkeyEnabled = inputMethodManager.enabledInputMethodList.any { imeNames.contains(it.id) }
            val selectedName = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
            val thumbkeySelected = imeNames.contains(selectedName)
            val startDestination by remember {
                mutableStateOf(if (!thumbkeyEnabled) "setup" else intent.extras?.getString("startRoute") ?: "settings")
            }

            ThumbkeyTheme(settings = settings) {
                val navController = rememberNavController()
                if (startDestination == "settings") ShowChangelog(appSettingsViewModel = appSettingsViewModel)
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIMATION_SPEED)) },
                    exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIMATION_SPEED)) },
                    popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIMATION_SPEED)) },
                    popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIMATION_SPEED)) },
                ) {
                    composable("setup") { SetupScreen(navController, thumbkeyEnabled, thumbkeySelected) }
                    composable("settings") { SettingsScreen(navController, appSettingsViewModel, thumbkeyEnabled, thumbkeySelected) }
                    composable("lookAndFeel") { LookAndFeelScreen(navController, appSettingsViewModel) }
                    composable("advancedBoards") { com.dessalines.thumbkey.ui.components.settings.boards.AdvancedBoardManagementScreen(navController) }
                    composable("typingOverlay") { com.dessalines.thumbkey.ui.components.settings.boards.TypingOverlaySettingsScreen(navController) }
                    composable("advancedLookAndFeel") { AdvancedLookAndFeelScreen(navController) }
                    composable("advancedKeyWordSelection") { AdvancedKeyWordSelectionScreen(navController, appSettingsViewModel) }
                    composable("behavior") { BehaviorScreen(navController, appSettingsViewModel) }
                    composable("advancedInput") { AdvancedInputScreen(navController) }
                    composable("contextEngine") { ContextEngineScreen() }
                    composable("advancedCharacters") { AdvancedCharactersScreen() }
                    composable("advancedPowerOptions") { AdvancedPowerOptionsScreen(navController) }
                    composable("advancedDebug") { AdvancedDebugScreen(navController) }
                    composable("clipboardSettings") {
                        ClipboardSettingsScreen(navController, appSettingsViewModel, (application as ThumbkeyApplication).clipboardRepository)
                    }
                    composable("modifyKeys") { ModifyKeysScreen(navController, appSettingsViewModel) }
                    composable("about") { AboutScreen(navController) }
                    composable("backupAndRestore") { BackupAndRestoreScreen(navController, appSettingsViewModel) }
                    composable("otherSettings") { OtherSettingsScreen(navController, appSettingsViewModel) }
                }
            }
        }
    }
}
