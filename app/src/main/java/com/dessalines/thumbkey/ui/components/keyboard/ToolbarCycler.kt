package com.dessalines.thumbkey.ui.components.keyboard

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
    ;

    fun step(delta: Int) = entries[Math.floorMod(ordinal + delta, entries.size)]

    val palette: InputPalette?
        get() =
            when (this) {
                CHARACTERS -> null
                FORMATTING -> InputPalette.FANCY_TEXT
                KAOMOJI -> InputPalette.KAOMOJI
                QUICK_SETTINGS -> InputPalette.QUICK_SETTINGS
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
    val toolbar = remember(TaskerThemeAutomation.revision) { ToolbarThemePreferences.load(context) }
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
    var showSettings by remember { mutableStateOf(false) }
    val currentPage by rememberUpdatedState(page)
    val toggle by rememberUpdatedState(onToggle)
    val changePage by rememberUpdatedState(onPage)
    val openSettings by rememberUpdatedState(onOpenSettings)
    val performAction: (ToolbarGestureAction) -> Unit = { action ->
        when (action) {
            ToolbarGestureAction.NONE -> Unit
            ToolbarGestureAction.NEXT -> changePage(currentPage.step(1))
            ToolbarGestureAction.PREVIOUS -> changePage(currentPage.step(-1))
            ToolbarGestureAction.HOME -> changePage(ToolbarViewPreferences.home(context))
            ToolbarGestureAction.QUICK_SETTINGS -> changePage(ToolbarPage.QUICK_SETTINGS)
            ToolbarGestureAction.OPEN_SETTINGS -> openSettings?.invoke() ?: openKeywiSettings(context)
            ToolbarGestureAction.CONFIGURE -> showSettings = true
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
                    contentDescription = "${page.title}, page ${page.ordinal + 1} of ${ToolbarPage.entries.size}"
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
                                changePage(currentPage.step(1))
                                true
                            },
                            CustomAccessibilityAction("Previous view") {
                                changePage(currentPage.step(-1))
                                true
                            },
                            CustomAccessibilityAction("Home view") {
                                changePage(ToolbarViewPreferences.home(context))
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
                                showSettings = true
                                true
                            },
                        )
                }.pointerInput(taps) {
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
                }.pointerInput(taps) {
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
                            if (kotlin.math.abs(distance) > 12.dp.toPx()) changePage(currentPage.step(if (distance < 0) 1 else -1))
                            distance = 0f
                        },
                    )
                },
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            ToolbarKiwiIcon(
                light = toolbarIconIsLight(iconStyle, iconBackground),
                modifier = Modifier.size(28.dp),
                alpha = if (enabled || page != ToolbarPage.CHARACTERS) 1f else .65f,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                ToolbarPage.entries.forEach { entry ->
                    Box(
                        Modifier.size(if (entry == page) 5.dp else 3.dp).background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = if (entry == page) 1f else .35f),
                            CircleShape,
                        ),
                    )
                }
            }
        }
    }
    if (showSettings) ToolbarControlsDialog(onDismiss = { showSettings = false })
}

@Composable
fun ToolbarControlsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var home by remember { mutableStateOf(ToolbarViewPreferences.home(context)) }
    var iconStyle by remember { mutableStateOf(ToolbarViewPreferences.iconStyle(context)) }
    var actions by remember { mutableStateOf(ToolbarGesture.entries.associateWith { ToolbarViewPreferences.action(context, it) }) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Toolbar controls") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tap to toggle suggestions. Swipe up or down to change views. Scroll suggestions left or right.")
                ToolbarChoice(
                    title = "Home view",
                    selected = home.title,
                    choices = ToolbarPage.entries.map { it.title },
                    onSelect = { index ->
                        home = ToolbarPage.entries[index]
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
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
    )
}

@Composable
private fun ToolbarChoice(
    title: String,
    selected: String,
    choices: List<String>,
    onSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { expanded = true }) { Text("$title: $selected") }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            choices.forEachIndexed { index, choice ->
                DropdownMenuItem(text = { Text(choice) }, onClick = {
                    expanded = false
                    onSelect(index)
                })
            }
        }
    }
}
