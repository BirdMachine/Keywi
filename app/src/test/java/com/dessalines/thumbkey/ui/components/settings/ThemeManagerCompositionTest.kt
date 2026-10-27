package com.dessalines.thumbkey.ui.components.settings

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dessalines.thumbkey.ui.components.keyboard.BackdropMode
import com.dessalines.thumbkey.ui.components.keyboard.BackdropThemePreferences
import com.dessalines.thumbkey.ui.components.keyboard.CUSTOM_THEME_ID
import com.dessalines.thumbkey.ui.components.keyboard.KeywiAppearancePreferences
import com.dessalines.thumbkey.ui.components.keyboard.KeyThemePreferences
import com.dessalines.thumbkey.ui.components.keyboard.ThemeEngine
import com.dessalines.thumbkey.ui.components.settings.lookandfeel.AdvancedLookAndFeelScreen
import com.dessalines.thumbkey.ui.components.settings.lookandfeel.ThemeManagerScreen
import com.dessalines.thumbkey.ui.components.settings.lookandfeel.ThemeManagerSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ThemeManagerCompositionTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun navigationPreferenceComposesWithoutACallerPreferenceProvider() {
        var clicked = false
        compose.setContent {
            MaterialTheme {
                GemNavigationPreference(title = { Text("Keyboard tuning") }, onClick = { clicked = true })
            }
        }
        compose.onNodeWithText("Keyboard tuning").assertIsDisplayed().performClick()
        compose.runOnIdle { assertTrue(clicked) }
    }

    @Test
    fun managerOpensSelectsAndEditsWithoutClobberingSavedTheme() {
        val context =
            androidx.test.core.app.ApplicationProvider
                .getApplicationContext<Application>()
        KeywiAppearancePreferences.save(context, true)
        ThemeEngine.ensureMigrated(context)
        val saved = ThemeEngine.saveCustomAs(context, "Pondly regression")
        val savedJson = ThemeEngine.export(saved)
        val customJson = ThemeEngine.export(ThemeEngine.loadCustom(context))

        compose.setContent {
            MaterialTheme {
                GemSettingsTheme(true) {
                    val navController = rememberNavController()
                    NavHost(navController, startDestination = "manager") {
                        composable("manager") { ThemeManagerScreen(navController) }
                        composable("advancedLookAndFeelEditor") { AdvancedLookAndFeelScreen(navController) }
                    }
                }
            }
        }
        compose.onNodeWithText("Advanced theme editor").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(saved.id, ThemeEngine.activeThemeId(context))
            assertEquals(customJson, ThemeEngine.export(ThemeEngine.loadCustom(context)))
            assertEquals(savedJson, ThemeEngine.export(requireNotNull(ThemeEngine.loadTheme(context, saved.id))))
        }
        compose.onNodeWithText("Pondly regression").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Advanced theme editor").performScrollTo().performClick()
        compose.onNodeWithText("Enable custom keyboard backgrounds").assertIsDisplayed()
        compose.onNodeWithText("Suggestion bar").performScrollTo().performClick()
        compose.onNodeWithText("Current / unfinished word chip").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Current-word background", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Current-word highlight", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Key surface gradient").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Border gradient").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithText("Gif")[0].performScrollTo().performClick()
        compose.onNodeWithText("Choose media").performScrollTo().assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(BackdropMode.GIF, BackdropThemePreferences.load(context).mode)
            assertEquals(CUSTOM_THEME_ID, ThemeEngine.activeThemeId(context))
            assertEquals(savedJson, ThemeEngine.export(requireNotNull(ThemeEngine.loadTheme(context, saved.id))))
            assertEquals(saved.keys, ThemeEngine.loadCustom(context).keys)
        }
    }

    @Test
    fun savedThemeEditButtonOpensGradientEditor() {
        val context =
            androidx.test.core.app.ApplicationProvider
                .getApplicationContext<Application>()
        KeywiAppearancePreferences.save(context, true)
        ThemeEngine.ensureMigrated(context)
        val saved = ThemeEngine.saveCustomAs(context, "Saved editor regression")
        val savedJson = ThemeEngine.export(saved)
        compose.setContent {
            MaterialTheme {
                GemSettingsTheme(true) {
                    val navController = rememberNavController()
                    NavHost(navController, startDestination = "manager") {
                        composable("manager") { ThemeManagerScreen(navController) }
                        composable("advancedLookAndFeelEditor") { AdvancedLookAndFeelScreen(navController) }
                    }
                }
            }
        }
        compose.onNodeWithText("Edit").performScrollTo().performClick()
        compose.onNodeWithText("Enable custom keyboard backgrounds").assertIsDisplayed()
        compose.onAllNodesWithText("Edit gradient  ▼")[0].performScrollTo().performClick()
        compose.onNodeWithText("color stops", substring = true).performScrollTo().performClick()
        compose
            .onNodeWithText("＋ Add stop")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        compose.runOnIdle {
            assertEquals(CUSTOM_THEME_ID, ThemeEngine.activeThemeId(context))
            assertEquals(saved.backdrop.stops.size + 1, BackdropThemePreferences.load(context).stops.size)
            assertEquals(savedJson, ThemeEngine.export(requireNotNull(ThemeEngine.loadTheme(context, saved.id))))
        }
    }
    @Test
    fun selectingThemeIsNonDestructiveUntilApplyAndCustomScratchSurvivesSwitching() {
        val context =
            androidx.test.core.app.ApplicationProvider
                .getApplicationContext<Application>()
        KeywiAppearancePreferences.save(context, true)
        ThemeEngine.ensureMigrated(context)
        val saved = ThemeEngine.saveCustomAs(context, "Chromatic safe switch")
        val savedJson = ThemeEngine.export(requireNotNull(ThemeEngine.loadTheme(context, saved.id)))
        ThemeEngine.beginEditing(context, saved)
        KeyThemePreferences.save(context, KeyThemePreferences.load(context).copy(borderPhaseStepDegrees = 137f))

        compose.setContent {
            MaterialTheme {
                GemSettingsTheme(true) {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        ThemeManagerSection(onThemeApplied = {}, onEditTheme = {})
                    }
                }
            }
        }
        compose.onNodeWithText(saved.name).performScrollTo().performClick()
        compose.onNodeWithText("Apply selected theme").performScrollTo().assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(CUSTOM_THEME_ID, ThemeEngine.activeThemeId(context))
            assertEquals(137f, KeyThemePreferences.load(context).borderPhaseStepDegrees, .001f)
        }
        compose.onNodeWithText("Cancel selection").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(CUSTOM_THEME_ID, ThemeEngine.activeThemeId(context)) }

        compose.onNodeWithText(saved.name).performScrollTo().performClick()
        compose.onNodeWithText("Apply selected theme").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(saved.id, ThemeEngine.activeThemeId(context))
            assertEquals(137f, ThemeEngine.loadCustom(context).keys.borderPhaseStepDegrees, .001f)
            assertEquals(savedJson, ThemeEngine.export(requireNotNull(ThemeEngine.loadTheme(context, saved.id))))
        }

        compose.onNodeWithText("Custom").performScrollTo().performClick()
        compose.onNodeWithText("Apply selected theme").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(CUSTOM_THEME_ID, ThemeEngine.activeThemeId(context))
            assertEquals(137f, KeyThemePreferences.load(context).borderPhaseStepDegrees, .001f)
        }
    }

    @Test
    fun refreshingThemeManagerKeepsStagedSelection() {
        val context =
            androidx.test.core.app.ApplicationProvider
                .getApplicationContext<Application>()
        ThemeEngine.ensureMigrated(context)
        ThemeEngine.resetCustom(context)
        val saved = ThemeEngine.builtInThemes().first()
        val revision = mutableIntStateOf(0)

        compose.setContent {
            MaterialTheme {
                GemSettingsTheme(true) {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        ThemeManagerSection(
                            onThemeApplied = {},
                            onEditTheme = {},
                            refreshRevision = revision.intValue,
                        )
                    }
                }
            }
        }
        compose.onNodeWithText(saved.name).performScrollTo().performClick()
        compose.onNodeWithText("Apply selected theme").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { revision.intValue++ }
        compose.onNodeWithText("Apply selected theme").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { assertEquals(CUSTOM_THEME_ID, ThemeEngine.activeThemeId(context)) }
    }

    @Test
    fun resettingCustomRequiresExplicitConfirmation() {
        val context =
            androidx.test.core.app.ApplicationProvider
                .getApplicationContext<Application>()
        ThemeEngine.ensureMigrated(context)
        ThemeEngine.resetCustom(context)
        KeyThemePreferences.save(context, KeyThemePreferences.load(context).copy(borderPhaseStepDegrees = 193f))
        compose.setContent {
            MaterialTheme {
                GemSettingsTheme(true) {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        ThemeManagerSection(onThemeApplied = {}, onEditTheme = {})
                    }
                }
            }
        }
        compose.onNodeWithText("Reset Custom").performScrollTo().performClick()
        compose.onNodeWithText("Keep Custom").assertIsDisplayed().performClick()
        compose.runOnIdle {
            assertEquals(193f, KeyThemePreferences.load(context).borderPhaseStepDegrees, .001f)
        }
    }

}
