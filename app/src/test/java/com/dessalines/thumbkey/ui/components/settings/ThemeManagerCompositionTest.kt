package com.dessalines.thumbkey.ui.components.settings

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dessalines.thumbkey.ui.components.keyboard.CUSTOM_THEME_ID
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
        compose.onNodeWithText("Keyboard tuning").performScrollTo().assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(saved.id, ThemeEngine.activeThemeId(context))
            assertEquals(customJson, ThemeEngine.export(ThemeEngine.loadCustom(context)))
            assertEquals(savedJson, ThemeEngine.export(requireNotNull(ThemeEngine.loadTheme(context, saved.id))))
        }
        compose.onNodeWithText("Theme: Pondly regression  ▾").performScrollTo().performClick()
        compose.onNodeWithText("Pondly regression").assertIsDisplayed().performClick()
        compose.onNodeWithText("Edit active theme").performScrollTo().performClick()
        compose.onNodeWithText("Enable custom keyboard backgrounds").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(CUSTOM_THEME_ID, ThemeEngine.activeThemeId(context))
            assertEquals(savedJson, ThemeEngine.export(requireNotNull(ThemeEngine.loadTheme(context, saved.id))))
            assertEquals(saved.backdrop, ThemeEngine.loadCustom(context).backdrop)
            assertEquals(saved.keys, ThemeEngine.loadCustom(context).keys)
        }
    }
}
