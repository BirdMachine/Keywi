package com.dessalines.thumbkey.ui.components.keyboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** Shared key-themed category control for Fancy Text and Kaomoji. */
@Composable
internal fun PaletteWorkspaceCategoryChip(
    text: String,
    selected: Boolean,
    theme: SuggestionLozengeThemeState,
    onClick: () -> Unit,
) {
    PaletteKeySurface(onClick = onClick, radius = 12) {
        Text(
            text = if (selected) "◆ $text" else text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
