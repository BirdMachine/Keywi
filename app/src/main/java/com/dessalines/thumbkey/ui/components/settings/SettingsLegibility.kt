package com.dessalines.thumbkey.ui.components.settings

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import com.dessalines.thumbkey.ui.components.keyboard.BackdropMode

/** Contrast uses rendered colors, rather than assuming custom keyboard text matches its surface. */
internal fun settingsContrastRatio(
    foreground: Color,
    background: Color,
): Float {
    val back = background.compositeOver(Color.White)
    val front = foreground.compositeOver(back)
    val a = front.luminance()
    val b = back.luminance()
    return (maxOf(a, b) + .05f) / (minOf(a, b) + .05f)
}

internal fun readableSettingsForeground(background: Color): Color =
    if (settingsContrastRatio(Color.White, background) >= settingsContrastRatio(Color.Black, background)) Color.White else Color.Black

/** Use the still frame so animated themes never make controls flash between light and dark. */
internal fun settingsArtworkIsDark(
    style: SettingsDripStyle?,
    fallback: Color,
): Boolean {
    val samples =
        when {
            style == null -> {
                emptyList()
            }

            style.backdrop.mode == BackdropMode.COLORFUL -> {
                style.backdrop.stops.map { it.color }
            }

            style.image != null -> {
                runCatching {
                    val pixels = style.image.toPixelMap()
                    buildList {
                        for (row in 0..4) {
                            for (column in 0..4) {
                                add(pixels[(pixels.width - 1) * column / 4, (pixels.height - 1) * row / 4])
                            }
                        }
                    }
                }.getOrDefault(emptyList())
            }

            else -> {
                emptyList()
            }
        }
    val luminance =
        if (samples.isEmpty()) {
            fallback.luminance()
        } else {
            samples
                .map {
                    it
                        .copy(
                            alpha = it.alpha * (style?.backdrop?.opacity ?: 1f),
                        ).compositeOver(fallback)
                        .luminance()
                }.average()
                .toFloat()
        }
    return luminance < .5f
}

/** A complete local palette prevents keyboard-specific text overrides leaking into controls. */
internal fun gemSettingsScheme(
    dark: Boolean,
    colors: AdvancedSettingsColors,
): ColorScheme =
    (if (dark) darkColorScheme() else lightColorScheme()).copy(
        primary = colors.accent,
        onPrimary = readableSettingsForeground(colors.accent),
        primaryContainer = colors.surfaceRaised,
        onPrimaryContainer = colors.text,
        secondary = if (dark) Color(0xFFA9A6FF) else Color(0xFF5952A4),
        secondaryContainer = colors.surfaceRaised,
        onSecondaryContainer = colors.text,
        tertiary = if (dark) Color(0xFF71DDE8) else Color(0xFF006875),
        background = colors.background,
        onBackground = colors.text,
        surface = colors.background,
        onSurface = colors.text,
        surfaceVariant = colors.surface,
        onSurfaceVariant = colors.textMuted,
        surfaceContainer = colors.surface,
        surfaceContainerLow = colors.surface,
        surfaceContainerLowest = colors.background,
        surfaceContainerHigh = colors.surfaceRaised,
        surfaceContainerHighest = colors.surfaceRaised,
        outline = colors.outline,
        outlineVariant = colors.outline.copy(alpha = .65f),
        inversePrimary = if (dark) Color(0xFF7434BD) else Color(0xFFD969FF),
        surfaceTint = colors.accent,
    )
