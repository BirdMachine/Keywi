package com.dessalines.thumbkey.ui.components.keyboard

import android.content.Intent
import android.view.KeyEvent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.IMEService
import com.dessalines.thumbkey.MainActivity
import com.dessalines.thumbkey.db.DEFAULT_KEY_BORDER_WIDTH
import com.dessalines.thumbkey.db.DEFAULT_KEY_RADIUS
import com.dessalines.thumbkey.effects.ActionSoundEngine
import com.dessalines.thumbkey.effects.ActionSoundPreferences
import com.dessalines.thumbkey.effects.SoundSelection
import com.dessalines.thumbkey.effects.SoundTrigger
import com.dessalines.thumbkey.effects.TaskerKeyBindings

/**
 * HK-Like: compact touch keyboard with an optional desktop-control layer.
 * Shares the ambient Keywi theme rather than introducing a separate palette.
 */
@Composable
fun MachineCutSkeleton(
    modifier: Modifier = Modifier,
    layout: MachineCutLayout = MachineCutLayouts.touchMain,
    keyBorderWidth: Float = DEFAULT_KEY_BORDER_WIDTH / 10f,
    keyRadius: Int = DEFAULT_KEY_RADIUS,
) {
    val context = LocalContext.current
    val ime = context as? IMEService
    remember(context) { KeyThemePreferences.load(context) }
    val appearanceEnabled = KeywiAppearancePreferences.currentEnabled
    val theme = KeyThemePreferences.current
    val density = LocalDensity.current
    var advanced by remember { mutableStateOf(false) }
    var shifted by remember { mutableStateOf(false) }
    var symbols by remember { mutableStateOf(false) }
    val designMode = if (symbols) com.dessalines.thumbkey.utils.KeyboardMode.NUMERIC else if (shifted) com.dessalines.thumbkey.utils.KeyboardMode.SHIFTED else com.dessalines.thumbkey.utils.KeyboardMode.MAIN
    var ctrl by remember { mutableStateOf(false) }
    var alt by remember { mutableStateOf(false) }
    var momentaryAdvanced by remember { mutableStateOf(false) }

    val rows =
        when {
            advanced || momentaryAdvanced -> MachineCutLayouts.touchAdvanced.rows
            symbols -> MachineCutLayouts.touchSymbols.rows
            else -> layout.rows
        }

    fun keyCode(code: Int) {
        val connection = ime?.currentInputConnection ?: return
        val meta = (if (ctrl) KeyEvent.META_CTRL_ON else 0) or (if (alt) KeyEvent.META_ALT_ON else 0)
        connection.sendKeyEvent(KeyEvent(0L, 0L, KeyEvent.ACTION_DOWN, code, 0, meta))
        connection.sendKeyEvent(KeyEvent(0L, 0L, KeyEvent.ACTION_UP, code, 0, meta))
        ctrl = false
        alt = false
    }

    fun press(label: String) {
        when (label) {
            "⚙" -> {
                context.startActivity(
                    Intent(context, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra("startRoute", "settings"),
                )
            }

            "⌘" -> {
                advanced = !advanced
                momentaryAdvanced = false
            }

            "?123" -> {
                symbols = true
            }

            "ABC" -> {
                symbols = false
                advanced = false
            }

            "⇧" -> {
                shifted = !shifted
            }

            "Ctrl" -> {
                ctrl = !ctrl
            }

            "Alt" -> {
                alt = !alt
            }

            "⌫" -> {
                keyCode(KeyEvent.KEYCODE_DEL)
            }

            "Del" -> {
                keyCode(KeyEvent.KEYCODE_FORWARD_DEL)
            }

            "↵" -> {
                keyCode(KeyEvent.KEYCODE_ENTER)
            }

            "Tab" -> {
                keyCode(KeyEvent.KEYCODE_TAB)
            }

            "Esc" -> {
                keyCode(KeyEvent.KEYCODE_ESCAPE)
            }

            "Win" -> {
                keyCode(KeyEvent.KEYCODE_META_LEFT)
            }

            "Home" -> {
                keyCode(KeyEvent.KEYCODE_MOVE_HOME)
            }

            "End" -> {
                keyCode(KeyEvent.KEYCODE_MOVE_END)
            }

            "PgUp" -> {
                keyCode(KeyEvent.KEYCODE_PAGE_UP)
            }

            "PgDn" -> {
                keyCode(KeyEvent.KEYCODE_PAGE_DOWN)
            }

            "Ins" -> {
                keyCode(KeyEvent.KEYCODE_INSERT)
            }

            "↑" -> {
                keyCode(KeyEvent.KEYCODE_DPAD_UP)
            }

            "↓" -> {
                keyCode(KeyEvent.KEYCODE_DPAD_DOWN)
            }

            "←" -> {
                keyCode(KeyEvent.KEYCODE_DPAD_LEFT)
            }

            "→" -> {
                keyCode(KeyEvent.KEYCODE_DPAD_RIGHT)
            }

            "☺" -> {
                ime?.currentInputConnection?.commitText("☺", 1)
            }

            "Space" -> {
                ime?.currentInputConnection?.commitText(" ", 1)
            }

            else -> {
                if (label.startsWith("F") && label.drop(1).toIntOrNull() in 1..12) {
                    keyCode(KeyEvent.KEYCODE_F1 + label.drop(1).toInt() - 1)
                } else {
                    val output = if (shifted) label.uppercase() else label
                    val connection = ime?.currentInputConnection
                    if (ctrl || alt) {
                        val code = KeyEvent.keyCodeFromString("KEYCODE_${label.uppercase()}")
                        if (code != KeyEvent.KEYCODE_UNKNOWN) keyCode(code) else connection?.commitText(output, 1)
                    } else {
                        connection?.commitText(output, 1)
                    }
                    shifted = false
                }
            }
        }
    }

    BoxWithConstraints(modifier.fillMaxWidth()) {
        val keyboardWidth = maxWidth
        val canvasWidth = with(density) { (maxWidth - 6.dp).toPx() }
        val canvasHeight = with(density) { (rows.size * 53 - 2).dp.toPx() }
        val keyRectangles =
            keyCanvasRects(
                rows.map { row ->
                    val available = keyboardWidth - 6.dp - 3.dp * (row.size - 1)
                    val total = row.sumOf { it.width.toDouble() }.toFloat()
                    row.map { cell -> with(density) { (available * (cell.width / total)).toPx() } }
                },
                rows.mapIndexed { index, _ -> with(density) { (if (index == rows.lastIndex) 48.dp else 50.dp).toPx() } },
                columnGap = with(density) { 3.dp.toPx() },
                rowGap = with(density) { 3.dp.toPx() },
            )
        Column(
            modifier = Modifier.fillMaxWidth().padding(3.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            rows.forEachIndexed { index, row ->
                val bottomRow = index == rows.lastIndex
                val height = if (bottomRow) 48.dp else 50.dp
                val availableWidth = keyboardWidth - 6.dp - 3.dp * (row.size - 1)
                val totalWeight = row.sumOf { it.width.toDouble() }.toFloat()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    row.forEachIndexed { cellIndex, cell ->
                        val label = cell.label
                        val cellWidth = availableWidth * (cell.width / totalWeight)
                        val radius = ((keyRadius / 100f) * ((cellWidth.value + height.value) / 4f)).dp
                        val x =
                            availableWidth * (row.take(cellIndex).sumOf { it.width.toDouble() }.toFloat() / totalWeight) + 3.dp * cellIndex
                        val active =
                            when (label) {
                                "⇧" -> shifted
                                "Ctrl" -> ctrl
                                "Alt" -> alt
                                "⌘" -> advanced || momentaryAdvanced
                                else -> false
                            }
                        HkKey(
                            label = if (shifted && label.length == 1 && label[0].isLetter()) label.uppercase() else label,
                            width = cell.width,
                            height = height,
                            radius = radius,
                            borderWidth = keyBorderWidth.dp,
                            theme = theme.forKey("$index:$cellIndex", designMode),
                            groupSurface = theme.groupCanvasForKey("$index:$cellIndex", KeyGradientLayer.SURFACE, keyRectangles, designMode),
                            groupBorder = theme.groupCanvasForKey("$index:$cellIndex", KeyGradientLayer.BORDER, keyRectangles, designMode),
                            designId = "$index:$cellIndex",
                            sequenceIndex = rows.take(index).sumOf { it.size } + cellIndex,
                            appearanceEnabled = appearanceEnabled,
                            canvasWidth = canvasWidth,
                            canvasHeight = canvasHeight,
                            offsetX = with(density) { x.toPx() },
                            offsetY = with(density) { (53.dp * index).toPx() },
                            active = active,
                            effectKeyId = "hk${index}_$cellIndex",
                            xFraction = (x.value + cellWidth.value / 2f) / keyboardWidth.value,
                            yFraction = (index + 0.5f) / rows.size,
                            onClick = { press(label) },
                            onLongClick = { if (label == "⌘") momentaryAdvanced = !momentaryAdvanced },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RowScope.HkKey(
    label: String,
    width: Float,
    height: Dp,
    radius: Dp,
    borderWidth: Dp,
    theme: KeyThemeState,
    groupSurface: KeyCanvasRect?,
    groupBorder: KeyCanvasRect?,
    designId: String,
    sequenceIndex: Int,
    appearanceEnabled: Boolean,
    canvasWidth: Float,
    canvasHeight: Float,
    offsetX: Float,
    offsetY: Float,
    active: Boolean,
    effectKeyId: String,
    xFraction: Float,
    yFraction: Float,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    val context = LocalContext.current
    val feedbackMode = com.dessalines.thumbkey.effects.rememberFeedbackMode()
    val view = androidx.compose.ui.platform.LocalView.current
    val password =
        (context as? IMEService)?.let {
            com.dessalines.thumbkey.utils
                .isPasswordField(it)
        } ?: false
    var soundPressDispatched by remember { mutableStateOf(false) }
    var gifPressDispatched by remember { mutableStateOf(false) }
    var holdPendingRelease by remember { mutableStateOf(false) }

    fun effects(
        direction: String,
        phase: SoundTrigger,
    ) {
        if (phase != SoundTrigger.PRESS || !soundPressDispatched) {
            ActionSoundEngine.fire(context, direction, effectKeyId, phase)
            if (phase == SoundTrigger.PRESS) soundPressDispatched = true
        }
        if (!password && (phase != SoundTrigger.PRESS || !gifPressDispatched)) {
            ActionGifRuntime.trigger(context, effectKeyId, direction, phase.name.lowercase(), xFraction, yFraction)
            if (phase == SoundTrigger.PRESS) gifPressDispatched = true
        }
    }
    LaunchedEffect(pressed) {
        if (pressed) {
            soundPressDispatched = false
            gifPressDispatched = false
            holdPendingRelease = false
            if (feedbackMode == com.dessalines.thumbkey.effects.FeedbackMode.VIBRATE) {
                view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
            }
            val sounds = ActionSoundPreferences.get(context)
            if (sounds.selection != SoundSelection.DIRECTION && sounds.keys.isEmpty() && sounds.directions.isEmpty()) {
                ActionSoundEngine.fire(context, keyId = effectKeyId, phase = SoundTrigger.PRESS)
                soundPressDispatched = true
            }
            val gifs = ActionGifPreferences.current
            if (!password && gifs.mode != "direction" && gifs.keyAssignments.isEmpty() && gifs.directionAssignments.isEmpty()) {
                ActionGifRuntime.trigger(context, effectKeyId, "CENTER", "press", xFraction, yFraction)
                gifPressDispatched = true
            }
        } else if (holdPendingRelease) {
            holdPendingRelease = false
            effects("HOLD", SoundTrigger.RELEASE)
        }
    }
    var visual: Modifier = Modifier
    if (appearanceEnabled && theme.borderStyle == KeyBorderStyle.SHADOW) {
        visual =
            visual.shadow(
                theme.shadowElevation.dp,
                shape,
                clip = false,
                ambientColor = theme.shadowColor.copy(alpha = theme.shadowAlpha),
                spotColor = theme.shadowColor.copy(alpha = theme.shadowAlpha),
            )
    }
    visual = visual.clip(shape)
    visual =
        if (pressed || active) {
            visual.background(MaterialTheme.colorScheme.inversePrimary)
        } else if (appearanceEnabled) {
            when (theme.surfaceStyle) {
                KeySurfaceStyle.GRADIENT -> {
                    visual.keyboardGradientSlice(
                        theme.surfaceGradient,
                        groupSurface?.width ?: canvasWidth,
                        groupSurface?.height ?: canvasHeight,
                        offsetX - (groupSurface?.left ?: 0f),
                        offsetY - (groupSurface?.top ?: 0f),
                    )
                }

                KeySurfaceStyle.SOLID -> {
                    visual.background(theme.surfaceColor)
                }

                KeySurfaceStyle.NONE -> {
                    visual
                }
            }
        } else {
            visual.background(MaterialTheme.colorScheme.surface)
        }
    if (borderWidth > 0.dp) {
        visual =
            if (!appearanceEnabled) {
                visual.border(borderWidth, MaterialTheme.colorScheme.outline, shape)
            } else {
                when (theme.borderStyle) {
                    KeyBorderStyle.GRADIENT -> {
                        visual.keyboardGradientBorder(
                            theme.borderGradient,
                            borderWidth,
                            radius,
                            canvasWidth =
                                groupBorder?.width ?: if (theme.borderDistribution == GradientDistribution.SHARED) canvasWidth else 0f,
                            canvasHeight =
                                groupBorder?.height ?: if (theme.borderDistribution == GradientDistribution.SHARED) canvasHeight else 0f,
                            offsetX =
                                if (groupBorder !=
                                    null
                                ) {
                                    offsetX - groupBorder.left
                                } else if (theme.borderDistribution ==
                                    GradientDistribution.SHARED
                                ) {
                                    offsetX
                                } else {
                                    0f
                                },
                            offsetY =
                                if (groupBorder !=
                                    null
                                ) {
                                    offsetY - groupBorder.top
                                } else if (theme.borderDistribution ==
                                    GradientDistribution.SHARED
                                ) {
                                    offsetY
                                } else {
                                    0f
                                },
                            phaseDegrees =
                                if (groupBorder != null) {
                                    0f
                                } else {
                                    gradientPhaseDegrees(
                                        theme.borderDistribution,
                                        designId,
                                        sequenceIndex,
                                        theme.borderPhaseStepDegrees,
                                        theme.borderRandomSeed,
                                    )
                                },
                        )
                    }

                    KeyBorderStyle.SOLID -> {
                        visual.border(borderWidth, theme.borderColor, shape)
                    }

                    KeyBorderStyle.NONE, KeyBorderStyle.SHADOW -> {
                        visual
                    }
                }
            }
    }
    Box(
        modifier =
            Modifier
                .weight(width)
                .height(height)
                .then(visual)
                .semantics {
                    if (label == "⚙") contentDescription = "Keywi settings"
                }.combinedClickable(
                    interactionSource = interactions,
                    indication = null,
                    hapticFeedbackEnabled = false,
                    onClick = {
                        effects("CENTER", SoundTrigger.PRESS)
                        if (!TaskerKeyBindings.dispatch(context, effectKeyId, "CENTER")) onClick()
                        effects("CENTER", SoundTrigger.RELEASE)
                    },
                    onLongClick = {
                        effects("HOLD", SoundTrigger.PRESS)
                        if (!TaskerKeyBindings.dispatch(context, effectKeyId, "HOLD")) onLongClick()
                        holdPendingRelease = true
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        KeyAccentOverlay(pressed, Modifier.matchParentSize(), suppressed = password)
        val legendColor = (if (appearanceEnabled) theme.glyphColor else null) ?: if (pressed || active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        TaskerCommandLegend(effectKeyId, "CENTER", 24.dp, legendColor) {
            Text(label, color = legendColor)
        }
        Box(Modifier.matchParentSize().padding(3.dp), contentAlignment = Alignment.BottomEnd) {
            TaskerCommandLegend(effectKeyId, "HOLD", 12.dp, legendColor) {}
        }
    }
}
