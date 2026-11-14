package com.dessalines.thumbkey.ui.components.keyboard

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil
import kotlin.math.min

/** Visual bounds only: pointer input remains on the original whole key. */
@Composable
internal fun GestureLabelSlot(
    alignment: Alignment,
    horizontalInset: Dp = 0.dp,
    verticalInset: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    Layout(
        content = { Box(Modifier.clipToBounds(), contentAlignment = alignment) { content() } },
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val column = alignment.align(IntSize.Zero, IntSize(2, 0), layoutDirection).x
        val start = width * column / 3
        val end = width * (column + 1) / 3
        // Keep rounded-corner insets where possible, but reserve space for the legend.
        val inset = min(horizontalInset.roundToPx(), (end - start) / 4)
        val left = start + if (column == 0) inset else 0
        val right = end - if (column == 2) inset else 0
        val yInset = min(verticalInset.roundToPx(), height / 4)
        val child = measurables.single().measure(Constraints(maxWidth = (right - left).coerceAtLeast(0), maxHeight = (height - 2 * yInset).coerceAtLeast(0)))
        val offset = alignment.align(IntSize(child.width, child.height), IntSize(right - left, height - 2 * yInset), layoutDirection)
        layout(width, height) { child.place(left + offset.x, yInset + offset.y) }
    }
}

/** Scale the shaped text as a whole; never slice UTF-16 or re-shape individual clusters. */
@Composable
internal fun FittedGestureText(
    label: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    Layout(
        modifier = modifier.clipToBounds().clearAndSetSemantics { text = AnnotatedString(label) },
        content = {
            Text(label, style = style, softWrap = false, overflow = TextOverflow.Visible)
            Text("…", style = style, softWrap = false, overflow = TextOverflow.Visible)
        },
    ) { measurables, constraints ->
        val natural = measurables[0].measure(Constraints())
        val scale = gestureLabelScale(natural.width, natural.height, constraints.maxWidth, constraints.maxHeight)
        // Below 6sp an entire label becomes illegible. Show a whole-label placeholder instead
        // of clipping/truncating a combining sequence. TalkBack still receives the full label.
        val minimum = min(style.fontSize.toPx(), 6.sp.toPx())
        val usePlaceholder = style.fontSize.toPx() * scale < minimum
        val ellipsis = measurables[1].measure(Constraints())
        val child = if (usePlaceholder) ellipsis else natural
        val fittedScale = if (usePlaceholder) gestureLabelScale(child.width, child.height, constraints.maxWidth, constraints.maxHeight) else scale
        val width = constraints.constrainWidth(ceil(child.width * fittedScale).toInt())
        val height = constraints.constrainHeight(ceil(child.height * fittedScale).toInt())
        layout(width, height) {
            child.placeWithLayer(0, 0) {
                scaleX = fittedScale
                scaleY = fittedScale
                transformOrigin = TransformOrigin(0f, 0f)
            }
        }
    }
}

internal fun gestureLabelScale(width: Int, height: Int, maxWidth: Int, maxHeight: Int): Float =
    min(1f, min(maxWidth.toFloat() / width.coerceAtLeast(1), maxHeight.toFloat() / height.coerceAtLeast(1))).coerceAtLeast(0f)
