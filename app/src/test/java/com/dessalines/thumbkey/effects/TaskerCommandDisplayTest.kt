package com.dessalines.thumbkey.effects

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.ui.components.keyboard.TaskerCommandLegend
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
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
class TaskerCommandDisplayTest {
    @get:Rule val compose = createComposeRule()
    private val context: Application get() = RuntimeEnvironment.getApplication()
    private val task = TaskerTask("Example task", "net.dinglisch.android.taskerm")

    @Before
    fun reset() {
        context
            .getSharedPreferences("keywi_tasker_keys", 0)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun customLegendsRenderAndClearWithoutChangingTaskOrInputMode() {
        val binding = TaskerKeyBinding(task, TaskerBindingMode.SECONDARY, TaskerCommandDisplay(TaskerCommandDisplayKind.TEXT, "⚙︎"))
        TaskerKeyBindings.save(context, "key0_0", "TOP", binding)
        compose.setContent {
            MaterialTheme {
                TaskerCommandLegend("key0_0", "TOP", 24.dp) { Text("Original") }
            }
        }
        compose.onNodeWithText("⚙︎").assertExists()
        compose.onNodeWithText("Original").assertDoesNotExist()
        compose.runOnIdle {
            TaskerKeyBindings.save(
                context,
                "key0_0",
                "TOP",
                binding.copy(display = TaskerCommandDisplay(TaskerCommandDisplayKind.ICON, "settings")),
            )
        }
        compose.onNodeWithContentDescription("Tasker Gear symbol").assertExists()
        val rendered = compose.onNodeWithContentDescription("Tasker Gear symbol").captureToImage().asAndroidBitmap()
        val background = rendered.getPixel(0, 0)
        assertTrue((0 until rendered.width).any { x -> (0 until rendered.height).any { y -> rendered.getPixel(x, y) != background } })

        assertEquals(task, TaskerKeyBindings.get(context, "key0_0", "TOP")?.task)
        assertEquals(TaskerBindingMode.SECONDARY, TaskerKeyBindings.get(context, "key0_0", "TOP")?.mode)
        compose.runOnIdle { TaskerKeyBindings.save(context, "key0_0", "TOP", binding.copy(display = null)) }
        compose.onNodeWithText("Original").assertExists()
        compose.onNodeWithContentDescription("Tasker Gear symbol").assertDoesNotExist()
    }

    @Test
    fun imagesAreCopiedSampledPersistedAndCleanedUp() =
        runBlocking {
            val source = File(context.cacheDir, "command-source.png")
            val bitmap = Bitmap.createBitmap(2048, 1024, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.MAGENTA) }
            source.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
            val display = TaskerCommandImages.importImage(context, Uri.fromFile(source))
            assertTrue(source.delete())
            TaskerKeyBindings.save(context, "hk1_1", "HOLD", TaskerKeyBinding(task, display = display))
            val restored = TaskerKeyBindings.get(context, "hk1_1", "HOLD")?.display
            assertEquals(display, restored)
            val decoded = TaskerCommandImages.load(context, display.value)
            assertNotNull(decoded)
            assertTrue(decoded!!.width <= 256 && decoded.height <= 256)
            assertEquals(Color.MAGENTA, decoded.getPixel(0, 0))
            decoded.recycle()
            val stored = TaskerCommandImages.file(context, display.value)!!
            assertTrue(stored.isFile)
            TaskerKeyBindings.save(context, "hk1_1", "HOLD", TaskerKeyBinding(task))
            assertFalse(stored.exists())
        }

    @Test
    fun missingOrInvalidArtworkFallsBackAndOldBindingsRemainCompatible() {
        context
            .getSharedPreferences("keywi_tasker_keys", 0)
            .edit()
            .putString(
                "hk0_0:CENTER",
                """{"name":"Example task","package":"net.dinglisch.android.taskerm","mode":"REPLACE"}""",
            ).commit()
        assertNull(TaskerKeyBindings.get(context, "hk0_0", "CENTER")?.display)
        assertFalse(TaskerCommandDisplay(TaskerCommandDisplayKind.IMAGE, "../../outside.png").valid())
        assertNull(TaskerCommandImages.file(context, "../../outside.png"))
        val missing = TaskerCommandDisplay(TaskerCommandDisplayKind.IMAGE, "00000000-0000-0000-0000-000000000000.png")
        TaskerKeyBindings.save(context, "hk0_0", "CENTER", TaskerKeyBinding(task, display = missing))
        compose.setContent {
            MaterialTheme { TaskerCommandLegend("hk0_0", "CENTER", 24.dp) { Text("Original HK label") } }
        }
        compose.onNodeWithText("Original HK label").assertExists()
        assertFalse(TaskerCommandDisplay(TaskerCommandDisplayKind.TEXT, "a".repeat(17)).valid())
        assertTrue(TaskerCommandDisplay(TaskerCommandDisplayKind.TEXT, "🩷⚙").valid())
    }
}
