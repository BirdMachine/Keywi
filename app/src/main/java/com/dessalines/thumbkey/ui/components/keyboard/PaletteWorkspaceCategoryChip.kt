package com.dessalines.thumbkey.ui.components.keyboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** Shared theme-aware category chip for Fancy Text and Kaomoji. */
@Composable
internal fun PaletteWorkspaceCategoryChip(
    text: String,
    selected: Boolean,
    theme: SuggestionLozengeThemeState,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    val modifier =
        Modifier
            .clip(shape)
            .then(
                when (theme.surfaceStyle) {
                    SuggestionLozengeSurfaceStyle.GRADIENT -> Modifier.keyboardGradientBackground(theme.surfaceGradient)
                    SuggestionLozengeSurfaceStyle.SOLID,
                    SuggestionLozengeSurfaceStyle.NONE,
                    -> Modifier
                },
            )

    Surface(
        onClick = onClick,
        shape = shape,
        color =
            when (theme.surfaceStyle) {
                SuggestionLozengeSurfaceStyle.SOLID -> theme.surfaceColor.copy(alpha = if (selected) 0.95f else 0.72f)
                SuggestionLozengeSurfaceStyle.GRADIENT -> Color.Transparent
                SuggestionLozengeSurfaceStyle.NONE -> MaterialTheme.colorScheme.surface.copy(alpha = if (selected) 0.72f else 0.46f)
            },
        border =
            BorderStroke(
                if (selected) 2.dp else 1.dp,
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
            ),
        modifier = modifier,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

