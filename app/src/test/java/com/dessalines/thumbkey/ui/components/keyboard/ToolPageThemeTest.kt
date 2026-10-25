package com.dessalines.thumbkey.ui.components.keyboard

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ToolPageThemeTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = RuntimeEnvironment.getApplication()

    @Test fun themeFillStaysSubtleInBothModesAndRespectsBorderNone() {
        val vivid = KeyThemeState(surfaceStyle = KeySurfaceStyle.SOLID, surfaceColor = Color.Red, borderStyle = KeyBorderStyle.SOLID, borderColor = Color.Cyan)
        val dark = ToolPageStyle(true)
        val light = ToolPageStyle(false)
        assertEquals(Color.Cyan, toolEdge(vivid)!!.stops.first().color)
        assertNull(toolEdge(vivid.copy(borderStyle = KeyBorderStyle.NONE)))
        assertTrue(toolFill(vivid, dark).stops.first().color.luminance() < .2f)
        assertTrue(toolFill(vivid, light).stops.first().color.luminance() > .5f)
        assertEquals(1f, toolFill(vivid, dark).stops.first().color.alpha)
        val gradient = vivid.copy(surfaceStyle = KeySurfaceStyle.GRADIENT, surfaceGradient = BIRDIE_RAINBOW_BACKDROP)
        assertEquals(gradient.surfaceGradient.stops.map { it.position }, toolFill(gradient, dark).stops.map { it.position })
        assertEquals(gradient.surfaceGradient.angleDegrees, toolFill(gradient, dark).angleDegrees)
    }

    @Test fun darkPageHasOpaqueBackgroundAndSelectedStyleStillApplies() = render(true)

    @Test fun lightPageHasOpaqueBackgroundAndSelectedStyleStillApplies() = render(false)

    private fun render(dark: Boolean) {
        KeyThemePreferences.save(context, KeyThemeState(borderGradient = BIRDIE_RAINBOW_BACKDROP))
        var applied = ""
        compose.setContent {
            MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                ToolPageTheme {
                    Column(Modifier.size(360.dp, 640.dp).toolPageBackground().testTag("page")) {
                        FancyTextWorkspace(onApply = { applied = it }, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
        compose.onNodeWithText("UPPERCASE").performClick().assertIsSelected()
        compose.runOnIdle { assertTrue(applied.isNotEmpty()) }
        val bitmap = compose.onNodeWithTag("page").captureToImage().asAndroidBitmap()
        val corner = bitmap.getPixel(0, bitmap.height - 1)
        assertEquals(255, android.graphics.Color.alpha(corner))
        assertTrue(if (dark) android.graphics.Color.red(corner) < 100 else android.graphics.Color.red(corner) > 180)
        val file = File("build/outputs/tool-page-review/${if (dark) "dark" else "light"}.png")
        file.parentFile!!.mkdirs()
        file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
    }
}
