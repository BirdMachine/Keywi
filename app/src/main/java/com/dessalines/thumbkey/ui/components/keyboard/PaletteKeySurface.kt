package com.dessalines.thumbkey.ui.components.keyboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp

/** Shared key-surface treatment for input workspace controls and preview cards. */
@Composable
internal fun PaletteKeySurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    radius: Int = 12,
    designId: String? = null,
    sequenceIndex: Int? = null,
    selected: Boolean = false,
    content: @Composable () -> Unit,
) {
    if (LocalToolPageStyle.current != null) {
        ToolPageSurface(
            onClick = onClick, modifier = modifier, radius = radius, selected = selected,
            designId = designId ?: "tool-control", sequenceIndex = sequenceIndex ?: 0, content = content,
        )
        return
    }
    val theme = KeyThemePreferences.current
    val shape = RoundedCornerShape(radius.dp)
    var position by remember { mutableStateOf(Offset.Zero) }
    var initialPosition by remember { mutableStateOf<Offset?>(null) }
    var rootSize by remember { mutableStateOf(Size.Zero) }
    var localSize by remember { mutableStateOf(Size.Zero) }
    val shared = theme.borderDistribution == GradientDistribution.SHARED
    // Logical IDs keep random phase stable; shared sampling follows the current root position.
    val stablePosition = initialPosition ?: Offset.Zero
    val column = (stablePosition.x / localSize.width.coerceAtLeast(1f)).toInt()
    val row = (stablePosition.y / localSize.height.coerceAtLeast(1f)).toInt()
    val phase =
        gradientPhaseDegrees(
            theme.borderDistribution,
            designId ?: "palette:$row:$column",
            sequenceIndex ?: (row * 32 + column),
            theme.borderPhaseStepDegrees,
            theme.borderRandomSeed,
        )
    val decorated =
        modifier
            .onGloballyPositioned { coordinates ->
                position = coordinates.positionInRoot()
                if (initialPosition == null) initialPosition = position
                val root = coordinates.findRootCoordinates().size
                rootSize = Size(root.width.toFloat(), root.height.toFloat())
                localSize = Size(coordinates.size.width.toFloat(), coordinates.size.height.toFloat())
            }.clip(shape)
            .then(
                if (theme.surfaceStyle == KeySurfaceStyle.GRADIENT) {
                    Modifier.keyboardGradientBackground(theme.surfaceGradient)
                } else {
                    Modifier
                },
            ).then(
                if (theme.borderStyle == KeyBorderStyle.GRADIENT) {
                    Modifier.keyboardGradientBorder(
                        backdrop = theme.borderGradient,
                        width = 1.dp,
                        radius = radius.dp,
                        canvasWidth = if (shared) rootSize.width else 0f,
                        canvasHeight = if (shared) rootSize.height else 0f,
                        offsetX = if (shared) position.x else 0f,
                        offsetY = if (shared) position.y else 0f,
                        phaseDegrees = phase,
                    )
                } else {
                    Modifier
                },
            )
    Surface(
        onClick = onClick,
        modifier = decorated,
        shape = shape,
        color =
            when (theme.surfaceStyle) {
                KeySurfaceStyle.SOLID -> theme.surfaceColor
                KeySurfaceStyle.GRADIENT -> Color.Transparent
                KeySurfaceStyle.NONE -> MaterialTheme.colorScheme.surface.copy(alpha = 0.24f)
            },
        contentColor = MaterialTheme.colorScheme.onSurface,
        border =
            if (theme.borderStyle == KeyBorderStyle.SOLID) {
                BorderStroke(1.dp, theme.borderColor)
            } else {
                null
            },
        shadowElevation = if (theme.borderStyle == KeyBorderStyle.SHADOW) theme.shadowElevation.dp else 0.dp,
    ) {
        Box { content() }
    }
}
