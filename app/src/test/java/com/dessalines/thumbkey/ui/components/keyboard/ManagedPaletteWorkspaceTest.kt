package com.dessalines.thumbkey.ui.components.keyboard

import android.app.Application
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w420dp-h900dp")
class ManagedPaletteWorkspaceTest {
    @get:Rule val compose = createComposeRule()

    @Test fun visibleSnippetInsertsExactMultilineTextAndHiddenEntriesStayOut() {
        ToolPagePreferences.save(RuntimeEnvironment.getApplication(), ToolPageConfig(snippets = listOf(
            ManagedCollection("mine", "My snippets", entries = listOf(ManagedEntry("line 1\n  line 2", "Greeting"), ManagedEntry("hidden", enabled = false))),
        )))
        var committed = ""
        compose.setContent { MaterialTheme { Surface { Box(Modifier.size(380.dp, 600.dp)) { StringPalettesWorkspace { committed = it } } } } }
        compose.onNodeWithText("hidden").assertDoesNotExist()
        compose.onNodeWithText("line 1\n  line 2").performClick()
        compose.runOnIdle { assertEquals("line 1\n  line 2", committed) }
    }
}
