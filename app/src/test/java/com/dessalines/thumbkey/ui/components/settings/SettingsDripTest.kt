package com.dessalines.thumbkey.ui.components.settings

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.compose.composable
import androidx.test.core.app.ApplicationProvider
import com.dessalines.thumbkey.ui.components.keyboard.KeywiPowerMode
import com.dessalines.thumbkey.ui.components.keyboard.KeywiPowerPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h891dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsDripTest {
    @get:Rule val compose = createComposeRule()

    @Test fun limitsOpticsAndStylingToAuthorizedScope() {
        listOf(
            "lookAndFeel",
            "behavior",
            "modifyKeys",
            "advancedKeyWordSelection",
            "setup",
            null,
        ).forEach {
            assertFalse(usesSettingsDrip(it))
        }
        assertTrue(usesSettingsDrip("advancedLookAndFeel"))
        assertTrue(usesSettingsDrip("settings"))
        assertTrue(usesSettingsDrip("advancedLookAndFeelEditor"))
        for (mode in KeywiPowerMode.entries) {
            for (api in listOf(24, 30, 31, 33, 34)) {
                assertEquals(mode == KeywiPowerMode.UNBRIDLED && api >= 31, settingsOpticsEnabled(mode, api))
            }
        }
    }

    @Test fun rendersThemeArtworkAndTracksPowerWithoutChangingKeyboardSettings() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("birdie_backdrop_theme", 0)
        val file = File(context.cacheDir, "drip-test.png")
        Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888).apply {
            eraseColor(android.graphics.Color.CYAN)
            file.outputStream().use { compress(Bitmap.CompressFormat.PNG, 100, it) }
            recycle()
        }
        prefs
            .edit()
            .putString("mode", "IMAGE")
            .putString(
                "media_uri",
                android.net.Uri
                    .fromFile(file)
                    .toString(),
            ).commit()
        KeywiPowerPreferences.save(context, KeywiPowerMode.CONSERVED)
        val before = prefs.all.toMap()
        compose.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                GemSettingsTheme(true, dripEnabled = true) {
                    Column {
                        SettingsThemeBanner()
                        GemNavigationPreference(
                            title = { Text("Themes & appearance") },
                            summary = { Text("Your active keyboard atmosphere") },
                            onClick = {},
                        )
                        Text("Power: ${LocalSettingsDrip.current?.power}")
                        Text("Art loaded: ${LocalSettingsDrip.current?.image != null}")
                    }
                }
            }
        }
        compose.waitUntil(10000) { compose.onAllNodesWithText("Art loaded: true").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Themes & appearance").assertIsDisplayed().performClick()
        compose.runOnIdle { KeywiPowerPreferences.save(context, KeywiPowerMode.UNBRIDLED) }
        compose.waitUntil(10000) { compose.onAllNodesWithText("Power: UNBRIDLED").fetchSemanticsNodes().isNotEmpty() }
        val output = File("build/outputs/settings-review/settings-drip.png").apply { parentFile.mkdirs() }
        compose.onRoot().captureToImage().asAndroidBitmap().let { bitmap ->
            output.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
        assertEquals(before, prefs.all)
    }

    @Test fun homeAndPowerPagesRenderAndNavigate() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database =
            androidx.room.Room
                .inMemoryDatabaseBuilder(
                    context,
                    com.dessalines.thumbkey.db.AppDB::class.java,
                ).allowMainThreadQueries()
                .build()
        val model =
            com.dessalines.thumbkey.db.AppSettingsViewModel(
                com.dessalines.thumbkey.db
                    .AppSettingsRepository(database.appSettingsDao()),
            )
        compose.setContent {
            val nav = androidx.navigation.compose.rememberNavController()
            MaterialTheme(colorScheme = darkColorScheme()) {
                androidx.navigation.compose.NavHost(navController = nav, startDestination = "settings") {
                    composable("settings") {
                        GemSettingsTheme(true, dripEnabled = true) { SettingsScreen(nav, model, true, true) }
                    }
                    composable("advancedPowerOptions") {
                        GemSettingsTheme(
                            true,
                            dripEnabled = true,
                        ) {
                            com.dessalines.thumbkey.ui.components.settings.power
                                .AdvancedPowerOptionsScreen(nav)
                        }
                    }
                }
            }
        }
        compose.onNodeWithText("Gem cuts").assertIsDisplayed()

        fun capture(name: String) {
            val output = File("build/outputs/settings-review/$name.png").apply { parentFile.mkdirs() }
            compose.onRoot().captureToImage().asAndroidBitmap().let { bitmap ->
                output.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            }
        }
        capture("home")
        compose.onNodeWithText("Power options").performScrollTo().performClick()
        compose.onNodeWithText("Conserved").assertIsDisplayed().performClick()
        capture("power")
        compose.onNodeWithText("Unbridled").performClick()
        compose.runOnIdle { assertEquals(KeywiPowerMode.UNBRIDLED, KeywiPowerPreferences.load(context)) }
        compose.onNodeWithText("Restricted").performClick()
        compose.runOnIdle { assertEquals(KeywiPowerMode.RESTRICTED, KeywiPowerPreferences.load(context)) }
    }
}
