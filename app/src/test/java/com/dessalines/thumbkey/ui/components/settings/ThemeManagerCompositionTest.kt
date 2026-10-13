package com.dessalines.thumbkey.ui.components.settings

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.dessalines.thumbkey.ui.components.keyboard.ThemeEngine
import com.dessalines.thumbkey.ui.components.settings.lookandfeel.AdvancedLookAndFeelScreen
import com.dessalines.thumbkey.ui.components.settings.lookandfeel.ThemeManagerScreen
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
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<Application>()
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
        compose.onNodeWithText("Theme: Pondly regression  ▾").performScrollTo().performClick()
        compose.onNodeWithText("Pondly regression").assertIsDisplayed().performClick()
        compose.onNodeWithText("Advanced theme editor").performScrollTo().performClick()
        compose.onNodeWithText("Enable custom keyboard backgrounds").assertIsDisplayed()
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
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<Application>()
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
        compose.onNodeWithText("＋ Add stop").performScrollTo().assertIsDisplayed().performClick()
        compose.runOnIdle {
            assertEquals(CUSTOM_THEME_ID, ThemeEngine.activeThemeId(context))
            assertEquals(saved.backdrop.stops.size + 1, BackdropThemePreferences.load(context).stops.size)
            assertEquals(savedJson, ThemeEngine.export(requireNotNull(ThemeEngine.loadTheme(context, saved.id))))
        }
    }

}
