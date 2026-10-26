package com.dessalines.thumbkey.ui.components.keyboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.utils.SketchRecognizer
import io.github.toldry.glyphsketch.Point
import io.github.toldry.glyphsketch.Recognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Compact reusable handwriting surface for Keywi's tool-page and future Gem Cut presentation. */
@Composable
fun SketchPad(onCommit: (String) -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    val recognizer by produceState<Recognizer?>(null) {
        value = runCatching { SketchRecognizer.get(context) }.getOrNull()
    }
    var strokes by remember { mutableStateOf<List<List<Offset>>>(emptyList()) }
    var current by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var candidates by remember { mutableStateOf<List<Int>>(emptyList()) }
    LaunchedEffect(strokes, recognizer) {
        val engine = recognizer ?: return@LaunchedEffect
        if (strokes.isEmpty()) {
            candidates = emptyList()
        } else {
            val points = strokes.map { stroke -> stroke.map { Point(it.x.toDouble(), it.y.toDouble()) } }
            val result = withContext(Dispatchers.Default) {
                engine.recognize(points, language = null, tiles = 12, characters = 0, accept = SketchRecognizer.displayable)
            }
            candidates = result.tiles.map { it.representative }.distinct()
        }
    }
    Column {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = { strokes = strokes.dropLast(1) }) { Text("Undo") }
            TextButton(onClick = { strokes = emptyList(); current = emptyList() }) { Text("Clear") }
            TextButton(onClick = onClose) { Text("ABC") }
        }
        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp).padding(8.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { point -> current = listOf(point) },
                    onDragEnd = { if (current.isNotEmpty()) strokes = strokes + listOf(current); current = emptyList() },
                    onDragCancel = { current = emptyList() },
                    onDrag = { change, _ -> current = current + change.position; change.consume() },
                )
            }) {
            for (stroke in strokes + listOf(current)) {
                if (stroke.isEmpty()) continue
                val path = Path().apply {
                    moveTo(stroke.first().x, stroke.first().y)
                    stroke.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(path, color = androidx.compose.ui.graphics.Color(0xFFDA85BA),
                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxWidth()) {
            candidates.take(8).forEach { codePoint ->
                TextButton(onClick = { onCommit(String(Character.toChars(codePoint))) }) {
                    Text(String(Character.toChars(codePoint)))
                }
            }
            if (recognizer == null) Text("Loading handwriting…")
        }
    }
}
