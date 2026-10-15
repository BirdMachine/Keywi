package com.dessalines.thumbkey.ui.components.keyboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Shared key-surface treatment for input workspace controls and preview cards. */
@Composable
internal fun PaletteKeySurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    radius: Int = 12,
    content: @Composable () -> Unit,
) {
    val theme = KeyThemePreferences.current
    val shape = RoundedCornerShape(radius.dp)
    val decorated = modifier.clip(shape)
        .then(if (theme.surfaceStyle == KeySurfaceStyle.GRADIENT) {
            Modifier.keyboardGradientBackground(theme.surfaceGradient)
        } else Modifier)
        .then(if (theme.borderStyle == KeyBorderStyle.GRADIENT) {
            Modifier.keyboardGradientBorder(theme.borderGradient, 1.dp, radius.dp)
        } else Modifier)
    Surface(
        onClick = onClick,
        modifier = decorated,
        shape = shape,
        color = when (theme.surfaceStyle) {
            KeySurfaceStyle.SOLID -> theme.surfaceColor
            KeySurfaceStyle.GRADIENT -> Color.Transparent
            KeySurfaceStyle.NONE -> MaterialTheme.colorScheme.surface.copy(alpha = 0.24f)
        },
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = if (theme.borderStyle == KeyBorderStyle.SOLID) {
            BorderStroke(1.dp, theme.borderColor)
        } else null,
        shadowElevation = if (theme.borderStyle == KeyBorderStyle.SHADOW) theme.shadowElevation.dp else 0.dp,
    ) {
        Box { content() }
    }
}
