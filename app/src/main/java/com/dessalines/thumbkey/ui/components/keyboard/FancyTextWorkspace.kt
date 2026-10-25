package com.dessalines.thumbkey.ui.components.keyboard

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    var category by rememberSaveable { mutableStateOf<FancyTextFormatter.Category?>(null) }
    var lastApplied by remember(previewText) { mutableStateOf<String?>(null) }
    val sample = previewText.ifEmpty { "Keywi 123" }
    val config = rememberToolPageConfig()
    val visibleCategories = ordered(config.formatCategories, FancyTextFormatter.Category.entries.map { it.name })
        .filterNot { it in config.hiddenFormatCategories }.map { FancyTextFormatter.Category.valueOf(it) }
    val activeCategory = category?.takeIf { it in visibleCategories }
    val styles = config.visibleFormats().filter { activeCategory == null || it.category == activeCategory }

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        color = Color.Transparent,
        border = null,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 9.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "𝕱  FANCY TEXT",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                if (previewText.isEmpty()) {
                    "Select text to apply • showing sample previews"
                } else {
                    "Previewing selected text • tap a style to apply"
                },
                style = MaterialTheme.typography.bodySmall,
            )
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                PaletteWorkspaceCategoryChip(
                    text = "All",
                    selected = activeCategory == null,
                    theme = theme,
                    onClick = { category = null },
                )
                visibleCategories.forEach { entry ->
                    PaletteWorkspaceCategoryChip(
                        text = entry.title,
                        selected = activeCategory == entry,
                        theme = theme,
                        onClick = { category = entry },
                    )
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                if (styles.isEmpty()) Text("No styles enabled. Add them in Tool pages & palettes.")
                styles.forEachIndexed { index, style ->
                    PaletteKeySurface(
                        onClick = { lastApplied = style.id; onApply(style.id) },
                        selected = lastApplied == style.id,
                        designId = "fancy:${style.id}", sequenceIndex = index,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Text(style.title, modifier = Modifier.weight(.4f), style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                FancyTextFormatter.transform(sample, style.id),
                                modifier = Modifier.weight(.6f),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (lastApplied == style.id) FontWeight.SemiBold else FontWeight.Normal,
                                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}
