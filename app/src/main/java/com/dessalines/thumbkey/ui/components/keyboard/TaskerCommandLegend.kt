package com.dessalines.thumbkey.ui.components.keyboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.dessalines.thumbkey.effects.TaskerCommandDisplay
import com.dessalines.thumbkey.effects.TaskerCommandDisplayKind
import com.dessalines.thumbkey.effects.TaskerCommandImages
import com.dessalines.thumbkey.effects.TaskerKeyBindings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Display overrides never change the original action; absence/failure keeps the original legend. */
@Composable
fun TaskerCommandLegend(
    keyId: String,
    direction: String,
    size: Dp,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    fallback: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val revision = TaskerKeyBindings.revision
    val display = remember(context, keyId, direction, revision) { TaskerKeyBindings.get(context, keyId, direction)?.display }
    if (display != null) TaskerCommandSymbol(display, size, tint, fallback = fallback) else fallback()
}

@Composable
fun TaskerCommandSymbol(
    display: TaskerCommandDisplay,
    size: Dp,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
    fallback: @Composable () -> Unit = {},
) {
    if (!display.valid()) {
        fallback()
        return
    }
    when (display.kind) {
        TaskerCommandDisplayKind.TEXT -> {
            Text(
                display.value,
                color = tint,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * .75f).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = modifier.widthIn(max = size * 2f),
            )
        }

        TaskerCommandDisplayKind.ICON -> {
            val vector =
                when (display.value) {
                    "settings" -> Icons.Outlined.Settings
                    "bolt" -> Icons.Outlined.Bolt
                    "play" -> Icons.Outlined.PlayArrow
                    "home" -> Icons.Outlined.Home
                    "favorite" -> Icons.Outlined.FavoriteBorder
                    else -> Icons.Outlined.Code
                }
            Icon(
                vector,
                contentDescription = "Tasker ${TaskerCommandImages.icons[display.value]} symbol",
                tint = tint,
                modifier = modifier.size(size),
            )
        }

        TaskerCommandDisplayKind.IMAGE -> {
            val context = LocalContext.current
            val image by produceState<ImageBitmap?>(null, display.value) {
                value =
                    withContext(
                        Dispatchers.IO,
                    ) { runCatching { TaskerCommandImages.load(context, display.value)?.asImageBitmap() }.getOrNull() }
            }
            image?.let {
                Image(
                    it,
                    contentDescription = "Custom Tasker command image",
                    modifier = modifier.size(size),
                    contentScale = ContentScale.Fit,
                )
            }
                ?: fallback()
        }
    }
}
