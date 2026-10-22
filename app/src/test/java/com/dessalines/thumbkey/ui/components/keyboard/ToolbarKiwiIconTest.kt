package com.dessalines.thumbkey.ui.components.keyboard

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ToolbarKiwiIconTest {
    @get:Rule val compose = createComposeRule()

    @Test fun suppliedVariantsKeepTheirGreenCentersAndDistinctBodies() {
        compose.setContent {
            Row(Modifier.background(Color.Gray).testTag("sheet")) {
                Box(Modifier.size(64.dp)) {
                    ToolbarKiwiIcon(false, Modifier.size(64.dp).testTag("dark"))
                }
                Box(Modifier.size(64.dp)) {
                    ToolbarKiwiIcon(true, Modifier.size(64.dp).testTag("light"))
                }
            }
        }
        val bodyBrightness =
            listOf("dark", "light").map { tag ->
                val bitmap = compose.onNodeWithTag(tag).captureToImage().asAndroidBitmap()
                val center = bitmap.getPixel(bitmap.width / 2, bitmap.height / 2)
                assertTrue("Kiwi center remains green", android.graphics.Color.green(center) > android.graphics.Color.red(center))
                val body = bitmap.getPixel(bitmap.width / 4, (bitmap.height * .65f).toInt())
                android.graphics.Color.red(body) + android.graphics.Color.green(body) + android.graphics.Color.blue(body)
            }
        assertTrue("Light and dark supplied artwork remain distinct", bodyBrightness[1] > bodyBrightness[0] + 200)
        val file = File("build/outputs/toolbar-review/kiwi-icons.png")
        file.parentFile!!.mkdirs()
        file.outputStream().use {
            compose
                .onNodeWithTag("sheet")
                .captureToImage()
                .asAndroidBitmap()
                .compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
    }


    @Test fun launcherUsesTransparentArtworkWithoutAnAdaptiveBackgroundLayer() {
        val context = RuntimeEnvironment.getApplication<Application>()
        val icon = context.packageManager.getApplicationIcon(context.applicationInfo)
        assertFalse("Use the transparent artwork directly", icon is android.graphics.drawable.AdaptiveIconDrawable)
        val bitmap = android.graphics.BitmapFactory.decodeResource(
            context.resources,
            com.dessalines.thumbkey.R.drawable.keywi_launcher_foreground,
            android.graphics.BitmapFactory.Options().apply { inScaled = false },
        )
        assertTrue("Launcher artwork retains alpha", bitmap.hasAlpha())
        listOf(0 to 0, bitmap.width - 1 to 0, 0 to bitmap.height - 1, bitmap.width - 1 to bitmap.height - 1).forEach { (x, y) ->
            assertTrue("Launcher corners are transparent", android.graphics.Color.alpha(bitmap.getPixel(x, y)) == 0)
        }
        bitmap.recycle()
    }

    @Test fun automaticContrastAndManualVariantsAreAvailable() {
        assertTrue(toolbarIconIsLight(ToolbarIconStyle.AUTOMATIC, Color.Black))
        assertFalse(toolbarIconIsLight(ToolbarIconStyle.AUTOMATIC, Color.White))
        assertTrue(toolbarIconIsLight(ToolbarIconStyle.LIGHT, Color.White))
        assertFalse(toolbarIconIsLight(ToolbarIconStyle.DARK, Color.Black))
    }
}
