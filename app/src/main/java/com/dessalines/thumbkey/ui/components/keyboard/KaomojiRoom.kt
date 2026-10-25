package com.dessalines.thumbkey.ui.components.keyboard

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException

private const val FAVORITES_CATEGORY = "__favorites__"
private const val RECENTS_CATEGORY = "__recents__"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KaomojiRoom(
    onCommit: (String) -> Unit,
    onBackToLetters: () -> Unit,
    onGoToEmoji: (() -> Unit)? = null,
    showDismissButton: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var selectedCategoryId by rememberSaveable { mutableStateOf(KaomojiLibrary.categories.first().id) }
    val searchQuery = PaletteSearchCapture.query
    val searchActive = PaletteSearchCapture.active
    var favorites by remember { mutableStateOf(KaomojiPreferences.loadFavorites(context)) }
    var recents by remember { mutableStateOf(KaomojiPreferences.loadRecents(context)) }
    val lozengeTheme = SuggestionLozengeThemePreferences.load(context)

    var webItems by remember { mutableStateOf<List<StringPaletteItem>>(emptyList()) }
    var requestedQuery by remember { mutableStateOf<String?>(null) }
    var requestNumber by remember { mutableIntStateOf(0) }
    var webStatus by remember { mutableStateOf<String?>(null) }
    var webLoading by remember { mutableStateOf(false) }
    LaunchedEffect(searchQuery, requestedQuery, requestNumber) {
        webItems = emptyList()
        webStatus = null
        webLoading = false
        if (requestedQuery != searchQuery || searchQuery.isBlank()) {
            requestedQuery = null
            return@LaunchedEffect
        }
        val source = KaomojiSearchPreferences.active(context)
        webLoading = true
        try {
            webItems = KaomojiWebSearch.search(source, searchQuery)
            webStatus = "${source.name}: ${webItems.size} web results"
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            webStatus = error.message ?: "Web search unavailable. Try opening the website."
        } finally {
            webLoading = false
        }
    }

    val localItems =
        if (searchQuery.isNotBlank()) {
            KaomojiLibrary.search(searchQuery).distinctBy { it.text }
        } else {
            when (selectedCategoryId) {
                FAVORITES_CATEGORY -> {
                    favorites.map { text -> KaomojiLibrary.allItems.firstOrNull { it.text == text } ?: StringPaletteItem(text) }
                }

                RECENTS_CATEGORY -> {
                    recents.map { recent -> KaomojiLibrary.allItems.firstOrNull { it.text == recent } ?: StringPaletteItem(recent) }
                }

                else -> {
                    KaomojiLibrary.categories
                        .firstOrNull { it.id == selectedCategoryId }
                        ?.items
                        ?: KaomojiLibrary.categories.first().items
                }
            }
        }

    val items = (localItems + webItems).distinctBy { it.text }

    fun commit(text: String) {
        recents = KaomojiPreferences.recordRecent(context, text)
        onCommit(text)
    }

    fun chooseCategory(id: String) {
        PaletteSearchCapture.release(clear = true)
        selectedCategoryId = id
    }

    fun openSearchWebsite() {
        val source = KaomojiSearchPreferences.active(context)
        runCatching {
            val uri = Uri.parse(source.searchUrl(searchQuery))
            validateSearchUri(uri.toString())
            context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.onFailure { webStatus = "Could not open the search website." }
    }

    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                .toolPageBackground()
                .padding(horizontal = 9.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(
            "✧  KAOMOJI",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ToolPageSurface(
                onClick = { PaletteSearchCapture.activate() },
                modifier = Modifier.weight(1f), selected = searchActive, designId = "kaomoji-search",
            ) {
                Text(
                    text =
                        when {
                            searchQuery.isNotEmpty() && searchActive -> "$searchQuery ▏"
                            searchQuery.isNotEmpty() -> searchQuery
                            searchActive -> "Type to search… ▏"
                            else -> "Search…"
                        },
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
                    color =
                        if (searchQuery.isEmpty() && !searchActive) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (searchQuery.isNotBlank()) {
                PaletteLozengeButton(
                    text = "Web↗",
                    onClick = ::openSearchWebsite,
                    theme = lozengeTheme,
                    compact = true,
                )
            }

            onGoToEmoji?.let { goToEmoji ->
                PaletteLozengeButton(
                    text = "☺",
                    onClick = goToEmoji,
                    theme = lozengeTheme,
                )
            }
            if (showDismissButton) {
                PaletteLozengeButton(
                    text = "✕",
                    onClick = {
                        PaletteSearchCapture.release(clear = true)
                        onBackToLetters()
                    },
                    theme = lozengeTheme,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (searchQuery.isNotBlank()) {
                PaletteLozengeButton(
                    text = if (webLoading) "Searching…" else "Search web",
                    onClick = {
                        if (!webLoading) {
                            if (KaomojiSearchPreferences.active(context).selector.isBlank()) {
                                openSearchWebsite()
                            } else {
                                requestedQuery = searchQuery
                                requestNumber++
                            }
                        }
                    },
                    theme = lozengeTheme,
                    compact = true,
                )
            }
            PaletteLozengeButton(
                text = "Search sources ⚙",
                onClick = {
                    PaletteSearchCapture.release()
                    context.startActivity(
                        Intent(context, KaomojiSearchSettingsActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                },
                theme = lozengeTheme,
                compact = true,
            )
        }
        webStatus?.let { status ->
            Text(status, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
        }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            PaletteCategoryChip(
                text = "★ Favorites",
                selected = selectedCategoryId == FAVORITES_CATEGORY && searchQuery.isBlank(),
                theme = lozengeTheme,
                onClick = { chooseCategory(FAVORITES_CATEGORY) },
            )
            PaletteCategoryChip(
                text = "↻ Recent",
                selected = selectedCategoryId == RECENTS_CATEGORY && searchQuery.isBlank(),
                theme = lozengeTheme,
                onClick = { chooseCategory(RECENTS_CATEGORY) },
            )
            KaomojiLibrary.categories.forEach { entry ->
                PaletteCategoryChip(
                    text = "${entry.glyph} ${entry.title}",
                    selected = selectedCategoryId == entry.id && searchQuery.isBlank(),
                    theme = lozengeTheme,
                    onClick = { chooseCategory(entry.id) },
                )
            }
        }

        if (items.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.62f),
            ) {
                Text(
                    text =
                        when {
                            searchQuery.isNotBlank() -> "No local kaomoji match “$searchQuery”. Tap Search web for more."
                            selectedCategoryId == FAVORITES_CATEGORY -> "No favorites yet — double-tap a kaomoji to pin it here."
                            else -> "Nothing here yet. Your recently used kaomoji will appear here."
                        },
                    modifier = Modifier.padding(11.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 106.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f),
            ) {
                items(items, key = { it.text }) { entry ->
                    val favorite = entry.text in favorites
                    PaletteKaomojiCell(
                        entry = entry,
                        favorite = favorite,
                        theme = lozengeTheme,
                        onClick = { commit(entry.text) },
                        onDoubleClick = {
                            favorites = KaomojiPreferences.toggleFavorite(context, entry.text)
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PaletteKaomojiCell(
    entry: StringPaletteItem,
    favorite: Boolean,
    theme: SuggestionLozengeThemeState,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
) {
    ToolPageSurface(
        onClick = onClick, onDoubleClick = onDoubleClick, selected = favorite,
        designId = "kaomoji:${entry.text}", modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 9.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = entry.text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = if (favorite) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            entry.label?.let { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun PaletteCategoryChip(
    text: String,
    selected: Boolean,
    theme: SuggestionLozengeThemeState,
    onClick: () -> Unit,
) {
    ToolPageSurface(onClick = onClick, selected = selected, designId = "kaomoji-category:$text") {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PaletteLozengeButton(
    text: String,
    onClick: () -> Unit,
    theme: SuggestionLozengeThemeState,
    compact: Boolean = false,
) {
    ToolPageSurface(onClick = onClick, designId = "kaomoji-control:$text") {
        Text(
            text = text,
            modifier =
                if (compact) {
                    Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                } else {
                    Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                },
            style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium,
        )
    }
}
