package com.dessalines.thumbkey.ui.components.settings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/** Optional settings-slider decoration, deliberately separate from exported keyboard themes. */
enum class GlassRailPattern(val label: String) {
    PLAIN("Plain"),
    TICKS("Ticks"),
    DOTS("Stipple"),
    MESH("Mesh"),
    DIAGONAL("Diagonal"),
}

/** Spacing of the decorative marks; never changes slider touch or progress semantics. */
enum class GlassRailDensity(val label: String, val spacingDp: Float) {
    FINE("Fine", 4f),
    BALANCED("Balanced", 6f),
    OPEN("Open", 10f),
}

/** Decorative contrast only; the underlying slider and progress semantics stay unchanged. */
enum class GlassRailIntensity(val label: String, val alphaMultiplier: Float) {
    SUBTLE("Subtle", .55f),
    BALANCED("Balanced", 1f),
    VIVID("Vivid", 1.45f),
}

object GlassRailPreferences {
    private const val PREFS = "keywi_control_appearance"
    private const val RAIL_PATTERN = "glass_rail_pattern"
    private const val RAIL_DENSITY = "glass_rail_density"
    private const val RAIL_INTENSITY = "glass_rail_intensity"

    var currentPattern by mutableStateOf(GlassRailPattern.PLAIN)
        private set

    var currentDensity by mutableStateOf(GlassRailDensity.BALANCED)
        private set

    var currentIntensity by mutableStateOf(GlassRailIntensity.BALANCED)
        private set

    fun loadIntensity(context: Context): GlassRailIntensity {
        val stored = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(RAIL_INTENSITY, GlassRailIntensity.BALANCED.name)
        currentIntensity = GlassRailIntensity.entries.firstOrNull { it.name == stored } ?: GlassRailIntensity.BALANCED
        return currentIntensity
    }

    fun saveIntensity(context: Context, intensity: GlassRailIntensity) {
        currentIntensity = intensity
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(RAIL_INTENSITY, intensity.name).apply()
    }

    fun load(context: Context): GlassRailPattern {
        val stored = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(RAIL_PATTERN, GlassRailPattern.PLAIN.name)
        currentPattern = GlassRailPattern.entries.firstOrNull { it.name == stored } ?: GlassRailPattern.PLAIN
        return currentPattern
    }

    fun save(context: Context, pattern: GlassRailPattern) {
        currentPattern = pattern
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(RAIL_PATTERN, pattern.name).apply()
    }
    fun loadDensity(context: Context): GlassRailDensity {
        val stored = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(RAIL_DENSITY, GlassRailDensity.BALANCED.name)
        currentDensity = GlassRailDensity.entries.firstOrNull { it.name == stored } ?: GlassRailDensity.BALANCED
        return currentDensity
    }

    fun saveDensity(context: Context, density: GlassRailDensity) {
        currentDensity = density
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(RAIL_DENSITY, density.name).apply()
    }
}

/** Load once per slider instance, then observe shared Compose state for live updates. */
@Composable
internal fun rememberGlassRailPreferences(context: Context) {
    remember(context) {
        GlassRailPreferences.load(context)
        GlassRailPreferences.loadDensity(context)
        GlassRailPreferences.loadIntensity(context)
    }
}
