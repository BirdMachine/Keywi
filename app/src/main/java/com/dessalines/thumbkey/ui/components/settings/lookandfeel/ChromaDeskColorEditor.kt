package com.dessalines.thumbkey.ui.components.settings.lookandfeel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dessalines.thumbkey.ui.components.settings.GemButton
import com.dessalines.thumbkey.ui.components.settings.GemOutlinedButton
import com.dessalines.thumbkey.ui.components.settings.LocalSettingsDrip
import com.dessalines.thumbkey.ui.components.settings.SettingsDripBackdrop
import com.dessalines.thumbkey.ui.components.settings.rememberSettingsDrip
import kotlin.math.roundToInt

/** Every theme color uses the same transactional editor; opening it never writes preferences. */
@Composable
internal fun ColorEditor(
    label: String,
    color: Color,
    onChange: (Color) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(CutCornerShape(7.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(1.dp, MaterialTheme.colorScheme.outline, CutCornerShape(7.dp))
            .clickable(onClickLabel = "Edit $label") { open = true }
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ColorSample(color, Modifier.size(38.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(
                "${colorHex(color)} · ${(color.alpha * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
            )
        }
        Text("Edit ◇", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
    }
    if (open) {
        ChromaDeskDialog(label, color, onDismiss = { open = false }, onApply = {
            onChange(it)
            open = false
        })
    }
}

@Composable
private fun ColorSample(
    color: Color,
    modifier: Modifier,
) {
    Canvas(modifier.clip(CutCornerShape(5.dp)).border(1.dp, MaterialTheme.colorScheme.outline, CutCornerShape(5.dp))) {
        drawTransparencyGrid()
        drawRect(color)
    }
}

@Composable
internal fun ChromaDeskDialog(
    label: String,
    initial: Color,
    onDismiss: () -> Unit,
    onApply: (Color) -> Unit,
) {
    val context = LocalContext.current
    val preferences = remember(context) { context.getSharedPreferences("keywi_chroma_desk", android.content.Context.MODE_PRIVATE) }
    val recents =
        remember {
            preferences
                .getString(
                    "recent",
                    "",
                ).orEmpty()
                .split(',')
                .mapNotNull { it.toLongOrNull()?.toInt()?.let(::Color) }
                .take(7)
        }
    val artwork = rememberSettingsDrip()
    val openingHsv = remember { FloatArray(3).also { android.graphics.Color.colorToHSV(initial.toArgb(), it) } }
    var hue by remember { mutableFloatStateOf(openingHsv[0]) }
    var saturation by remember { mutableFloatStateOf(openingHsv[1]) }
    var brightness by remember { mutableFloatStateOf(openingHsv[2]) }
    var alpha by remember { mutableFloatStateOf(initial.alpha) }
    val draft = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, brightness))).copy(alpha = alpha)
    var hex by remember { mutableStateOf(colorHex(initial)) }
    var channelsExpanded by remember { mutableStateOf(false) }
    val channelErrors = remember { mutableStateMapOf<String, Boolean>() }
    var channelRevision by remember { mutableIntStateOf(0) }

    fun updateHsv(
        h: Float = hue,
        s: Float = saturation,
        v: Float = brightness,
    ) {
        hue = h
        saturation = s
        brightness = v
        hex = colorHex(Color(android.graphics.Color.HSVToColor(floatArrayOf(h, s, v))))
        channelErrors.clear()
        channelRevision++
    }

    fun updateColor(
        next: Color,
        resetChannels: Boolean = true,
    ) {
        val hsv = FloatArray(3).also { android.graphics.Color.colorToHSV(next.toArgb(), it) }
        // Hue is undefined at gray, and saturation is undefined at black: preserve intent.
        if (hsv[1] > 0f && hsv[2] > 0f) hue = hsv[0]
        if (hsv[2] > 0f) saturation = hsv[1]
        brightness = hsv[2]
        alpha = next.alpha
        hex = colorHex(next)
        if (resetChannels) {
            channelErrors.clear()
            channelRevision++
        }
    }
    val validHex = parseColorHex(hex, alpha) != null
    val shape = CutCornerShape(14.dp)
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            Column(
                Modifier
                    .padding(12.dp)
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(.94f)
                    .imePadding()
                    .clip(shape)
                    .background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.background),
                        ),
                    ).border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .75f), shape)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("◇ CHROMA DESK", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(label, style = MaterialTheme.typography.bodyMedium)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            ColorSample(initial, Modifier.fillMaxWidth().height(38.dp))
                            Text("Before", style = MaterialTheme.typography.labelSmall)
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            ColorSample(
                                draft,
                                Modifier.fillMaxWidth().height(38.dp).semantics {
                                    contentDescription =
                                        "Selected color ${colorHex(draft)}, opacity ${(alpha * 100).roundToInt()} percent"
                                },
                            )
                            Text("After", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    CompositionLocalProvider(LocalSettingsDrip provides artwork) {
                        Box(Modifier.fillMaxWidth().clip(CutCornerShape(12.dp))) {
                            SettingsDripBackdrop(Modifier.matchParentSize(), panel = true)
                            Box(Modifier.padding(8.dp)) {
                                ColorFieldPicker(hue, saturation, brightness) { h, s, v -> updateHsv(h, s, v) }
                            }
                        }
                    }
                    Text(
                        "Hue ${hue.roundToInt()}° · Saturation ${(saturation * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text("Brightness · ${(brightness * 100).roundToInt()}%", style = MaterialTheme.typography.labelMedium)
                    ChromaStrip(
                        "Brightness",
                        brightness,
                        listOf(Color.Black, Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, 1f)))),
                    ) {
                        updateHsv(v = it)
                    }
                    Text("Opacity · ${(alpha * 100).roundToInt()}%", style = MaterialTheme.typography.labelMedium)
                    ChromaStrip("Opacity", alpha, listOf(draft.copy(alpha = 0f), draft.copy(alpha = 1f)), checkerboard = true) {
                        alpha = it
                        hex =
                            colorHex(draft)
                    }
                    OutlinedTextField(
                        value = hex,
                        onValueChange = { text ->
                            parseColorHex(text, alpha)?.let { updateColor(it) }
                            hex = text
                        },
                        label = { Text("HEX") },
                        singleLine = true,
                        isError = !validHex,
                        supportingText = {
                            Text(
                                if (validHex) "RRGGBB · or AARRGGBB with opacity" else "Enter 6 hex digits, or 8 in AARRGGBB order",
                            )
                        },
                        keyboardOptions =
                            KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                keyboardType = KeyboardType.Ascii,
                            ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    GemOutlinedButton(onClick = {
                        channelsExpanded = !channelsExpanded
                    }, modifier = Modifier.fillMaxWidth()) { Text(if (channelsExpanded) "RGB channels ▴" else "RGB channels ▾") }
                    if (channelsExpanded) {
                        key(channelRevision) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("R" to draft.red, "G" to draft.green, "B" to draft.blue).forEach { (name, value) ->
                                    ChannelInput(
                                        name,
                                        (value * 255).roundToInt(),
                                        Modifier.weight(1f),
                                        onError = { channelErrors[name] = it },
                                        onChange = { channel ->
                                            updateColor(
                                                when (name) {
                                                    "R" -> draft.copy(red = channel / 255f)
                                                    "G" -> draft.copy(green = channel / 255f)
                                                    else -> draft.copy(blue = channel / 255f)
                                                },
                                                resetChannels = false,
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }
                    if (recents.isNotEmpty()) {
                        Text("Recent colors", style = MaterialTheme.typography.labelMedium)
                        // Keep every swatch a full touch target on narrow phones.
                        Row(
                            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            recents.forEach { recent ->
                                Box(
                                    Modifier
                                        .size(48.dp)
                                        .semantics {
                                            contentDescription =
                                                "Use recent ${colorHex(recent)}, opacity ${(recent.alpha * 100).roundToInt()} percent"
                                        }.clickable { updateColor(recent) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    ColorSample(recent, Modifier.fillMaxWidth().height(32.dp))
                                }
                            }
                        }
                    }
                    GemOutlinedButton(onClick = {
                        hue = openingHsv[0]
                        saturation = openingHsv[1]
                        brightness = openingHsv[2]
                        alpha = initial.alpha
                        hex = colorHex(initial)
                        channelErrors.clear()
                        channelRevision++
                    }, modifier = Modifier.fillMaxWidth()) { Text("↶ Restore original") }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GemOutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    GemButton(enabled = validHex && channelErrors.values.none { it }, onClick = {
                        val colors = (listOf(draft.toArgb()) + recents.map { it.toArgb() }).distinct().take(7)
                        preferences.edit().putString("recent", colors.joinToString(",")).apply()
                        onApply(draft)
                    }, modifier = Modifier.weight(1f)) { Text("Apply") }
                }
            }
        }
    }
}

@Composable
private fun ChannelInput(
    name: String,
    value: Int,
    modifier: Modifier,
    onError: (Boolean) -> Unit,
    onChange: (Int) -> Unit,
) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    val invalid = text.toIntOrNull()?.let { it !in 0..255 } ?: true
    OutlinedTextField(
        text,
        { next ->
            text = next
            val channel = next.toIntOrNull()?.takeIf { it in 0..255 }
            onError(channel == null)
            channel?.let(onChange)
        },
        modifier = modifier,
        label = { Text(name) },
        singleLine = true,
        isError = invalid,
        supportingText = { if (invalid) Text("0–255") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}
