package com.dessalines.thumbkey.ui.components.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * The shared palette host owns the header, background and dismiss action.
 * This panel owns only Fancy Text browsing and editor-selection previews.
 */
@Composable
fun FancyTextWorkspace(
    onApply: (String) -> Unit,
    modifier: Modifier = Modifier,
    previewText: String = "",
) {
    val context = LocalContext.current
    val theme = SuggestionLozengeThemePreferences.load(context)
    var category by remember { mutableStateOf<FancyTextFormatter.Category?>(null) }
    val sample = previewText.ifEmpty { "Keywi 123" }
    val styles = FancyTextFormatter.styles.filter { category == null || it.category == category }

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 9.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                if (previewText.isEmpty()) "Select text to apply • showing sample previews"
                else "Previewing selected text • tap a style to apply",
                style = MaterialTheme.typography.bodySmall,
            )
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                PaletteWorkspaceCategoryChip(
                    text = "All",
                    selected = category == null,
                    theme = theme,
                    onClick = { category = null },
                )
                FancyTextFormatter.Category.entries.forEach { entry ->
                    PaletteWorkspaceCategoryChip(
                        text = entry.title,
                        selected = category == entry,
                        theme = theme,
                        onClick = { category = entry },
                    )
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                styles.chunked(2).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        row.forEach { style ->
                            Surface(
                                onClick = { onApply(style.id) },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                modifier = Modifier.weight(1f),
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp),
                                ) {
                                    Text(style.title, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                    Text(
                                        FancyTextFormatter.transform(sample, style.id),
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                        if (row.size == 1) {
                            androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
