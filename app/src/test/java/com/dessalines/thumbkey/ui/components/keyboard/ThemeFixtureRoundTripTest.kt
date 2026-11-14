package com.dessalines.thumbkey.ui.components.keyboard

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipInputStream

/** Opt-in check for a real exported package; the user's media stays outside Git. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ThemeFixtureRoundTripTest {
    @Test
    fun realPackageSurvivesSaveExportAndImportWithoutOriginalStorage() {
        val fixturePath = System.getenv("KEYWI_THEME_FIXTURE")
        assumeTrue("Set KEYWI_THEME_FIXTURE to an exported theme ZIP", !fixturePath.isNullOrBlank())
        val fixture = File(requireNotNull(fixturePath))
        assertTrue("Requested theme fixture must exist", fixture.isFile)
        val originalAssets = linkedMapOf<String, ByteArray>()
        ZipInputStream(fixture.inputStream()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (entry.name.startsWith("assets/") && !entry.isDirectory) originalAssets[entry.name] = zip.readBytes()
            }
        }
        val context = ApplicationProvider.getApplicationContext<Application>()
        ThemeEngine.ensureMigrated(context)
        val feedback = AccentFeedbackPreferences.get(context)
        val imported = fixture.inputStream().use { ThemeEngine.importPackage(context, it).getOrThrow() }
        assertTrue(ThemeEngine.missingAssets(context, imported).isEmpty())
        val saved = ThemeEngine.saveCustomAs(context, imported.name)
        val output = ByteArrayOutputStream()
        ThemeEngine.exportPackage(context, saved, output)
        val bytes = output.toByteArray()
        val outputPath = System.getenv("KEYWI_THEME_ROUNDTRIP_OUTPUT")
        if (!outputPath.isNullOrBlank()) File(outputPath).writeBytes(bytes)

        // Remove every original app-owned theme asset before reinstalling the export.
        File(context.filesDir, "themes").deleteRecursively()
        context.getSharedPreferences("keywi_theme_engine", 0).edit().clear().commit()
        val restored = ThemeEngine.importPackage(context, ByteArrayInputStream(bytes)).getOrThrow()
        assertEquals(saved.copy(id = CUSTOM_THEME_ID), restored)
        assertEquals(CUSTOM_THEME_ID, ThemeEngine.activeThemeId(context))
        assertTrue(ThemeEngine.missingAssets(context, restored).isEmpty())
        assertEquals(feedback, AccentFeedbackPreferences.get(context))
        originalAssets.forEach { (reference, expected) ->
            val actual = ThemeAssetStore.assetFile(ThemeAssetStore.directory(context, CUSTOM_THEME_ID), reference)
            assertArrayEquals("Embedded asset must survive byte-for-byte: $reference", expected, actual.readBytes())
        }
    }
}
