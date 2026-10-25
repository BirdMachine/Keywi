package com.dessalines.thumbkey.ui.components.keyboard

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.ui.theme.Typography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

internal data class ToolPageStyle(
    val dark: Boolean,
    val artwork: ImageBitmap? = null,
    val backdrop: BackdropThemeState? = null,
) {
    val background get() = if (dark) Color(0xFF30343D) else Color(0xFFE1E5EB)
    val well get() = if (dark) Color(0xFF1D2129) else Color(0xFFF1F3F6)
    val text get() = if (dark) Color(0xFFF2F3F7) else Color(0xFF20242C)
    val muted get() = if (dark) Color(0xFFBCC4D0) else Color(0xFF505A68)
}

internal val LocalToolPageStyle = staticCompositionLocalOf<ToolPageStyle?> { null }

/** Stable opaque chrome, with a single still image shared by all key wells on the page. */
@Composable
internal fun ToolPageTheme(content: @Composable () -> Unit) {
    if (LocalToolPageStyle.current != null) {
        content()
        return
    }
    val context = LocalContext.current
    var backdrop by remember { mutableStateOf(BackdropThemePreferences.load(context)) }
    DisposableEffect(context) {
        val prefs = context.getSharedPreferences("birdie_backdrop_theme", Context.MODE_PRIVATE)
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            backdrop = BackdropThemePreferences.load(context)
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    val media = backdrop.mediaUri.takeIf { backdrop.mode == BackdropMode.IMAGE || backdrop.mode == BackdropMode.GIF }
    val artwork by produceState<ImageBitmap?>(null, media) {
        value = null
        value = withContext(Dispatchers.IO) {
            runCatching {
                media?.let {
                    val uri = Uri.parse(it)
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(uri)?.use { input -> BitmapFactory.decodeStream(input, null, bounds) }
                    var sample = 1
                    while (bounds.outWidth / sample > 768 || bounds.outHeight / sample > 768) sample *= 2
                    val options = BitmapFactory.Options().apply { inSampleSize = sample }
                    context.contentResolver.openInputStream(uri)?.use { input -> BitmapFactory.decodeStream(input, null, options)?.asImageBitmap() }
                }
            }.getOrNull()
        }
    }
    val style = ToolPageStyle(MaterialTheme.colorScheme.background.luminance() < .5f, artwork, backdrop)
    val scheme = if (style.dark) darkColorScheme() else lightColorScheme()
    val colors = scheme.copy(
        background = style.background,
        surface = style.background,
        surfaceContainer = style.well,
        onSurface = style.text,
        onBackground = style.text,
        onSurfaceVariant = style.muted,
    )
    CompositionLocalProvider(LocalToolPageStyle provides style, LocalContentColor provides style.text) {
        MaterialTheme(colorScheme = colors, typography = Typography, content = content)
    }
}

private val grain by lazy {
    val random = Random(2401)
    val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
    for (y in 0 until 64) for (x in 0 until 64) {
        val alpha = random.nextInt(8, 34)
        bitmap.setPixel(x, y, android.graphics.Color.argb(alpha, 255, 255, 255))
    }
    bitmap.asImageBitmap()
}

@Composable
internal fun Modifier.toolPageBackground(): Modifier {
    val style = LocalToolPageStyle.current ?: ToolPageStyle(isSystemInDarkTheme())
    return drawWithCache {
        val noise = ShaderBrush(ImageShader(grain, TileMode.Repeated, TileMode.Repeated))
        onDrawBehind {
            drawRect(style.background)
            drawRect(noise, alpha = if (style.dark) .16f else .1f)
        }
    }
}

internal fun toolFill(theme: KeyThemeState, style: ToolPageStyle): KeyboardBackdrop = when (theme.surfaceStyle) {
    KeySurfaceStyle.GRADIENT -> theme.surfaceGradient.copy(stops = theme.surfaceGradient.stops.map {
        it.copy(color = lerp(style.well, it.color.copy(alpha = 1f), .24f * it.color.alpha))
    })
    KeySurfaceStyle.SOLID -> KeyboardBackdrop.fromColors(listOf(lerp(style.well, theme.surfaceColor.copy(alpha = 1f), .24f * theme.surfaceColor.alpha)))
    KeySurfaceStyle.NONE -> KeyboardBackdrop.fromColors(listOf(style.well))
}

internal fun toolEdge(theme: KeyThemeState): KeyboardBackdrop? = when (theme.borderStyle) {
    KeyBorderStyle.GRADIENT -> theme.borderGradient
    KeyBorderStyle.SOLID -> KeyboardBackdrop.fromColors(listOf(theme.borderColor))
    KeyBorderStyle.SHADOW, KeyBorderStyle.NONE -> null
}

/** Glow is drawn outside the clip; artwork, fill and inset shadows remain inside the well. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ToolPageSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    radius: Int = 10,
    designId: String = "tool",
    sequenceIndex: Int = 0,
    onDoubleClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val style = LocalToolPageStyle.current ?: ToolPageStyle(MaterialTheme.colorScheme.background.luminance() < .5f)
    val theme = KeyThemePreferences.current
    val shape = RoundedCornerShape(radius.dp)
    var position by remember { mutableStateOf(Offset.Zero) }
    var rootSize by remember { mutableStateOf(Size.Zero) }
    val shared = theme.borderDistribution == GradientDistribution.SHARED
    val phase = gradientPhaseDegrees(theme.borderDistribution, designId, sequenceIndex, theme.borderPhaseStepDegrees, theme.borderRandomSeed)
    val edge = toolEdge(theme)
    val fill = toolFill(theme, style)
    val outer = modifier.heightIn(min = 48.dp).onGloballyPositioned {
        position = it.positionInRoot()
        val size = it.findRootCoordinates().size
        rootSize = Size(size.width.toFloat(), size.height.toFloat())
    }.drawWithCache {
        val brush = edge?.brush(
            if (shared) rootSize.width else size.width,
            if (shared) rootSize.height else size.height,
            if (shared) position else Offset.Zero,
            phase,
        )
        onDrawBehind {
            if (selected && brush != null) {
                // A bounded halo works on API 24+, without animated blur or expensive render effects.
                for (spread in 6 downTo 1) {
                    val pad = spread.dp.toPx() * .6f
                    drawRoundRect(
                        brush, topLeft = Offset(-pad, -pad), size = Size(size.width + pad * 2, size.height + pad * 2),
                        cornerRadius = CornerRadius(radius.dp.toPx() + pad),
                        style = Stroke((1 + spread * .6f).dp.toPx()), alpha = .018f,
                    )
                }
            }
        }
    }
    Box(outer) {
        Box(
            Modifier.clip(shape).drawWithCache {
                val fillBrush = fill.brush(size.width, size.height)
                val edgeBrush = edge?.brush(
                    if (shared) rootSize.width else size.width,
                    if (shared) rootSize.height else size.height,
                    if (shared) position else Offset.Zero,
                    phase,
                )
                val topShade = Brush.verticalGradient(listOf(Color.Black.copy(alpha = if (style.dark) .42f else .13f), Color.Transparent), endY = 7.dp.toPx())
                val bottomLight = Brush.verticalGradient(listOf(Color.Transparent, Color.White.copy(alpha = if (style.dark) .09f else .7f)))
                onDrawBehind {
                    drawRect(style.well)
                    val media = style.artwork
                    val backdrop = style.backdrop
                    if (media != null && backdrop != null) {
                        val scale = maxOf(size.width / media.width, size.height / media.height)
                        val width = (media.width * scale).toInt().coerceAtLeast(1)
                        val height = (media.height * scale).toInt().coerceAtLeast(1)
                        drawImage(media, dstOffset = IntOffset((size.width.toInt() - width) / 2, (size.height.toInt() - height) / 2), dstSize = IntSize(width, height), alpha = .14f * backdrop.opacity)
                    } else if (backdrop?.mode == BackdropMode.COLORFUL) {
                        backdrop.toBackdrop().brush(size.width, size.height)?.let { drawRect(it, alpha = .10f * backdrop.opacity) }
                    }
                    fillBrush?.let { drawRect(it, alpha = .80f) }
                    drawRect(topShade)
                    clipRect(top = size.height - 2.dp.toPx()) { drawRoundRect(bottomLight, cornerRadius = CornerRadius(radius.dp.toPx()), style = Stroke(2.dp.toPx())) }
                    if (edgeBrush != null) {
                        drawRoundRect(edgeBrush, topLeft = Offset(.5.dp.toPx(), .5.dp.toPx()), size = Size(size.width - 1.dp.toPx(), size.height - 1.dp.toPx()), cornerRadius = CornerRadius(radius.dp.toPx()), style = Stroke(1.dp.toPx()), alpha = if (selected) .95f else .16f)
                    }
                }
            }.heightIn(min = 48.dp).semantics { this.selected = selected }.combinedClickable(
                onClick = onClick, onDoubleClick = onDoubleClick, hapticFeedbackEnabled = false,
            ),
        ) { content() }
    }
}
