package com.dessalines.thumbkey.ui.components.settings

import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dessalines.thumbkey.ui.components.keyboard.BackdropMode
import com.dessalines.thumbkey.ui.components.keyboard.BackdropThemePreferences
import com.dessalines.thumbkey.ui.components.keyboard.BackdropThemeState
import com.dessalines.thumbkey.ui.components.keyboard.KeyBorderStyle
import com.dessalines.thumbkey.ui.components.keyboard.KeyThemePreferences
import com.dessalines.thumbkey.ui.components.keyboard.KeyThemeState
import com.dessalines.thumbkey.ui.components.keyboard.KeywiPowerMode
import com.dessalines.thumbkey.ui.components.keyboard.KeywiPowerPreferences
import com.dessalines.thumbkey.ui.components.keyboard.keyboardGradientBackground
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

/** Shared artwork for modern settings, including the advanced theme editor. Legacy controls stay isolated. */
fun usesSettingsDrip(route: String?): Boolean =
    route in
        setOf(
            "settings",
            "advancedLookAndFeel",
            "advancedLookAndFeelEditor",
            "advancedPowerOptions",
            "clipboardSettings",
            "backupAndRestore",
            "about",
            "otherSettings",
            "advancedInput",
            "keypressEffects",
        )

@androidx.annotation.ChecksSdkIntAtLeast(api = 31)
fun settingsOpticsEnabled(
    mode: KeywiPowerMode,
    api: Int,
): Boolean = mode == KeywiPowerMode.UNBRIDLED && api >= 31

data class SettingsDripStyle(
    val backdrop: BackdropThemeState,
    val image: ImageBitmap?,
    val animated: Drawable?,
    val frame: Int,
    val keys: KeyThemeState,
    val power: KeywiPowerMode,
    val optics: androidx.compose.ui.graphics.RenderEffect?,
)

val LocalSettingsDrip = staticCompositionLocalOf<SettingsDripStyle?> { null }

@Composable
fun rememberSettingsDrip(): SettingsDripStyle {
    val context = LocalContext.current
    var revision by remember { mutableIntStateOf(0) }
    DisposableEffect(context) {
        val stores =
            listOf("birdie_backdrop_theme", "key_theme_preferences", "keywi_power_preferences")
                .map { context.getSharedPreferences(it, 0) }
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> revision++ }
        stores.forEach { it.registerOnSharedPreferenceChangeListener(listener) }
        onDispose { stores.forEach { it.unregisterOnSharedPreferenceChangeListener(listener) } }
    }
    val backdrop = remember(revision) { BackdropThemePreferences.load(context) }
    val keys = remember(revision) { KeyThemePreferences.load(context) }
    val power = remember(revision) { KeywiPowerPreferences.load(context) }
    // One bounded media decode per destination, shared by every panel; no decoder per row.
    val media = backdrop.mediaUri.takeIf { backdrop.mode == BackdropMode.IMAGE || backdrop.mode == BackdropMode.GIF }
    val image by produceState<ImageBitmap?>(null, media) {
        value = null
        value =
            withContext(Dispatchers.IO) {
                runCatching {
                    val uri = Uri.parse(media ?: return@withContext null)
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                    val options =
                        BitmapFactory.Options().apply {
                            var sample = 1
                            while (max(bounds.outWidth, bounds.outHeight) / sample > 1024) sample *= 2
                            inSampleSize = sample
                        }
                    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options)?.asImageBitmap() }
                }.getOrNull()
            }
    }
    val motion = power == KeywiPowerMode.UNBRIDLED
    val animated by produceState<Drawable?>(null, media, motion) {
        value = null
        if (motion && backdrop.mode == BackdropMode.GIF && media != null && Build.VERSION.SDK_INT >= 28) {
            value =
                withContext(Dispatchers.IO) {
                    runCatching {
                        ImageDecoder.decodeDrawable(
                            ImageDecoder.createSource(context.contentResolver, Uri.parse(media)),
                        ) { decoder, info, _ ->
                            val scale = 1024f / max(info.size.width, info.size.height).coerceAtLeast(1024)
                            decoder.setTargetSize(
                                (info.size.width * scale).toInt().coerceAtLeast(1),
                                (info.size.height * scale).toInt().coerceAtLeast(1),
                            )
                        }
                    }.getOrNull()
                }
        }
    }
    var frame by remember(animated) { mutableIntStateOf(0) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(animated, owner) {
        val drawable = animated
        val handler = Handler(Looper.getMainLooper())
        val callback =
            object : Drawable.Callback {
                override fun invalidateDrawable(who: Drawable) {
                    frame++
                }

                override fun scheduleDrawable(
                    who: Drawable,
                    task: Runnable,
                    at: Long,
                ) {
                    handler.postAtTime(task, at)
                }

                override fun unscheduleDrawable(
                    who: Drawable,
                    task: Runnable,
                ) {
                    handler.removeCallbacks(task)
                }
            }
        drawable?.callback = callback

        fun playback(active: Boolean) {
            if (Build.VERSION.SDK_INT >= 28 && drawable is AnimatedImageDrawable) {
                drawable.repeatCount = AnimatedImageDrawable.REPEAT_INFINITE
                if (active) drawable.start() else drawable.stop()
            }
        }
        val observer = LifecycleEventObserver { _, _ -> playback(owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) }
        owner.lifecycle.addObserver(observer)
        playback(owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
        onDispose {
            playback(false)
            owner.lifecycle.removeObserver(observer)
            drawable?.callback = null
            handler.removeCallbacksAndMessages(null)
        }
    }
    val optics =
        remember(power) {
            if (settingsOpticsEnabled(power, Build.VERSION.SDK_INT)) {
                runCatching {
                    val blur = RenderEffect.createBlurEffect(3f, 3f, Shader.TileMode.CLAMP)
                    if (Build.VERSION.SDK_INT >= 33) {
                        val shader =
                            RuntimeShader(
                                """
                                uniform shader artwork;
                                half4 main(float2 p) {
                                    float2 bend = float2(sin(p.y * 0.018), cos(p.x * 0.014)) * 5.0;
                                    return artwork.eval(p + bend);
                                }
                                """.trimIndent(),
                            )
                        RenderEffect
                            .createChainEffect(RenderEffect.createRuntimeShaderEffect(shader, "artwork"), blur)
                            .asComposeRenderEffect()
                    } else {
                        blur.asComposeRenderEffect()
                    }
                }.getOrNull()
            } else {
                null
            }
        }
    return SettingsDripStyle(backdrop, image, animated, frame, keys, power, optics)
}

private fun DrawScope.drawThemeImage(
    image: ImageBitmap,
    alpha: Float,
) {
    val scale = max(size.width / image.width, size.height / image.height)
    val width = (image.width * scale).toInt().coerceAtLeast(1)
    val height = (image.height * scale).toInt().coerceAtLeast(1)
    drawImage(
        image,
        dstOffset = IntOffset((size.width.toInt() - width) / 2, (size.height.toInt() - height) / 2),
        dstSize = IntSize(width, height),
        alpha = alpha,
    )
}

private fun DrawScope.drawThemeArtwork(
    style: SettingsDripStyle,
    alpha: Float,
) {
    // Cover-cropped portrait artwork extends well beyond short surfaces such as the
    // top app bar. Clip the artwork itself: not every caller owns a clipping layer.
    // Without this, the bar (drawn above scrolling content) paints raw media over rows.
    clipRect {
        // Reading the frame on composition invalidates shared artwork surfaces during playback.
        val drawable = style.animated
        if (drawable == null) {
            style.image?.let { drawThemeImage(it, alpha) }
        } else {
            val width = drawable.intrinsicWidth.coerceAtLeast(1)
            val height = drawable.intrinsicHeight.coerceAtLeast(1)
            val scale = max(size.width / width, size.height / height)
            drawIntoCanvas { canvas ->
                val native = canvas.nativeCanvas
                val save = native.save()
                native.translate((size.width - width * scale) / 2f, (size.height - height * scale) / 2f)
                native.scale(scale, scale)
                drawable.setBounds(0, 0, width, height)
                drawable.alpha = (alpha * 255).toInt().coerceIn(0, 255)
                drawable.draw(native)
                native.restoreToCount(save)
            }
        }
    }
}

/** Only artwork is filtered. Text, focus rings and touch feedback stay on a separate layer. */
@Composable
fun SettingsDripBackdrop(
    modifier: Modifier = Modifier,
    panel: Boolean = false,
) {
    val style = LocalSettingsDrip.current ?: return
    val colors = LocalAdvancedSettingsColors.current
    val shade = if (panel) colors.surface else colors.background
    Box(modifier.background(shade)) {
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer {
                    renderEffect = style.optics
                    clip = true
                    alpha =
                        if (style.backdrop.mode == BackdropMode.COLORFUL) style.backdrop.opacity else 1f
                }.then(
                    if (style.backdrop.mode ==
                        BackdropMode.COLORFUL
                    ) {
                        Modifier.keyboardGradientBackground(style.backdrop.toBackdrop())
                    } else {
                        Modifier
                    },
                ).drawWithCache {
                    onDrawBehind {
                        drawThemeArtwork(style, style.backdrop.opacity)
                    }
                },
        )
        Box(
            Modifier.matchParentSize().background(
                Brush.verticalGradient(
                    listOf(
                        shade.copy(alpha = .75f),
                        shade.copy(alpha = if (panel) .88f else .93f),
                    ),
                ),
            ),
        )
    }
}

/** Shared first-frame artwork plus tinted, sculpted edges, without any preference writes. */
@Composable
fun Modifier.settingsDripSurface(selected: Boolean = false): Modifier {
    val style = LocalSettingsDrip.current ?: return this
    val colors = LocalAdvancedSettingsColors.current
    val edge =
        when (style.keys.borderStyle) {
            KeyBorderStyle.GRADIENT -> {
                style.keys.borderGradient.stops
                    .map { it.color }
            }

            KeyBorderStyle.NONE -> {
                listOf(colors.outline, colors.outline)
            }

            else -> {
                listOf(style.keys.borderColor, style.keys.borderColor)
            }
        }.let { if (it.size < 2) listOf(colors.outline, colors.outline) else it }
    return drawWithCache {
        val tint = Brush.verticalGradient(listOf(colors.surfaceRaised.copy(alpha = .84f), colors.surface.copy(alpha = .94f)))
        val rim = Brush.linearGradient(edge, Offset.Zero, Offset(size.width, size.height))
        val gloss = Brush.verticalGradient(listOf(Color.White.copy(alpha = .04f), Color.Transparent, Color.Black.copy(alpha = .04f)))
        onDrawBehind {
            drawRect(colors.surface)
            drawThemeArtwork(style, style.backdrop.opacity * .75f)
            if (style.backdrop.mode == BackdropMode.COLORFUL) {
                drawRect(
                    Brush.linearGradient(
                        style.backdrop.stops.map { it.color }.let {
                            if (it.size <
                                2
                            ) {
                                listOf(colors.surface, colors.surfaceRaised)
                            } else {
                                it
                            }
                        },
                    ),
                    alpha = style.backdrop.opacity,
                )
            }
            drawRect(tint)
            drawRect(gloss)
            drawLine(
                rim,
                Offset(12.dp.toPx(), 1.dp.toPx()),
                Offset(size.width - 12.dp.toPx(), 1.dp.toPx()),
                if (selected) 2.dp.toPx() else 1.dp.toPx(),
            )
            drawLine(
                Color.Black.copy(alpha = .35f),
                Offset(10.dp.toPx(), size.height - 2.dp.toPx()),
                Offset(
                    size.width - 10.dp.toPx(),
                    size.height - 2.dp.toPx(),
                ),
                2.dp.toPx(),
            )
        }
    }
}

@Composable
fun settingsDripBorder(): Brush {
    val style = LocalSettingsDrip.current
    val colors = LocalAdvancedSettingsColors.current
    val stops =
        style?.keys?.let { keys ->
            when (keys.borderStyle) {
                KeyBorderStyle.GRADIENT -> keys.borderGradient.stops.map { it.color }
                KeyBorderStyle.NONE -> listOf(colors.outline)
                else -> listOf(keys.borderColor)
            }
        } ?: listOf(colors.outline)
    return Brush.linearGradient(
        if (stops.size <
            2
        ) {
            listOf(stops.firstOrNull() ?: colors.outline, stops.firstOrNull() ?: colors.outline)
        } else {
            stops
        },
    )
}

@Composable
fun SettingsThemeBanner() {
    if (LocalSettingsDrip.current == null) return
    Column(gemSectionModifier().padding(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text("YOUR THEME, EVERYWHERE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text("A little keyboard atmosphere.", style = MaterialTheme.typography.titleLarge)
        Text("Background art · sculpted edges · your key-border colors", style = MaterialTheme.typography.bodySmall)
    }
}
