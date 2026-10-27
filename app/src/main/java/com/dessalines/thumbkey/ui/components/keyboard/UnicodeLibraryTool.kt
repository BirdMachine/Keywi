package com.dessalines.thumbkey.ui.components.keyboard

import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.R
import com.dessalines.thumbkey.unicode.UnicodeCatalog
import com.dessalines.thumbkey.unicode.UnicodeCategories
import com.dessalines.thumbkey.unicode.UnicodeEntry
import com.dessalines.thumbkey.unicode.UnicodeLibraryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

private const val UNICODE_PAGE_SIZE = 120

/** The search query stays inside Keywi; insertion always bypasses palette search capture. */
@Composable
fun UnicodeLibraryTool(
    onInsert: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var catalog by remember { mutableStateOf<UnicodeCatalog?>(null) }
    var loadFailed by remember { mutableStateOf(false) }
    var retry by remember { mutableIntStateOf(0) }
    // Start with useful symbols, rather than a screen of C0 controls. The first
    // search covers the full catalog unless the user deliberately chose a filter.
    var categoryId by rememberSaveable { mutableStateOf("symbols") }
    var initialBrowseCategory by rememberSaveable { mutableStateOf(true) }
    var block by rememberSaveable { mutableStateOf<String?>(null) }
    var categoryMenu by remember { mutableStateOf(false) }
    var blockMenu by remember { mutableStateOf(false) }
    var help by remember { mutableStateOf(false) }
    var detail by remember { mutableStateOf<UnicodeEntry?>(null) }
    val query = PaletteSearchCapture.query
    val searchActive = PaletteSearchCapture.active
    var page by remember(query, categoryId, block) { mutableIntStateOf(0) }
    val gridState = rememberLazyGridState()
    var results by remember { mutableStateOf<List<UnicodeEntry>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }
    var hasMore by remember { mutableStateOf(false) }
    var support by remember { mutableStateOf<Map<Int, Boolean>>(emptyMap()) }

    DisposableEffect(Unit) {
        onDispose { PaletteSearchCapture.release(clear = true) }
    }
    LaunchedEffect(retry) {
        loadFailed = false
        try {
            catalog = UnicodeLibraryRepository.load(context.applicationContext)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            loadFailed = true
        }
    }
    LaunchedEffect(catalog, query, categoryId, block, page) {
        val loaded = catalog ?: return@LaunchedEffect
        searching = true
        // Avoid retaining stale cells while a different query or filter is being evaluated.
        results = emptyList()
        support = emptyMap()
        if (query.isNotEmpty()) delay(120)
        val found = withContext(Dispatchers.Default) {
            val searchContext = coroutineContext
            loaded.search(
                query, categoryId, block,
                limit = UNICODE_PAGE_SIZE + 1,
                offset = page * UNICODE_PAGE_SIZE,
                ensureActive = { searchContext.ensureActive() },
            )
        }
        hasMore = found.size > UNICODE_PAGE_SIZE
        results = found.take(UNICODE_PAGE_SIZE)
        gridState.scrollToItem(0)
        searching = false
    }
    LaunchedEffect(results) {
        val checked = withContext(Dispatchers.Default) {
            val paint = Paint().apply { typeface = Typeface.SANS_SERIF }
            buildMap {
                for (entry in results) {
                    coroutineContext.ensureActive()
                    // Combining marks need a shaping context. A standalone failure does not
                    // establish that their font is missing. Never probe the dotted-circle aid.
                    if (entry.hasStandaloneGlyphPreview()) put(entry.codePoint, paint.hasGlyph(entry.text))
                }
            }
        }
        support = checked
    }

    fun close() {
        PaletteSearchCapture.release(clear = true)
        onBack()
    }
    fun insert(entry: UnicodeEntry) {
        PaletteSearchCapture.bypass { onInsert(entry.text) }
    }

    Column(
        modifier = modifier.toolPageBackground().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                stringResource(R.string.unicode_library_title, catalog?.version ?: "…"),
                modifier = Modifier.weight(1f).padding(top = 13.dp),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            TextButton(onClick = { PaletteSearchCapture.release(); help = true }) {
                Text(stringResource(R.string.unicode_library_help))
            }
            TextButton(onClick = ::close) { Text(stringResource(R.string.unicode_library_keyboard)) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ToolPageSurface(
                onClick = {
                    if (initialBrowseCategory) {
                        categoryId = "all"
                        initialBrowseCategory = false
                    }
                    PaletteSearchCapture.activate()
                },
                modifier = Modifier.weight(1f),
                selected = searchActive,
                designId = "unicode-search",
            ) {
                Text(
                    when {
                        query.isNotEmpty() -> if (searchActive) "$query ▏" else query
                        searchActive -> stringResource(R.string.unicode_library_search_active)
                        else -> stringResource(R.string.unicode_library_search)
                    },
                    Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (query.isNotEmpty()) {
                TextButton(onClick = { PaletteSearchCapture.clear() }) {
                    Text(stringResource(R.string.unicode_library_clear))
                }
            }
            if (searchActive) {
                TextButton(onClick = { PaletteSearchCapture.release() }) {
                    Text(stringResource(R.string.unicode_library_done))
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(Modifier.weight(1f)) {
                TextButton(onClick = { categoryMenu = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        (UnicodeCategories.groups.firstOrNull { it.id == categoryId }?.label ?: "All") + " ▾",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                DropdownMenu(categoryMenu, { categoryMenu = false }) {
                    UnicodeCategories.groups.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.label) },
                            onClick = { categoryId = category.id; initialBrowseCategory = false; categoryMenu = false },
                        )
                    }
                }
            }
            Box(Modifier.weight(1f)) {
                TextButton(onClick = { blockMenu = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(block ?: stringResource(R.string.unicode_library_all_blocks), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                DropdownMenu(blockMenu, { blockMenu = false }, modifier = Modifier.heightIn(max = 280.dp)) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.unicode_library_all_blocks)) },
                        onClick = { block = null; blockMenu = false },
                    )
                    catalog?.blocks.orEmpty().forEach { name ->
                        DropdownMenuItem(text = { Text(name) }, onClick = { block = name; blockMenu = false })
                    }
                }
            }
        }
        when {
            loadFailed -> {
                Text(stringResource(R.string.unicode_library_error))
                TextButton(onClick = { retry++ }) { Text(stringResource(R.string.unicode_library_retry)) }
            }
            catalog == null || searching -> Text(stringResource(R.string.unicode_library_loading))
            results.isEmpty() -> Text(stringResource(R.string.unicode_library_empty), style = MaterialTheme.typography.bodyMedium)
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(88.dp),
                    state = gridState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(results, key = { it.codePoint }) { entry ->
                        UnicodeCharacterCell(entry, support[entry.codePoint], onClick = {
                            if (entry.isIntentionallyInvisible()) detail = entry else insert(entry)
                        }, onDetails = { detail = entry })
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                TextButton(onClick = { page-- }, enabled = page > 0) {
                                    Text(stringResource(R.string.unicode_library_previous))
                                }
                                Text(stringResource(R.string.unicode_library_page, page + 1), modifier = Modifier.padding(top = 13.dp))
                                TextButton(onClick = { page++ }, enabled = hasMore) {
                                    Text(stringResource(R.string.unicode_library_next))
                                }
                            }
                            Text(
                                stringResource(R.string.unicode_library_results, results.size),
                                Modifier.padding(8.dp),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }
        }
    }
    detail?.let { entry ->
        AlertDialog(
            onDismissRequest = { detail = null },
            title = { Text(entry.displayName) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.unicode_library_details, entry.codePointLabel, entry.name, entry.block, entry.category))
                    if (entry.aliases.isNotEmpty()) Text(stringResource(R.string.unicode_library_aliases, entry.aliases.joinToString()))
                    if (entry.isIntentionallyInvisible()) Text(stringResource(R.string.unicode_library_special))
                    if (entry.category.startsWith("M")) Text(stringResource(R.string.unicode_library_mark))
                    if (support[entry.codePoint] == false) Text(stringResource(R.string.unicode_library_missing))
                }
            },
            confirmButton = { TextButton(onClick = { insert(entry); detail = null }) { Text(stringResource(R.string.unicode_library_insert)) } },
            dismissButton = { TextButton(onClick = { detail = null }) { Text(stringResource(R.string.unicode_library_close)) } },
        )
    }
    if (help) {
        UnicodeGlyphHelp(results, support, onClose = { help = false })
    }
}

@Composable
private fun UnicodeCharacterCell(entry: UnicodeEntry, supported: Boolean?, onClick: () -> Unit, onDetails: () -> Unit) {
    ToolPageSurface(
        onClick = onClick,
        onLongClick = onDetails,
        designId = "unicode:${entry.codePoint}",
        modifier = Modifier.fillMaxWidth().semantics {
            contentDescription = "${entry.displayName}, ${entry.codePointLabel}"
        },
    ) {
        Column(Modifier.fillMaxWidth().padding(7.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = when {
                    entry.isIntentionallyInvisible() -> stringResource(R.string.unicode_library_invisible)
                    supported == false -> "□"
                    entry.category.startsWith("M") -> "◌${entry.text}"
                    else -> entry.text
                },
                fontFamily = FontFamily.SansSerif,
                style = if (entry.isIntentionallyInvisible()) MaterialTheme.typography.labelSmall else MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(entry.codePointLabel, style = MaterialTheme.typography.labelSmall, maxLines = 1)
            Text(entry.displayName, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun UnicodeEntry.isIntentionallyInvisible(): Boolean =
    category.startsWith("C") || category.startsWith("Z") || codePoint == 0x034F ||
        codePoint in 0xFE00..0xFE0F || codePoint in 0xE0100..0xE01EF ||
        codePoint in 0x115F..0x1160 || codePoint == 0x3164 || codePoint == 0xFFA0

private fun UnicodeEntry.hasStandaloneGlyphPreview(): Boolean = !isIntentionallyInvisible() && !category.startsWith("M")

@Composable
private fun UnicodeGlyphHelp(entries: List<UnicodeEntry>, support: Map<Int, Boolean>, onClose: () -> Unit) {
    val context = LocalContext.current
    val missing = entries.filter { support[it.codePoint] == false }
    val checking = entries.any { it.hasStandaloneGlyphPreview() && it.codePoint !in support }
    fun openFonts(url: String) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.onFailure { Toast.makeText(context, R.string.unicode_library_link_error, Toast.LENGTH_SHORT).show() }
    }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(stringResource(R.string.unicode_library_help_title)) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { Text(stringResource(R.string.unicode_library_help_body)) }
                item {
                    Text(
                        if (checking) stringResource(R.string.unicode_library_support_checking)
                        else stringResource(R.string.unicode_library_missing_count, missing.size, entries.size),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                if (!checking && missing.isEmpty()) item { Text(stringResource(R.string.unicode_library_no_missing)) }
                items(missing, key = { it.codePoint }) { entry ->
                    Text(stringResource(R.string.unicode_library_missing_item, entry.codePointLabel, entry.displayName))
                }
                item {
                    TextButton(onClick = { openFonts("https://fonts.google.com/noto/") }) { Text(stringResource(R.string.unicode_library_noto)) }
                    TextButton(onClick = { openFonts("https://notofonts.github.io/") }) { Text(stringResource(R.string.unicode_library_font_downloads)) }
                }
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.unicode_library_close)) } },
    )
}
