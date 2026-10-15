package com.dessalines.thumbkey.ui.components.settings.lookandfeel

import com.dessalines.thumbkey.ui.components.settings.gemSectionModifier
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.dessalines.thumbkey.ui.components.settings.GemButton as Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.dessalines.thumbkey.ui.components.keyboard.ThemeAssetStore
import com.dessalines.thumbkey.ui.components.keyboard.ThemeEngine
import com.dessalines.thumbkey.ui.components.keyboard.CUSTOM_THEME_ID
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dessalines.thumbkey.ui.components.keyboard.AdvancedKeyWordPreferences
import com.dessalines.thumbkey.ui.components.keyboard.BackdropMode
import com.dessalines.thumbkey.ui.components.keyboard.BackdropPreset
import com.dessalines.thumbkey.ui.components.keyboard.BackdropThemePreferences
import com.dessalines.thumbkey.ui.components.keyboard.BackdropThemeState
import com.dessalines.thumbkey.ui.components.keyboard.BackdropVisualLayer
import com.dessalines.thumbkey.ui.components.keyboard.FontPreferences
import com.dessalines.thumbkey.ui.components.keyboard.GradientLibrary
import com.dessalines.thumbkey.ui.components.keyboard.KeyBorderStyle
import com.dessalines.thumbkey.ui.components.keyboard.KeySurfaceStyle
import com.dessalines.thumbkey.ui.components.keyboard.KeyThemePreferences
import com.dessalines.thumbkey.ui.components.keyboard.KeyThemeState
import com.dessalines.thumbkey.ui.components.keyboard.KeyboardBackdrop
import com.dessalines.thumbkey.ui.components.keyboard.KeyboardGradientStop
import com.dessalines.thumbkey.ui.components.keyboard.KeywiAppearancePreferences
import com.dessalines.thumbkey.ui.components.keyboard.SavedGradient
import com.dessalines.thumbkey.ui.components.keyboard.SuggestionLozengeBorderStyle
import com.dessalines.thumbkey.ui.components.keyboard.SuggestionLozengeSurfaceStyle
import com.dessalines.thumbkey.ui.components.keyboard.SuggestionLozengeThemePreferences
import com.dessalines.thumbkey.ui.components.keyboard.SuggestionLozengeThemeState
import com.dessalines.thumbkey.ui.components.keyboard.SuggestionMotionPreferences
import com.dessalines.thumbkey.ui.components.keyboard.SuggestionMotionStyle
import com.dessalines.thumbkey.ui.components.keyboard.ToolbarBorderPreferences
import com.dessalines.thumbkey.ui.components.keyboard.ToolbarLayoutPreferences
import com.dessalines.thumbkey.ui.components.keyboard.ToolbarThemePreferences
import com.dessalines.thumbkey.ui.components.keyboard.keyboardGradientBackground
import com.dessalines.thumbkey.ui.components.settings.GemSettingsTopAppBar as SimpleTopAppBar
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedLookAndFeelScreen(navController: NavController) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var keywiEnabled by remember { mutableStateOf(KeywiAppearancePreferences.load(context)) }
    val openingTheme = remember(context) { ThemeEngine.captureCurrent(context, "Opening appearance", CUSTOM_THEME_ID) }
    var editNotice by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { SimpleTopAppBar(text = "Theme editor", navController = navController) }) { padding ->
        Column(Modifier.padding(padding).verticalScroll(scrollState).background(MaterialTheme.colorScheme.surface).imePadding()) {
            Row(gemSectionModifier(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column(Modifier.weight(1f).padding(end = 12.dp)) {
                    Text("Enable custom keyboard backgrounds", style = MaterialTheme.typography.titleSmall)
                    Text("Turn off to use Thumb-Key's original styling.", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = keywiEnabled, onCheckedChange = { keywiEnabled = it; KeywiAppearancePreferences.save(context, it) })
            }
            if (keywiEnabled) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(onClick = {
                        ThemeEngine.saveCustom(context, ThemeEngine.captureCurrent(context, "Custom", CUSTOM_THEME_ID))
                        editNotice = "Saved to Custom. Use Save as… in Theme manager to add it to the library."
                    }, modifier = Modifier.weight(1f)) { Text("Save") }
                    OutlinedButton(onClick = {
                        ThemeEngine.apply(context, openingTheme)
                        editNotice = "Reverted to the appearance when this editor opened. Reopen to refresh the sliders."
                    }, modifier = Modifier.weight(1f)) { Text("Revert") }
                }
                editNotice?.let { Text(it, Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodySmall) }
                KeywiThemeTestBench()
                Text("✦ Live theme tuning", Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium)
                Text("Use the test keyboard in Theme manager to inspect the actual geometry.",
                    Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodySmall)
                ThemeEditorFold("Keyboard background", "Image, GIF, opacity and gradients", true) {
                    SurfaceThemeSection("Main backdrop", "The large space behind and around the keys.", BackdropThemePreferences::load, BackdropThemePreferences::save)
                }
                ThemeEditorFold("Suggestion bar", "Toolbar, lozenges and current-word highlight") {
                    SurfaceThemeSection("Suggestion toolbar", "The strip behind suggestion lozenges and the ✨ toggle.", ToolbarThemePreferences::load, ToolbarThemePreferences::save, true)
                    SuggestionLozengeAppearanceSection()
                }
                ThemeEditorFold("Keys", "Fill, borders, shadows and key surfaces", true) {
                    KeyAppearanceSection()
                }
                ThemeEditorFold("Typography", "Key labels, fonts and suggestion text") {
                    FontAndSuggestionSection()
                }
                Text("Planned surface slot: one-shot key-press overlays (GIF/image effects).", Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall)
            } else {
                Column(Modifier.fillMaxWidth().alpha(0.42f).padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Main backdrop", style = MaterialTheme.typography.titleLarge); Text("Suggestion toolbar", style = MaterialTheme.typography.titleLarge)
                    Text("Keys", style = MaterialTheme.typography.titleSmall); Text("Typography", style = MaterialTheme.typography.titleSmall); Text("Suggestions", style = MaterialTheme.typography.titleSmall)
                    Text("Enable custom keyboard backgrounds to edit Keywi appearance settings.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun SurfaceThemeSection(title: String, subtitle: String, load: (android.content.Context) -> BackdropThemeState, save: (android.content.Context, BackdropThemeState) -> Unit, showToolbarBorder: Boolean = false) {
    val context = LocalContext.current
    var state by remember { mutableStateOf(load(context)) }
    var mediaPickMode by remember { mutableStateOf(BackdropMode.IMAGE) }
    val scope = rememberCoroutineScope()
    var mediaError by remember { mutableStateOf<String?>(null) }
    fun persist(next: BackdropThemeState) { state = next; save(context, next) }
    val mediaPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val selectedMode = mediaPickMode
            scope.launch {
                val result = withContext(Dispatchers.IO) { runCatching { ThemeAssetStore.copySelected(context, uri) } }
                result.fold(
                    onSuccess = { owned -> mediaError = null; persist(state.copy(mode = selectedMode, mediaUri = owned)) },
                    onFailure = { mediaError = "Could not copy media: ${it.message}" },
                )
            }
        }
    }
    Column(gemSectionModifier(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall); Text(subtitle, style = MaterialTheme.typography.bodySmall)
        mediaError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (showToolbarBorder) {
            var borderWidth by remember { mutableStateOf(ToolbarBorderPreferences.loadWidth(context)) }
            var toolbarGap by remember { mutableStateOf(ToolbarLayoutPreferences.loadKeyboardGap(context)) }
            var borderColorText by remember { mutableStateOf(String.format("#%08X", ToolbarBorderPreferences.loadColor(context).value.toLong())) }
            Text("Toolbar border: ${String.format("%.1f", borderWidth)} px", style = MaterialTheme.typography.titleSmall)
            Slider(borderWidth, { borderWidth = it; ToolbarBorderPreferences.saveWidth(context, it) }, valueRange = 0f..3f)
            OutlinedTextField(borderColorText, { text -> borderColorText = text; runCatching { Color(android.graphics.Color.parseColor(text)) }.getOrNull()?.let { ToolbarBorderPreferences.saveColor(context, it) } }, label = { Text("Border color (#AARRGGBB or #RRGGBB)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Text("Toolbar → keys gap: ${String.format("%.1f", toolbarGap)} dp", style = MaterialTheme.typography.titleSmall)
            Slider(toolbarGap, { toolbarGap = it; ToolbarLayoutPreferences.saveKeyboardGap(context, it) }, valueRange = 0f..24f)
        }
        ChipRow(BackdropMode.entries, state.mode, { it.name.lowercase().replaceFirstChar(Char::uppercase) }) { persist(state.copy(mode = it)) }
        Text("Opacity: ${(state.opacity * 100).roundToInt()}%")
        Slider(state.opacity, { persist(state.copy(opacity = it)) }, valueRange = 0f..1f)
        if (state.mode == BackdropMode.IMAGE || state.mode == BackdropMode.GIF) Button(onClick = { mediaPickMode = state.mode; mediaPicker.launch(arrayOf("image/*")) }, modifier = Modifier.fillMaxWidth()) { Text(if (state.mediaUri == null) "Choose media" else "Change media") }
        if (state.mode != BackdropMode.NONE) Box(Modifier.fillMaxWidth().height(54.dp)) { BackdropVisualLayer(state) }
        if (state.mode == BackdropMode.COLORFUL) ManagedGradientEditor("Gradient", state.toBackdrop()) { gradient -> persist(state.copy(preset = BackdropPreset.CUSTOM, angleDegrees = gradient.angleDegrees, stops = gradient.stops)) }
    }
}

@Composable
private fun ManagedGradientEditor(title: String, gradient: KeyboardBackdrop, onGradientChange: (KeyboardBackdrop) -> Unit) {
    val context = LocalContext.current
    var gradients by remember { mutableStateOf(GradientLibrary.load(context)) }; var menuOpen by remember { mutableStateOf(false) }; var selectedName by remember { mutableStateOf("Current gradient") }; var saveName by remember { mutableStateOf("") }; var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) { Text(title, style = MaterialTheme.typography.titleSmall); Text(if (expanded) "Hide details  ▲" else "Edit gradient  ▼", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { expanded = !expanded }.padding(6.dp)) }
        Box { Button(onClick = { menuOpen = true }, modifier = Modifier.fillMaxWidth()) { Text(selectedName, fontSize = 13.sp) }; DropdownMenu(menuOpen, { menuOpen = false }) {
            gradients.forEach { saved -> DropdownMenuItem({ Text(saved.name) }, { selectedName = saved.name; menuOpen = false; onGradientChange(saved.toBackdrop()) }) }
            DropdownMenuItem({ Text("＋ New gradient") }, { selectedName = "New gradient"; menuOpen = false; expanded = true; onGradientChange(KeyboardBackdrop(listOf(KeyboardGradientStop(0f, Color(0xFF151A2C)), KeyboardGradientStop(1f, Color(0xFFB5D8FF))), 0f)) })
        } }
        Box(Modifier.fillMaxWidth().height(24.dp).clip(RoundedCornerShape(12.dp)).keyboardGradientBackground(gradient))
        if (expanded) Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f), RoundedCornerShape(12.dp)).padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            GradientEditor(title, gradient) { selectedName = "Modified — save as new"; onGradientChange(it) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) { OutlinedTextField(saveName, { saveName = it }, label = { Text("Save as") }, singleLine = true, textStyle = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f)); Button(enabled = saveName.isNotBlank(), onClick = { val saved = GradientLibrary.saveCustom(context, SavedGradient("", saveName.trim(), gradient.angleDegrees, gradient.stops)); selectedName = saved.name; saveName = ""; gradients = GradientLibrary.load(context) }) { Text("Save", fontSize = 12.sp) } }
        }
    }
}

@Composable
private fun SuggestionLozengeAppearanceSection() {
    val context = LocalContext.current; var state by remember { mutableStateOf(SuggestionLozengeThemePreferences.load(context)) }
    fun persist(next: SuggestionLozengeThemeState) { state = next; SuggestionLozengeThemePreferences.save(context, next) }
    Column(gemSectionModifier(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Toolbar lozenges", style = MaterialTheme.typography.titleSmall); Text("Suggestion pills and the ✨ toggle get their own surface and border theme.", style = MaterialTheme.typography.bodySmall); Text("Lozenge background", style = MaterialTheme.typography.titleSmall)
        ChipRow(SuggestionLozengeSurfaceStyle.entries, state.surfaceStyle, { it.name.lowercase().replaceFirstChar(Char::uppercase) }) { persist(state.copy(surfaceStyle = it)) }
        when (state.surfaceStyle) { SuggestionLozengeSurfaceStyle.GRADIENT -> ManagedGradientEditor("Lozenge background gradient", state.surfaceGradient) { persist(state.copy(surfaceGradient = it)) }; SuggestionLozengeSurfaceStyle.SOLID -> ColorEditor("Lozenge color", state.surfaceColor) { persist(state.copy(surfaceColor = it)) }; SuggestionLozengeSurfaceStyle.NONE -> Text("Transparent lozenge faces.") }
        Text("Lozenge border", style = MaterialTheme.typography.titleSmall); ChipRow(SuggestionLozengeBorderStyle.entries, state.borderStyle, { it.name.lowercase().replaceFirstChar(Char::uppercase) }) { persist(state.copy(borderStyle = it)) }; Text("Border width: ${String.format("%.1f", state.borderWidth)} dp"); Slider(state.borderWidth, { persist(state.copy(borderWidth = it)) }, valueRange = 0f..4f)
        when (state.borderStyle) { SuggestionLozengeBorderStyle.GRADIENT -> ManagedGradientEditor("Lozenge border gradient", state.borderGradient) { persist(state.copy(borderGradient = it)) }; SuggestionLozengeBorderStyle.SOLID -> ColorEditor("Lozenge border color", state.borderColor) { persist(state.copy(borderColor = it)) }; SuggestionLozengeBorderStyle.NONE -> Text("No lozenge border.") }
        Text("Current / unfinished word chip", style = MaterialTheme.typography.titleSmall)
        Text("The word you are still typing. Background alpha controls transparency; highlight colors its text and outline.", style = MaterialTheme.typography.bodySmall)
        ColorEditor("Current-word background", state.currentWordBackgroundColor) { persist(state.copy(currentWordBackgroundColor = it)) }
        ColorEditor("Current-word highlight", state.currentWordHighlightColor ?: AdvancedKeyWordPreferences.newWordHighlightColor(context)) { persist(state.copy(currentWordHighlightColor = it)) }

    }
}

@Composable
private fun KeyAppearanceSection() {
    val context = LocalContext.current; var state by remember { mutableStateOf(KeyThemePreferences.load(context)) }
    fun persist(next: KeyThemeState) { state = next; KeyThemePreferences.save(context, next) }
    Column(gemSectionModifier(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Keys", style = MaterialTheme.typography.titleSmall); Text("Key surface", style = MaterialTheme.typography.titleSmall)
        ChipRow(KeySurfaceStyle.entries, state.surfaceStyle, { it.name.lowercase().replaceFirstChar(Char::uppercase) }) { persist(state.copy(surfaceStyle = it)) }
        when (state.surfaceStyle) { KeySurfaceStyle.GRADIENT -> ManagedGradientEditor("Key surface gradient", state.surfaceGradient) { persist(state.copy(surfaceGradient = it)) }; KeySurfaceStyle.SOLID -> ColorEditor("Key color", state.surfaceColor) { persist(state.copy(surfaceColor = it)) }; KeySurfaceStyle.NONE -> Text("Transparent key faces.") }
        Text("Key border", style = MaterialTheme.typography.titleSmall); ChipRow(KeyBorderStyle.entries, state.borderStyle, { it.name.lowercase().replaceFirstChar(Char::uppercase) }) { persist(state.copy(borderStyle = it)) }
        when (state.borderStyle) { KeyBorderStyle.GRADIENT -> ManagedGradientEditor("Border gradient", state.borderGradient) { persist(state.copy(borderGradient = it)) }; KeyBorderStyle.SOLID -> ColorEditor("Border color", state.borderColor) { persist(state.copy(borderColor = it)) }; KeyBorderStyle.SHADOW -> { ColorEditor("Shadow color", state.shadowColor) { persist(state.copy(shadowColor = it)) }; Text("Shadow opacity: ${(state.shadowAlpha * 100).roundToInt()}%"); Slider(state.shadowAlpha, { persist(state.copy(shadowAlpha = it)) }, valueRange = 0f..1f); Text("Shadow elevation: ${state.shadowElevation.roundToInt()} dp"); Slider(state.shadowElevation, { persist(state.copy(shadowElevation = it)) }, valueRange = 0f..16f) }; KeyBorderStyle.NONE -> Text("No key border.") }
    }
}

@Composable
private fun FontAndSuggestionSection() {
    val context = LocalContext.current; var fontName by remember { mutableStateOf(FontPreferences.displayName(context)) }; var motion by remember { mutableStateOf(SuggestionMotionPreferences.load(context)) }
    val scope = rememberCoroutineScope()
    var fontError by remember { mutableStateOf<String?>(null) }
    val fontPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            val imported = withContext(Dispatchers.IO) { FontPreferences.importFont(context, uri) }
            if (imported) { fontName = FontPreferences.displayName(context); fontError = null }
            else fontError = "Could not copy or load this font."
        }
    }
    Column(gemSectionModifier(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        fontError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Text("Typography", style = MaterialTheme.typography.titleSmall); Text(fontName); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = { fontPicker.launch(arrayOf("font/*", "application/x-font-ttf", "application/x-font-opentype", "application/octet-stream")) }) { Text("Import font") }; Button(onClick = { FontPreferences.clear(context); fontName = FontPreferences.displayName(context) }) { Text("System default") } }
        Text("Suggestions", style = MaterialTheme.typography.titleSmall); Text("Motion style", style = MaterialTheme.typography.titleSmall); ChipRow(SuggestionMotionStyle.entries, motion, { it.name.lowercase().replaceFirstChar(Char::uppercase) }) { motion = it; SuggestionMotionPreferences.save(context, it) }
    }
}

@Composable
private fun <T> ChipRow(values: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) { Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { values.forEach { value -> FilterChip(selected = selected == value, onClick = { onSelect(value) }, label = { Text(label(value)) }) } } }

@Composable
private fun GradientEditor(label: String, gradient: KeyboardBackdrop, onChange: (KeyboardBackdrop) -> Unit) {
    var stopsExpanded by remember { mutableStateOf(false) }; Text("$label angle • ${gradient.angleDegrees.roundToInt()}°", style = MaterialTheme.typography.labelMedium); Slider(gradient.angleDegrees, { onChange(gradient.copy(angleDegrees = it)) }, valueRange = 0f..360f); Text("${gradient.stops.size} color stops  ${if (stopsExpanded) "▲" else "▼"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { stopsExpanded = !stopsExpanded }.padding(vertical = 4.dp))
    if (stopsExpanded) { gradient.stops.forEachIndexed { index, stop -> Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f), RoundedCornerShape(10.dp)).padding(7.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) { Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) { Text("Stop ${index + 1} • ${(stop.position * 100).roundToInt()}%", style = MaterialTheme.typography.labelSmall); Button(enabled = gradient.stops.size > 2, onClick = { val next = gradient.stops.toMutableList().also { it.removeAt(index) }; onChange(gradient.copy(stops = next)) }) { Text("Delete", fontSize = 11.sp) } }; Slider(stop.position, { position -> val next = gradient.stops.toMutableList().also { it[index] = stop.copy(position = position) }.sortedBy { it.position }; onChange(gradient.copy(stops = next)) }, valueRange = 0f..1f); ColorEditor("Color", stop.color) { color -> val next = gradient.stops.toMutableList().also { it[index] = stop.copy(color = color) }; onChange(gradient.copy(stops = next)) } } }; Button(onClick = { val color = gradient.stops.firstOrNull()?.color ?: Color.White; onChange(gradient.copy(stops = (gradient.stops + KeyboardGradientStop(0.5f, color)).sortedBy { it.position })) }, modifier = Modifier.fillMaxWidth()) { Text("＋ Add stop", fontSize = 12.sp) } }
}

@Composable
private fun ColorEditor(label: String, color: Color, onChange: (Color) -> Unit) { Column(Modifier.fillMaxWidth().padding(vertical = 2.dp)) { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Box(Modifier.size(22.dp).clip(RoundedCornerShape(7.dp)).background(color)); Text("$label • A ${(color.alpha * 100).roundToInt()}%", style = MaterialTheme.typography.labelMedium) }; Channel("R", color.red) { onChange(Color(it, color.green, color.blue, color.alpha)) }; Channel("G", color.green) { onChange(Color(color.red, it, color.blue, color.alpha)) }; Channel("B", color.blue) { onChange(Color(color.red, color.green, it, color.alpha)) }; Channel("A", color.alpha) { onChange(Color(color.red, color.green, color.blue, it)) } } }
@Composable
private fun Channel(label: String, value: Float, onChange: (Float) -> Unit) { Text("$label ${(value * 255).roundToInt()}", style = MaterialTheme.typography.labelSmall); Slider(value, onChange, valueRange = 0f..1f) }

/** Compact, independently expandable editor sections; advanced controls stay intact. */
@Composable
private fun ThemeEditorFold(
    title: String,
    subtitle: String,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
        Surface(
            onClick = { expanded = !expanded },
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)),
        ) {
            Row(Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall)
                }
                Text(if (expanded) "⌃" else "›", style = MaterialTheme.typography.titleLarge)
            }
        }
        if (expanded) content()
    }
}
