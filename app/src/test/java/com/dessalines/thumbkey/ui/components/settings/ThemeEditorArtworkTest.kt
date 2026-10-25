package com.dessalines.thumbkey.ui.components.settings

import android.app.Application
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.compose.rememberNavController
import com.dessalines.thumbkey.ui.components.keyboard.BackdropPreset
import com.dessalines.thumbkey.ui.components.keyboard.BackdropThemePreferences
import com.dessalines.thumbkey.ui.components.keyboard.KeywiAppearancePreferences
import com.dessalines.thumbkey.ui.components.keyboard.KeywiPowerMode
import com.dessalines.thumbkey.ui.components.keyboard.KeywiPowerPreferences
import com.dessalines.thumbkey.ui.components.settings.lookandfeel.AdvancedLookAndFeelScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h891dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ThemeEditorArtworkTest {
    @get:Rule val compose = createComposeRule()
    @Test fun editorKeepsControlsOverActiveArtworkAndLinksToWorkingEffects() {
        val context = RuntimeEnvironment.getApplication()
        KeywiAppearancePreferences.save(context, true)
        KeywiPowerPreferences.save(context, KeywiPowerMode.CONSERVED)
        BackdropThemePreferences.save(context, BackdropThemePreferences.stateForPreset(BackdropPreset.BIRDIE_RAINBOW))
        compose.setContent {
            val nav = rememberNavController()
            GemSettingsTheme(enabled = true, dripEnabled = true) { AdvancedLookAndFeelScreen(nav) }
        }
        compose.onNodeWithText("Save").assertExists()
        compose.onNodeWithText("Revert").assertExists()
        val file = File("build/outputs/color-review/theme-editor-followup.png")
        file.parentFile.mkdirs()
        file.outputStream().use { compose.onRoot().captureToImage().asAndroidBitmap().compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        compose.onNodeWithText("Key flashes, drag trails & GIF / PNG effects").performScrollTo().assertExists()
    }
}
