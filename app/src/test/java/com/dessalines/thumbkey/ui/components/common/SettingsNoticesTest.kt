package com.dessalines.thumbkey.ui.components.common

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.dessalines.thumbkey.ui.components.settings.GemSettingsTheme
import org.junit.Assert.assertEquals
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
class SettingsNoticesTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun updateNoticesAppearInSequenceAndBothBannersReopenThem() {
        val viewed = mutableStateOf<Int?>(10)
        var acknowledgments = 0
        compose.setContent {
            MaterialTheme {
                GemSettingsTheme(true) {
                    SettingsNoticeBanners(11, viewed.value) {
                        acknowledgments++
                        viewed.value = 11
                    }
                }
            }
        }
        // Both notices remain available as banners, but neither auto-launches.
        assertEquals(0, compose.onAllNodesWithText("Done").fetchSemanticsNodes().size)
        compose.onNodeWithText("Welcome to Keywi").performClick()
        compose.onNodeWithText("BUILD 11 · WHAT'S NEW").assertIsDisplayed()
        compose.onNodeWithText("Done").performClick()
        compose.runOnIdle {
            assertEquals(1, acknowledgments)
            assertEquals(11, viewed.value)
        }
        // Closing the welcome does not automatically open the policy.
        assertEquals(0, compose.onAllNodesWithText("Done").fetchSemanticsNodes().size)
        compose.onNodeWithText("Android installation policy").performClick()
        compose.onNodeWithText("GOOGLE POLICY · INFORMATION").assertIsDisplayed()
        compose.onNodeWithText("Done").performClick()
        val context = ApplicationProvider.getApplicationContext<Application>()
        assertEquals(11, context.getSharedPreferences("keywi_notices", 0).getInt("policy_seen_version", -1))
    }

    @Test
    fun noticesWaitForSettingsAndReadingThemPersistsForTheCurrentBuild() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        context
            .getSharedPreferences("keywi_notices", 0)
            .edit()
            .putInt("policy_seen_version", 12)
            .apply()
        val viewed = mutableStateOf<Int?>(null)
        compose.setContent { MaterialTheme { SettingsNoticeBanners(12, viewed.value) {} } }
        compose.runOnIdle { viewed.value = 12 }
        compose.onNodeWithText("Welcome to Keywi").assertIsDisplayed()
        assertEquals(0, compose.onAllNodesWithText("Done").fetchSemanticsNodes().size)
        compose.onNodeWithText("Android installation policy").performClick()
        compose.onNodeWithText("GOOGLE POLICY · INFORMATION").assertIsDisplayed()
    }

    @Test
    fun upstreamLinksAreExplicitAndOwnNotesArePackaged() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val notes =
            context.assets
                .open("KEYWI_RELEASES.md")
                .bufferedReader()
                .use { it.readText() }
        assertTrue(notes.contains("What's changed"))
        assertTrue(notes.contains("Keywi"))
        assertTrue(UPSTREAM_MARKDOWN.contains("NOT Keywi"))
        assertTrue(UPSTREAM_MARKDOWN.contains(THUMBKEY_GITHUB_URL))
        assertTrue(UPSTREAM_MARKDOWN.contains(THUMBKEY_PATREON_URL))
        assertTrue(UPSTREAM_MARKDOWN.contains(THUMBKEY_LIBERAPAY_URL))
    }
}
