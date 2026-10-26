package com.dessalines.thumbkey.ui.components.settings.advancedinput

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.dessalines.thumbkey.ui.components.keyboard.ToolPagePreferences
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w420dp-h1100dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ToolPageManagerTest {
    @get:Rule val compose = createComposeRule()

    @Test fun hidingFormattingUpdatesSavedSettingsAndRestoreRequiresConfirmation() {
        val context = RuntimeEnvironment.getApplication()
        compose.setContent { MaterialTheme(colorScheme = darkColorScheme()) { Surface { ToolPageManagerScreen() } } }
        compose.onNodeWithText("Page order & visibility").performClick()
        compose.onNodeWithContentDescription("Show Formatting").performScrollTo().performClick()
        compose.runOnIdle { assertTrue("FORMATTING" in ToolPagePreferences.load(context).hiddenPages) }
        val file = File("build/outputs/tool-page-review/tool-page-manager.png")
        file.parentFile!!.mkdirs()
        file.outputStream().use { compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
        compose.onNodeWithText("Restore page defaults").performScrollTo().performClick()
        compose.onNodeWithText("Cancel").performClick()
        compose.runOnIdle { assertTrue("FORMATTING" in ToolPagePreferences.load(context).hiddenPages) }
        compose.onNodeWithText("Restore page defaults").performScrollTo().performClick()
        compose.onNodeWithText("Restore", useUnmergedTree = true).performClick()
        compose.runOnIdle { assertTrue("FORMATTING" !in ToolPagePreferences.load(context).hiddenPages) }
    }
}
