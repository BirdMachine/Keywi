package com.dessalines.thumbkey.ui.components.settings.modifykeys

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.MutableLiveData
import androidx.navigation.compose.rememberNavController
import com.dessalines.thumbkey.db.AppSettings
import com.dessalines.thumbkey.db.AppSettingsDao
import com.dessalines.thumbkey.db.AppSettingsRepository
import com.dessalines.thumbkey.db.AppSettingsViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.reflect.Proxy

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h891dp")
class KeyEditorSectionTest {
    @get:Rule val compose = createComposeRule()

    @Test fun realEditorsPreserveGestureKeyAndUnsavedValuesAcrossIndependentToggles() {
        val settings = MutableLiveData<AppSettings>()
        val dao = Proxy.newProxyInstance(AppSettingsDao::class.java.classLoader, arrayOf(AppSettingsDao::class.java)) { _, method, _ ->
            check(method.name == "getSettings") { "Unexpected database write: ${method.name}" }
            settings
        } as AppSettingsDao
        val viewModel = AppSettingsViewModel(AppSettingsRepository(dao))
        compose.setContent {
            MaterialTheme { AdvancedKeyWordSelectionScreen(rememberNavController(), viewModel) }
        }
        compose.onNodeWithText("Actions & values").performScrollTo().performClick()
        val actions = compose.onNodeWithTag("key-editor-header:Gesture Actions")
        val values = compose.onNodeWithTag("key-editor-header:Character Values")
        compose.onNodeWithText("↑ up").performScrollTo().performClick()
        actions.performScrollTo().performClick()
        actions.assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Collapsed"))
        values.performScrollTo().assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Expanded"))
        compose.onNodeWithText("center").performScrollTo().performTextReplacement("(｡•̀ᴗ-)✧")
        values.performScrollTo().performClick()
        compose.onNodeWithText("(｡•̀ᴗ-)✧").assertDoesNotExist()
        actions.performScrollTo().performClick()
        compose.onNodeWithText("↑ up").performScrollTo().assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))
        // Both selected-key labels still refer to the original key after expanding again.
        actions.performScrollTo().performClick()
        values.performScrollTo().performClick()
        compose.onNodeWithText("center").performScrollTo().assertTextContains("(｡•̀ᴗ-)✧")
        actions.performScrollTo().performClick()
        compose.onAllNodesWithText("key0_0")[0].assertExists()
        compose.onAllNodesWithText("key0_0")[1].assertExists()
        // The preview and its matrix chooser are outside both collapsed bodies.
        actions.performScrollTo().performClick()
        values.performScrollTo().performClick()
        compose.onNodeWithText("ME-Like matrix").performScrollTo().assertIsDisplayed()
    }
}
