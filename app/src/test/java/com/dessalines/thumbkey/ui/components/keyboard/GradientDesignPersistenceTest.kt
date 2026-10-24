package com.dessalines.thumbkey.ui.components.keyboard

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class GradientDesignPersistenceTest {
    @Test fun preferencesAndThemeExportKeepDistributionGroupsAndIndividualDesigns() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val keys =
            KeyThemeState(
                borderDistribution = GradientDistribution.INCREMENTAL,
                borderPhaseStepDegrees = -47f,
                borderRandomSeed = 701,
                keyOverrides = mapOf("0:1" to KeyDesignOverride(surfaceColor = Color.Red, borderGradient = BIRDIE_GOLD_BORDER)),
                keyGroups =
                    listOf(
                        KeyDesignGroup(
                            "Navigation",
                            setOf("1:0", "1:1"),
                            KeyDesignOverride(borderColor = Color.Cyan, surfaceGradient = BIRDIE_KEY_GRADIENT),
                            sharedGradientCanvas = true,
                        ),
                    ),
            )
        KeyThemePreferences.save(context, keys)
        assertEquals(keys, KeyThemePreferences.load(context))
        val source = ThemeEngine.captureCurrent(context, "Distribution test", CUSTOM_THEME_ID).copy(keys = keys)
        val restored = ThemeEngine.importIntoCustom(context, ThemeEngine.export(source)).getOrThrow()
        assertEquals(keys, restored.keys)
        assertEquals(keys, KeyThemePreferences.load(context))
    }

    @Test fun oldGroupDocumentsKeepIndependentCanvases() {
        val legacy = decodeGroups("""[{"id":"Legacy","keys":["0:0"],"design":{}}]""").single()
        assertEquals(false, legacy.sharedGradientCanvas)
    }

    @Test fun corruptOptionalDesignPreferencesFallBackWithoutLosingBaseStyle() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        context
            .getSharedPreferences("key_theme_preferences", 0)
            .edit()
            .clear()
            .putString("surface_style", "SOLID")
            .putString("border_distribution", "FUTURE_UNKNOWN")
            .putString("key_groups", "broken")
            .putString("key_overrides", "broken")
            .commit()
        val keys = KeyThemePreferences.load(context)
        assertEquals(KeySurfaceStyle.SOLID, keys.surfaceStyle)
        assertEquals(GradientDistribution.SAME, keys.borderDistribution)
        assertEquals(emptyList<KeyDesignGroup>(), keys.keyGroups)
        assertEquals(emptyMap<String, KeyDesignOverride>(), keys.keyOverrides)
    }
}
