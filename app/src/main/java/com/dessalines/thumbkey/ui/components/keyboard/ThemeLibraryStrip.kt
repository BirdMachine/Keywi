package com.dessalines.thumbkey.ui.components.keyboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Library presentation independent of persistence and the keyboard geometry.
 * The editor supplies each theme's real background renderer (image, GIF,
 * gradient or color); a dark scrim keeps names legible over busy artwork.
 */
data class ThemeLibraryTile(
    val id: String,
    val name: String,
    val author: String? = null,
    val background: @Composable () -> Unit,
)

@Composable
fun ThemeLibraryStrip(
    themes: List<ThemeLibraryTile>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        themes.forEach { theme ->
            val selected = theme.id == selectedId
            Surface(
                onClick = { onSelect(theme.id) },
                modifier = Modifier.width(178.dp).height(100.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border =
                    BorderStroke(
                        if (selected) 2.dp else 1.dp,
                        if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                        },
                    ),
            ) {
                Box {
                    theme.background()
                    // The title is readable without obscuring the upper artwork.
                    Surface(
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                        color = Color(0xD9141828),
                        contentColor = Color.White,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    theme.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                theme.author?.takeIf { it.isNotBlank() }?.let {
                                    Text(
                                        it,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                            if (selected) Text("✓", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
