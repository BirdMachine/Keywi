package com.dessalines.thumbkey.ui.components.keyboard

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.utils.KeyAction
import com.dessalines.thumbkey.utils.KeyC
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
class KeyGlyphColorRenderTest {
    @get:Rule val compose = createComposeRule()

    @Test fun explicitGlyphColorReachesTheRenderedLegend() {
        compose.setContent {
            MaterialTheme {
                CompositionLocalProvider(LocalKeyGlyphColor provides Color.Green) {
                    Box(Modifier.size(80.dp).background(Color.Black).testTag("glyph")) {
                        KeyText(KeyC(KeyAction.CommitText("A")), 100.dp, false, false, false)
                    }
                }
            }
        }
        val image = compose.onNodeWithTag("glyph").captureToImage().asAndroidBitmap()
        var green = 0
        for (y in 0 until image.height) for (x in 0 until image.width) {
            val c = image.getPixel(x, y)
            if (android.graphics.Color.green(c) > 150 && android.graphics.Color.red(c) < 30) green++
        }
        assertTrue("Legend must use the explicit glyph color", green > 10)
    }
}
