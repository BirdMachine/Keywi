package com.dessalines.thumbkey.ui.components.settings

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.robolectric.RuntimeEnvironment
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class GemGlassSliderTest {
    @get:Rule val compose = createComposeRule()

    @Test fun darkGlassRetainsAccessibleAdjustment() = render(true)

    @Test fun lightGlassRetainsAccessibleAdjustment() = render(false)

    @Test fun stippledRailRetainsAccessibleAdjustment() = render(true, GlassRailPattern.DOTS)

    @Test fun meshedRailRetainsAccessibleAdjustment() = render(false, GlassRailPattern.MESH)

    @Test fun fineStippleRetainsAccessibleAdjustment() = render(true, GlassRailPattern.DOTS, GlassRailDensity.FINE)

    @Test fun railPatternPreferencePersistsAndFallsBack() {
        val context = RuntimeEnvironment.getApplication()
        try {
            GlassRailPreferences.save(context, GlassRailPattern.DIAGONAL)
            assertEquals(GlassRailPattern.DIAGONAL, GlassRailPreferences.load(context))
            context.getSharedPreferences("keywi_control_appearance", 0).edit()
                .putString("glass_rail_pattern", "unknown").commit()
            assertEquals(GlassRailPattern.PLAIN, GlassRailPreferences.load(context))
        } finally {
            GlassRailPreferences.save(context, GlassRailPattern.PLAIN)
        }
    }

    @Test fun railDensityPreferencePersistsAndFallsBack() {
        val context = RuntimeEnvironment.getApplication()
        try {
            GlassRailPreferences.saveDensity(context, GlassRailDensity.FINE)
            assertEquals(GlassRailDensity.FINE, GlassRailPreferences.loadDensity(context))
            context.getSharedPreferences("keywi_control_appearance", 0).edit()
                .putString("glass_rail_density", "unknown").commit()
            assertEquals(GlassRailDensity.BALANCED, GlassRailPreferences.loadDensity(context))
        } finally {
            GlassRailPreferences.saveDensity(context, GlassRailDensity.BALANCED)
        }
    }

    private fun render(
        dark: Boolean,
        pattern: GlassRailPattern = GlassRailPattern.PLAIN,
        density: GlassRailDensity = GlassRailDensity.BALANCED,
    ) {
        var changed = 0f
        compose.setContent {
            MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                Box(Modifier.background(MaterialTheme.colorScheme.background).padding(24.dp).testTag("preview")) {
                    GemGlassSlider(.6f, { changed = it }, Modifier.width(280.dp).testTag("slider"), railPattern = pattern, railDensity = density)
                }
            }
        }
        val image = compose.onNodeWithTag("preview").captureToImage().asAndroidBitmap()
        val output = File("build/outputs/tool-page-review/glass-slider-${if (dark) "dark" else "light"}-${pattern.name.lowercase()}${if (density == GlassRailDensity.BALANCED) "" else "-${density.name.lowercase()}"}.png")
        output.parentFile.mkdirs()
        output.outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
        compose.onNodeWithTag("slider").performSemanticsAction(SemanticsActions.SetProgress) { it(.75f) }
        compose.runOnIdle { assertEquals(.75f, changed, .001f) }
    }
}
