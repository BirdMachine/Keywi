package com.dessalines.thumbkey.ui.components.keyboard

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.dessalines.thumbkey.R
import kotlin.math.roundToInt

enum class ToolbarIconStyle(
    val title: String,
) {
    AUTOMATIC("Automatic contrast"),
    LIGHT("Light kiwi"),
    DARK("Dark kiwi"),
}

internal fun toolbarIconIsLight(
    style: ToolbarIconStyle,
    background: Color,
): Boolean =
    when (style) {
        ToolbarIconStyle.LIGHT -> true
        ToolbarIconStyle.DARK -> false
        ToolbarIconStyle.AUTOMATIC -> background.compositeOver(Color.Black).luminance() < .5f
    }

/** Draw each region of Birdie's supplied sheet directly; the source artwork is unchanged. */
@Composable
internal fun ToolbarKiwiIcon(
    light: Boolean,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
) {
    val resources = LocalContext.current.resources
    val image =
        remember(resources) {
            BitmapFactory
                .decodeResource(
                    resources,
                    R.drawable.toolbar_kiwi_icons,
                    BitmapFactory.Options().apply {
                        inSampleSize = 4
                        inScaled = false
                    },
                ).asImageBitmap()
        }
    Canvas(modifier) {
        val scaleX = image.width / 1774f
        val scaleY = image.height / 887f
        val left = if (light) 925f else 50f
        drawImage(
            image,
            srcOffset = IntOffset((left * scaleX).roundToInt(), (35f * scaleY).roundToInt()),
            srcSize = IntSize((800f * scaleX).roundToInt(), (808f * scaleY).roundToInt()),
            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
            alpha = alpha,
        )
    }
}
