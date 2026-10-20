package com.dessalines.thumbkey.ui.components.settings

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GemSettingsIsolationTest {
    @Test
    fun matrixAndSetupKeepTheirOriginalTheme() {
        assertFalse(usesGemSettingsTheme("advancedKeyWordSelection"))
        assertFalse(usesGemSettingsTheme("setup"))
        assertFalse(usesGemSettingsTheme(null))
    }

    @Test
    fun settingsDestinationsShareGemChrome() {
        listOf(
            "settings",
            "gemCuts",
            "advancedBoards",
            "advancedLookAndFeel",
            "advancedLookAndFeelEditor",
            "lookAndFeel",
            "behavior",
            "clipboardSettings",
            "advancedInput",
            "contextEngine",
            "advancedCharacters",
            "advancedPowerOptions",
            "advancedDebug",
            "modifyKeys",
            "about",
            "backupAndRestore",
            "otherSettings",
            "typingOverlay",
        ).forEach { assertTrue(usesGemSettingsTheme(it)) }
    }
}
