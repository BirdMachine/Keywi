package com.dessalines.thumbkey.ui.components.settings

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.effects.ActionSoundConfig
import com.dessalines.thumbkey.effects.ActionSoundPreferences
import com.dessalines.thumbkey.ui.components.settings.advancedsound.AdvancedSoundScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SoundSectionsTest {
    @get:Rule val compose = createComposeRule()

    @Test fun activeInstrumentOpensAndLibraryCanBeExpandedSeparately() {
        ActionSoundPreferences.save(RuntimeEnvironment.getApplication(), ActionSoundConfig(toneEnabled = true))
        compose.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Box(Modifier.width(380.dp).height(1000.dp).background(MaterialTheme.colorScheme.background).testTag("sound-page")) {
                    AdvancedSoundScreen(Modifier.verticalScroll(rememberScrollState()))
                }
            }
        }
        compose.onNodeWithText("Preview a note").assertExists()
        compose.onNodeWithText("Import files").assertDoesNotExist()
        val output = File("build/outputs/tool-page-review/sound-sections.png")
        output.parentFile.mkdirs()
        output.outputStream().use {
            compose.onNodeWithTag("sound-page").captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        compose.onNodeWithText("Sound library").performClick()
        compose.onNodeWithText("Import files").assertExists()
        compose.onNodeWithText("Sound library").performClick()
        compose.onNodeWithText("Import files").assertDoesNotExist()
        compose.onNodeWithText("Preview a note").assertExists()
    }
}
