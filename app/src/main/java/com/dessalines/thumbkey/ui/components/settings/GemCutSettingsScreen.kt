package com.dessalines.thumbkey.ui.components.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dessalines.thumbkey.db.DEFAULT_KEY_BORDER_WIDTH
import com.dessalines.thumbkey.db.DEFAULT_KEY_RADIUS
import com.dessalines.thumbkey.db.AppSettingsViewModel
import com.dessalines.thumbkey.db.DEFAULT_KEYBOARD_LAYOUT
import com.dessalines.thumbkey.ui.components.keyboard.FacetId
import com.dessalines.thumbkey.ui.components.keyboard.FacetSpec
import com.dessalines.thumbkey.ui.components.keyboard.GemCutGeometryPreview
import com.dessalines.thumbkey.ui.components.keyboard.GemCutPreferences
import com.dessalines.thumbkey.ui.components.keyboard.GemCutPreviewScreen
import com.dessalines.thumbkey.ui.components.keyboard.KeywiFacets
import com.dessalines.thumbkey.utils.KeyboardDefinition
import com.dessalines.thumbkey.utils.KeyboardLayout
import com.dessalines.thumbkey.utils.getModifiedKeyboardDefinition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GemCutSettingsScreen(navController: NavController, appSettingsViewModel: AppSettingsViewModel) {
    val context = LocalContext.current
    val settings by appSettingsViewModel.appSettings.observeAsState()
    var facet by remember { mutableStateOf(GemCutPreferences.load(context)) }
    val layout = KeyboardLayout.entries.getOrNull(settings?.keyboardLayout ?: DEFAULT_KEYBOARD_LAYOUT)
        ?: KeyboardLayout.entries[DEFAULT_KEYBOARD_LAYOUT]
    val modifications = settings?.keyModifications
    val definition = remember(layout, modifications) {
        if (modifications.isNullOrEmpty()) layout.keyboardDefinition else
            getModifiedKeyboardDefinition(layout, modifications) ?: layout.keyboardDefinition
    }
    val dark = MaterialTheme.colorScheme.background.luminance() < .5f
    val colors = gemSettingsColors(dark)

    CompositionLocalProvider(LocalAdvancedSettingsColors provides colors) {
        Scaffold(
            containerColor = colors.background,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Outlined.Diamond, contentDescription = null, tint = colors.accent)
                            Text("Gem cuts", fontWeight = FontWeight.SemiBold)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.background, titleContentColor = colors.text,
                        navigationIconContentColor = colors.accent,
                    ),
                )
            },
        ) { padding ->
            Column(
                Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Choose your geometry", color = colors.text, style = MaterialTheme.typography.titleMedium)
                Text("A cut shapes the keys. Your theme and character layout stay independent.",
                    color = colors.textMuted, style = MaterialTheme.typography.bodySmall)
                Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    KeywiFacets.all.forEach { spec ->
                        GemCutChoice(spec, facet == spec.id, definition) {
                            facet = spec.id
                            GemCutPreferences.save(context, spec.id)
                        }
                    }
                }
                Surface(shape = RoundedCornerShape(12.dp), color = colors.surface,
                    border = BorderStroke(1.dp, colors.outline)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("ⓘ About Gem cuts", color = colors.text, style = MaterialTheme.typography.titleSmall)
                        Text("Cuts change the physical arrangement of keys. Your theme, colors and character assignments stay saved independently.",
                            color = colors.textMuted, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Surface(shape = RoundedCornerShape(12.dp), color = colors.surface,
                    border = BorderStroke(1.dp, colors.accent)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("✦ Live preview", color = colors.text, style = MaterialTheme.typography.titleMedium)
                            Text(KeywiFacets.all.first { it.id == facet }.displayName,
                                color = colors.accent, style = MaterialTheme.typography.labelMedium)
                        }
                        Text("Explore the selected cut using your current theme.",
                            color = colors.textMuted, style = MaterialTheme.typography.bodySmall)
                        // Keep the ambient keyboard palette; settings chrome does not apply a theme.
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            MaterialTheme(colorScheme = LocalKeyboardPreviewColorScheme.current ?: MaterialTheme.colorScheme) {
                                GemCutPreviewScreen(
                                    facet, definition,
                                    keyBorderWidth = (settings?.keyBorderWidth ?: DEFAULT_KEY_BORDER_WIDTH) / 10f,
                                    keyRadius = settings?.keyRadius ?: DEFAULT_KEY_RADIUS,
                                )
                            }
                        }
                    }
                }
                Text(if (facet == FacetId.HK_LIKE)
                    "HK-Like is experimental and currently uses its own prototype character rows. Your saved ME layout is retained."
                    else "The preview follows your selected character layout, including modified keys. Switch ABC / ⇧ / 123 to inspect its layers.",
                    color = colors.textMuted, style = MaterialTheme.typography.bodySmall)
                Text("◇ Geometry   ·   ✦ Theme   ·   ⌘ Characters", color = colors.accent,
                    style = MaterialTheme.typography.labelMedium)
                Text("Changing cuts keeps your colors and character assignments saved. Theme controls live in Look & Feel; character controls live in Layouts and Modify keys.",
                    color = colors.textMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun GemCutChoice(spec: FacetSpec, selected: Boolean, definition: KeyboardDefinition, onSelect: () -> Unit) {
    val colors = LocalAdvancedSettingsColors.current
    val shape = RoundedCornerShape(12.dp)
    Surface(
        modifier = Modifier.fillMaxWidth()
            .then(if (selected) Modifier.shadow(7.dp, shape, ambientColor = colors.accent,
                spotColor = colors.accent) else Modifier)
            .selectable(selected, role = Role.RadioButton, onClick = onSelect),
        shape = shape, color = colors.surface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) colors.accent else colors.outline),
    ) {
        BoxWithConstraints {
            val previewWidth = (maxWidth * .40f).coerceIn(100.dp, 200.dp)
            Column(
                Modifier.background(Brush.horizontalGradient(listOf(
                    if (selected) colors.surfaceRaised else colors.surface,
                    colors.surface,
                ))).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (selected) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = null, tint = colors.accent, modifier = Modifier.size(22.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(spec.displayName, color = colors.text, style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                        Text(if (spec.id == FacetId.ME_LIKE) "A central 3 × 3 swipe surface with persistent surrounding controls."
                            else "Five compact touch rows, with a desktop control layer.",
                            color = colors.textMuted, style = MaterialTheme.typography.bodySmall)
                        if (selected) Text("Selected", color = colors.accent, style = MaterialTheme.typography.labelSmall)
                    }
                    Box(Modifier.width(previewWidth)) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            MaterialTheme(colorScheme = LocalKeyboardPreviewColorScheme.current ?: MaterialTheme.colorScheme) {
                                GemCutGeometryPreview(spec.id, definition.modes.main, Modifier.fillMaxWidth(), compact = true)
                            }
                        }
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val tags = if (spec.id == FacetId.ME_LIKE) listOf("3×3 core", "Surrounding controls", "Swipe keys")
                        else listOf("5 touch rows", "Desktop layer", "Experimental")
                    tags.forEach { tag ->
                        Surface(shape = RoundedCornerShape(5.dp), color = Color.Transparent,
                            border = BorderStroke(1.dp, colors.accent.copy(alpha = .45f))) {
                            Text(tag, Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                color = colors.textMuted, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
