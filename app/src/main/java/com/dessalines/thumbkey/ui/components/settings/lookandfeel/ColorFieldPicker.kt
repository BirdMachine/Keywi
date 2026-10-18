package com.dessalines.thumbkey.ui.components.settings.lookandfeel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/** A hue/saturation disc, with brightness controlled independently. */
@Composable
internal fun ColorFieldPicker(
    hue: Float,
    saturation: Float,
    value: Float,
    onColorChange: (Float, Float, Float) -> Unit,
) {
    val latestOnChange by rememberUpdatedState(onColorChange)
    val latestValue by rememberUpdatedState(value)
    val latestHue by rememberUpdatedState(hue)
    val latestSaturation by rememberUpdatedState(saturation)
    Box(Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Canvas(
            Modifier
                .sizeIn(maxWidth = 272.dp, maxHeight = 272.dp)
                .fillMaxWidth()
                .aspectRatio(1f)
                .semantics {
                    contentDescription = "Hue and saturation disc"
                    stateDescription = "Hue ${hue.roundToInt()} degrees, saturation ${(saturation * 100).roundToInt()} percent"
                    customActions =
                        listOf(
                            CustomAccessibilityAction("Increase hue") {
                                latestOnChange((latestHue + 15f) % 360f, latestSaturation, latestValue)
                                true
                            },
                            CustomAccessibilityAction("Decrease hue") {
                                latestOnChange((latestHue + 345f) % 360f, latestSaturation, latestValue)
                                true
                            },
                            CustomAccessibilityAction("Increase saturation") {
                                latestOnChange(latestHue, (latestSaturation + .05f).coerceAtMost(1f), latestValue)
                                true
                            },
                            CustomAccessibilityAction("Decrease saturation") {
                                latestOnChange(latestHue, (latestSaturation - .05f).coerceAtLeast(0f), latestValue)
                                true
                            },
                        )
                }.pointerInput(Unit) {
                    detectTapGestures { point ->
                        val hs =
                            discSelection(point.x, point.y, size.width / 2f, size.height / 2f, size.width / 2f - 14.dp.toPx(), latestHue)
                        latestOnChange(hs.first, hs.second, latestValue)
                    }
                }.pointerInput(Unit) {
                    fun select(point: Offset) {
                        val hs =
                            discSelection(point.x, point.y, size.width / 2f, size.height / 2f, size.width / 2f - 14.dp.toPx(), latestHue)
                        latestOnChange(hs.first, hs.second, latestValue)
                    }
                    detectDragGestures(onDragStart = { select(it) }, onDrag = { change, _ ->
                        select(change.position)
                        change.consume()
                    })
                },
        ) {
            val radius = size.minDimension / 2f - 14.dp.toPx()
            val spectrum = listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
            drawCircle(Brush.sweepGradient(spectrum, center), radius)
            drawCircle(Brush.radialGradient(listOf(Color.White, Color.Transparent), center, radius), radius)
            drawCircle(Color.Black.copy(alpha = 1f - value), radius)
            drawCircle(Color.White.copy(alpha = .45f), radius, style = Stroke(1.dp.toPx()))
            val angle = Math.toRadians(hue.toDouble())
            val point = center + Offset(cos(angle).toFloat(), sin(angle).toFloat()) * radius * saturation
            drawLine(Color.Black, point - Offset(15.dp.toPx(), 0f), point + Offset(15.dp.toPx(), 0f), 3.dp.toPx())
            drawLine(Color.Black, point - Offset(0f, 15.dp.toPx()), point + Offset(0f, 15.dp.toPx()), 3.dp.toPx())
            drawCircle(Color.Black, 10.dp.toPx(), point, style = Stroke(4.dp.toPx()))
            drawCircle(Color.White, 10.dp.toPx(), point, style = Stroke(2.dp.toPx()))
        }
    }
}

/** Shared by previews and the opacity track so transparent colors never look opaque. */
internal fun DrawScope.drawTransparencyGrid(
    origin: Offset = Offset.Zero,
    area: Size = size,
) {
    val cell = 8.dp.toPx()
    var y = 0f
    var row = 0
    while (y < area.height) {
        var x = 0f
        var column = 0
        while (x < area.width) {
            drawRect(
                if ((row + column) % 2 ==
                    0
                ) {
                    Color(0xFFBFC1CC)
                } else {
                    Color(0xFF747789)
                },
                origin + Offset(x, y),
                Size(
                    minOf(cell, area.width - x),
                    minOf(
                        cell,
                        area.height - y,
                    ),
                ),
            )
            x += cell
            column++
        }
        y += cell
        row++
    }
}

@Composable
internal fun ChromaStrip(
    label: String,
    value: Float,
    colors: List<Color>,
    checkerboard: Boolean = false,
    onChange: (Float) -> Unit,
) {
    val latestOnChange by rememberUpdatedState(onChange)
    val latestValue by rememberUpdatedState(value)
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .semantics {
                contentDescription = label
                progressBarRangeInfo = ProgressBarRangeInfo(value, 0f..1f)
                setProgress {
                    latestOnChange(it.coerceIn(0f, 1f))
                    true
                }
            }.onKeyEvent {
                if (it.type == KeyEventType.KeyDown &&
                    it.key in listOf(Key.DirectionLeft, Key.DirectionRight, Key.DirectionUp, Key.DirectionDown)
                ) {
                    val delta = if (it.key == Key.DirectionLeft || it.key == Key.DirectionDown) -.01f else .01f
                    latestOnChange((latestValue + delta).coerceIn(0f, 1f))
                    true
                } else {
                    false
                }
            }.focusable()
            .pointerInput(Unit) {
                detectTapGestures { latestOnChange(stripSelection(it.x, size.width.toFloat(), 12.dp.toPx())) }
            }.pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { latestOnChange(stripSelection(it.x, size.width.toFloat(), 12.dp.toPx())) },
                    onDrag = {
                        change,
                        _,
                        ->
                        latestOnChange(stripSelection(change.position.x, size.width.toFloat(), 12.dp.toPx()))
                        change.consume()
                    },
                )
            },
    ) {
        val inset = 12.dp.toPx()
        val origin = Offset(inset, 13.dp.toPx())
        val area = Size((size.width - 2 * inset).coerceAtLeast(1f), 22.dp.toPx())
        if (checkerboard) drawTransparencyGrid(origin, area)
        drawRect(Brush.horizontalGradient(colors, startX = inset, endX = size.width - inset), origin, area)
        drawRect(Color.White.copy(alpha = .4f), origin, area, style = Stroke(1.dp.toPx()))
        val point = Offset(inset + value * area.width, size.height / 2f)
        drawCircle(Color.Black, 11.dp.toPx(), point)
        drawCircle(Brush.horizontalGradient(colors), 8.dp.toPx(), point)
        drawCircle(Color.White, 10.dp.toPx(), point, style = Stroke(2.dp.toPx()))
    }
}
