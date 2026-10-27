package com.dessalines.thumbkey.ui.components.keyboard

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.MainActivity
import com.dessalines.thumbkey.automation.TaskerThemeAutomation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Stable names keep saved homes valid when new tools join the rotation. */
enum class ToolbarPage(
    val title: String,
    val icon: String,
) {
    CHARACTERS("Character input", "◈"),
    FORMATTING("Formatting", "𝕱"),
    KAOMOJI("Kaomoji", "☺"),
    QUICK_SETTINGS("Quick Settings", "⚙"),
    SNIPPETS("String palettes", "▤"),
    ;

    fun step(delta: Int): ToolbarPage {
        val defaults = entries.filter { it != SNIPPETS }
        return defaults[Math.floorMod(defaults.indexOf(this).coerceAtLeast(0) + delta, defaults.size)]
    }

    val palette: InputPalette?
        get() =
            when (this) {
                CHARACTERS -> null
                FORMATTING -> InputPalette.FANCY_TEXT
                KAOMOJI -> InputPalette.KAOMOJI
                QUICK_SETTINGS -> InputPalette.QUICK_SETTINGS
                SNIPPETS -> InputPalette.SNIPPETS
            }

    companion object {
        fun from(palette: InputPalette?) = entries.first { it.palette == palette }
    }
}

enum class ToolbarGestureAction(
    val title: String,
) {
    NONE("Do nothing"),
    NEXT("Next view"),
    PREVIOUS("Previous view"),
    HOME("Home view"),
    QUICK_SETTINGS("Quick Settings"),
    OPEN_SETTINGS("Open Keywi settings"),
    CONFIGURE("Configure toolbar controls"),
}

enum class ToolbarGesture(
    val title: String,
    val default: ToolbarGestureAction,
) {
    DOUBLE_TAP("Double tap", ToolbarGestureAction.HOME),
    TRIPLE_TAP("Triple tap", ToolbarGestureAction.QUICK_SETTINGS),
    HOLD("Hold", ToolbarGestureAction.CONFIGURE),
}

object ToolbarViewPreferences {
    private fun preferences(context: Context) = context.getSharedPreferences("toolbar_views", Context.MODE_PRIVATE)

    fun invertScroll(context: Context) = preferences(context).getBoolean("invert_tool_scroll", false)
    fun setInvertScroll(context: Context, inverted: Boolean) { preferences(context).edit().putBoolean("invert_tool_scroll", inverted).apply() }

    fun iconStyle(context: Context): ToolbarIconStyle =
        runCatching {
            ToolbarIconStyle.valueOf(preferences(context).getString("icon_style", ToolbarIconStyle.AUTOMATIC.name)!!)
        }.getOrDefault(ToolbarIconStyle.AUTOMATIC)

    fun setIconStyle(
        context: Context,
        style: ToolbarIconStyle,
    ) {
        preferences(context).edit().putString("icon_style", style.name).apply()
    }

    fun home(context: Context): ToolbarPage =
        runCatching {
            ToolbarPage.valueOf(preferences(context).getString("home", ToolbarPage.CHARACTERS.name)!!)
        }.getOrDefault(ToolbarPage.CHARACTERS)

    fun setHome(
        context: Context,
        page: ToolbarPage,
    ) {
        preferences(context).edit().putString("home", page.name).apply()
    }

    fun action(
        context: Context,
        gesture: ToolbarGesture,
    ): ToolbarGestureAction =
        runCatching {
            ToolbarGestureAction.valueOf(preferences(context).getString(gesture.name, gesture.default.name)!!)
        }.getOrDefault(gesture.default)

    fun setAction(
        context: Context,
        gesture: ToolbarGesture,
        action: ToolbarGestureAction,
    ) {
        preferences(context).edit().putString(gesture.name, action.name).apply()
    }
}

internal fun toolbarSwipeStep(distance: Float, inverted: Boolean): Int = (if (distance > 0f) 1 else -1) * if (inverted) -1 else 1

/** Wait for the chain to finish; double/triple taps never fire an earlier single tap. */
internal class ToolbarTapSequence(
    private val scope: CoroutineScope,
    private val timeoutMillis: Long,
    private val onTaps: (Int) -> Unit,
) {
    private var count = 0
    private var pending: Job? = null

    fun pause() {
        pending?.cancel()
    }

    fun tap() {
        pending?.cancel()
        count++
        if (count == 3) {
            dispatch()
        } else {
            pending =
                scope.launch {
                    delay(timeoutMillis)
                    dispatch()
                }
        }
    }

    private fun dispatch() {
        val completed = count
        count = 0
        pending = null
        if (completed > 0) onTaps(completed)
    }

    fun cancel() {
        pending?.cancel()
        pending = null
        count = 0
    }
}

/** Safe from an IME context; no editor text or connection is needed for navigation. */
fun openKeywiSettings(
    context: Context,
    route: String = "settings",
) {
    runCatching {
        context.startActivity(
            Intent(context, MainActivity::class.java).apply {
                putExtra("startRoute", route)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
        )
    }.onFailure { Toast.makeText(context, "Could not open Keywi settings", Toast.LENGTH_SHORT).show() }
}

@Composable
fun ToolbarCycler(
    page: ToolbarPage,
    enabled: Boolean,
    onToggle: () -> Unit,
    onPage: (ToolbarPage) -> Unit,
    onOpenSettings: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    var iconRevision by remember { mutableIntStateOf(0) }
    DisposableEffect(context) {
        val prefs = context.getSharedPreferences("toolbar_views", Context.MODE_PRIVATE)
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> iconRevision++ }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    val iconStyle = remember(iconRevision) { ToolbarViewPreferences.iconStyle(context) }
    val toolbar = remember(TaskerThemeAutomation.revision) { ToolbarThemePreferences.load(context) }.let { if (KeywiPowerPreferences.advancedThemingEnabled) it else it.copy(mode = BackdropMode.NONE) }
    val iconBackground =
        if (toolbar.mode == BackdropMode.COLORFUL && toolbar.stops.isNotEmpty()) {
            val colors = toolbar.stops.map { it.color }
            Color(
                colors.map { it.red }.average().toFloat(),
                colors.map { it.green }.average().toFloat(),
                colors.map { it.blue }.average().toFloat(),
            )
        } else {
            MaterialTheme.colorScheme.surface
        }
    val toolConfig = rememberToolPageConfig()
    val visiblePages = toolConfig.visiblePages()
    val currentPage by rememberUpdatedState(page)
    val toggle by rememberUpdatedState(onToggle)
    val changePage by rememberUpdatedState(onPage)
    val openSettings by rememberUpdatedState(onOpenSettings)
    val performAction: (ToolbarGestureAction) -> Unit = { action ->
        when (action) {
            ToolbarGestureAction.NONE -> Unit
            ToolbarGestureAction.NEXT -> changePage(toolConfig.next(currentPage, 1))
            ToolbarGestureAction.PREVIOUS -> changePage(toolConfig.next(currentPage, -1))
            ToolbarGestureAction.HOME -> changePage(ToolbarViewPreferences.home(context).takeIf { it in visiblePages } ?: ToolbarPage.CHARACTERS)
            ToolbarGestureAction.QUICK_SETTINGS -> changePage(ToolbarPage.QUICK_SETTINGS)
            ToolbarGestureAction.OPEN_SETTINGS -> openSettings?.invoke() ?: openKeywiSettings(context)
            ToolbarGestureAction.CONFIGURE -> {
                ToolbarControlsState.expanded = true
                changePage(ToolbarPage.QUICK_SETTINGS)
            }
        }
    }
    val action by rememberUpdatedState(performAction)
    val scope = rememberCoroutineScope()
    val timeout = LocalViewConfiguration.current.doubleTapTimeoutMillis
    val taps =
        remember(scope, timeout) {
            ToolbarTapSequence(scope, timeout) { count ->
                when (count) {
                    1 -> toggle()
                    2 -> action(ToolbarViewPreferences.action(context, ToolbarGesture.DOUBLE_TAP))
                    3 -> action(ToolbarViewPreferences.action(context, ToolbarGesture.TRIPLE_TAP))
                }
            }
        }
    DisposableEffect(taps) { onDispose { taps.cancel() } }

    Surface(
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(18.dp),
        color = Color.Transparent,
        modifier =
            Modifier
                .width(54.dp)
                .fillMaxHeight()
                .semantics {
                    contentDescription = "${page.title}, page ${(visiblePages.indexOf(page) + 1).coerceAtLeast(1)} of ${visiblePages.size}"
                    onClick("Toggle suggestions") {
                        toggle()
                        true
                    }
                    onLongClick("Hold action") {
                        action(ToolbarViewPreferences.action(context, ToolbarGesture.HOLD))
                        true
                    }
                    customActions =
                        listOf(
                            CustomAccessibilityAction("Next view") {
                                changePage(toolConfig.next(currentPage, 1))
                                true
                            },
                            CustomAccessibilityAction("Previous view") {
                                changePage(toolConfig.next(currentPage, -1))
                                true
                            },
                            CustomAccessibilityAction("Home view") {
                                changePage(ToolbarViewPreferences.home(context).takeIf { it in visiblePages } ?: ToolbarPage.CHARACTERS)
                                true
                            },
                            CustomAccessibilityAction("Double tap action") {
                                action(ToolbarViewPreferences.action(context, ToolbarGesture.DOUBLE_TAP))
                                true
                            },
                            CustomAccessibilityAction("Triple tap action") {
                                action(ToolbarViewPreferences.action(context, ToolbarGesture.TRIPLE_TAP))
                                true
                            },
                            CustomAccessibilityAction("Quick Settings") {
                                changePage(ToolbarPage.QUICK_SETTINGS)
                                true
                            },
                            CustomAccessibilityAction("Configure toolbar controls") {
                                ToolbarControlsState.expanded = true
                                changePage(ToolbarPage.QUICK_SETTINGS)
                                true
                            },
                        )
                }.pointerInput(taps, toolConfig) {
                    detectTapGestures(
                        onPress = {
                            taps.pause()
                            if (!tryAwaitRelease()) taps.cancel()
                        },
                        onTap = { taps.tap() },
                        onLongPress = {
                            taps.cancel()
                            action(ToolbarViewPreferences.action(context, ToolbarGesture.HOLD))
                        },
                    )
                }.pointerInput(taps, toolConfig) {
                    var distance = 0f
                    detectVerticalDragGestures(
                        onDragStart = {
                            taps.cancel()
                            distance = 0f
                        },
                        onVerticalDrag = { change, amount ->
                            change.consume()
                            distance += amount
                        },
                        onDragCancel = { distance = 0f },
                        onDragEnd = {
                            if (kotlin.math.abs(distance) > 12.dp.toPx()) changePage(toolConfig.next(currentPage, toolbarSwipeStep(distance, ToolbarViewPreferences.invertScroll(context))))
                            distance = 0f
                        },
                    )
                },
    ) {
        Box(contentAlignment = Alignment.Center) {
            val light = toolbarIconIsLight(iconStyle, iconBackground)
            val tint = (if (light) Color.White else Color.Black).copy(alpha = if (enabled || page != ToolbarPage.CHARACTERS) 1f else .65f)
            Box(
                Modifier.size(30.dp).clip(CircleShape)
                    .background((if (light) Color.Black else Color.White).copy(alpha = .35f))
                    .border(1.dp, tint.copy(alpha = .55f), CircleShape),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.Diamond, null, Modifier.size(21.dp), tint = tint) }
            Column(
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 3.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                visiblePages.forEach { entry ->
                    Box(
                        Modifier.size(width = if (entry == page) 4.dp else 3.dp, height = 5.dp).background(
                            if (entry == page) Color(0xFFB6DC58) else MaterialTheme.colorScheme.onSurface.copy(alpha = .4f),
                            CircleShape,
                        ),
                    )
                }
            }
        }
    }
}

/** Transient navigation request; never creates a second window over the IME. */
internal object ToolbarControlsState {
    var expanded by mutableStateOf(false)
}

@Composable
internal fun ToolbarControlsPanel(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val visiblePages = rememberToolPageConfig().visiblePages()
    var home by remember { mutableStateOf(ToolbarViewPreferences.home(context)) }
    var iconStyle by remember { mutableStateOf(ToolbarViewPreferences.iconStyle(context)) }
    var inverted by remember { mutableStateOf(ToolbarViewPreferences.invertScroll(context)) }
    var actions by remember { mutableStateOf(ToolbarGesture.entries.associateWith { ToolbarViewPreferences.action(context, it) }) }
    Column(Modifier.padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Toolbar controls", style = MaterialTheme.typography.titleMedium)
                Text("Tap to toggle suggestions. Swipe up or down to change views. Scroll suggestions left or right.")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Invert tool scroll", Modifier.weight(1f))
                    Switch(checked = inverted, onCheckedChange = {
                        inverted = it
                        ToolbarViewPreferences.setInvertScroll(context, it)
                    }, modifier = Modifier.semantics { contentDescription = "Invert tool scroll" })
                }
                Text(if (inverted) "Swipe up: next view · down: previous view" else "Swipe down: next view · up: previous view", style = MaterialTheme.typography.bodySmall)
                ToolbarChoice(
                    title = "Home view",
                    selected = home.title,
                    choices = visiblePages.map { it.title },
                    onSelect = { index ->
                        home = visiblePages[index]
                        ToolbarViewPreferences.setHome(context, home)
                    },
                )
                ToolbarChoice(
                    title = "Cycler icon",
                    selected = iconStyle.title,
                    choices = ToolbarIconStyle.entries.map { it.title },
                    onSelect = { index ->
                        iconStyle = ToolbarIconStyle.entries[index]
                        ToolbarViewPreferences.setIconStyle(context, iconStyle)
                    },
                )
                ToolbarGesture.entries.forEach { gesture ->
                    ToolbarChoice(
                        title = gesture.title,
                        selected = actions.getValue(gesture).title,
                        choices = ToolbarGestureAction.entries.map { it.title },
                        onSelect = { index ->
                            val selected = ToolbarGestureAction.entries[index]
                            actions = actions + (gesture to selected)
                            ToolbarViewPreferences.setAction(context, gesture, selected)
                        },
                    )
                }
                Text(
                    "These controls are always available from Quick Settings, even if you change the hold action.",
                    style = MaterialTheme.typography.bodySmall,
                )
        TextButton(onClick = onDismiss) { Text("Done") }
    }
}

@Composable
private fun ToolbarChoice(
    title: String,
    selected: String,
    choices: List<String>,
    onSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        TextButton(onClick = { expanded = !expanded }) { Text("$title: $selected") }
        if (expanded) Column {
            choices.forEachIndexed { index, choice ->
                ToolPageSurface(selected = choice == selected, onClick = {
                    expanded = false
                    onSelect(index)
                }) { Text(choice, Modifier.padding(12.dp)) }
            }
        }
    }
}
