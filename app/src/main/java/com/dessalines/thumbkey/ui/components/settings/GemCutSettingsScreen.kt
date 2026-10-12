package com.dessalines.thumbkey.ui.components.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
    val colors = if (dark) AdvancedSettingsColors(
        background = Color(0xFF0B0917), surface = Color(0xFF151125),
        surfaceRaised = Color(0xFF261A3C), outline = Color(0xFF493563),
        text = Color(0xFFF4EDFF), textMuted = Color(0xFFC5B6D9),
        accent = Color(0xFFBA8AFF), onAccent = Color(0xFF180B2A),
    ) else AdvancedSettingsColors(
        background = Color(0xFFFAF7FF), surface = Color(0xFFF1EAFB),
        surfaceRaised = Color(0xFFE4D4FF), outline = Color(0xFFC0AED5),
        text = Color(0xFF211332), textMuted = Color(0xFF665274),
        accent = Color(0xFF7434BD), onAccent = Color.White,
    )

    CompositionLocalProvider(LocalAdvancedSettingsColors provides colors) {
        Scaffold(
            containerColor = colors.background,
            topBar = {
                TopAppBar(
                    title = { Text("◇ Gem cuts", fontWeight = FontWeight.SemiBold) },
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
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("✦ Live preview", color = colors.text, style = MaterialTheme.typography.titleMedium)
                    Text(KeywiFacets.all.first { it.id == facet }.displayName,
                        color = colors.accent, style = MaterialTheme.typography.labelMedium)
                }
                Surface(shape = RoundedCornerShape(8.dp), color = colors.surface,
                    border = BorderStroke(1.dp, colors.outline)) {
                    // Keep the ambient keyboard palette; settings chrome does not apply a theme.
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        GemCutPreviewScreen(facet, definition, Modifier.padding(10.dp))
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
    val shape = RoundedCornerShape(8.dp)
    Surface(
        modifier = Modifier.fillMaxWidth().selectable(selected, role = Role.RadioButton, onClick = onSelect),
        shape = shape, color = colors.surface,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) colors.accent else colors.outline),
    ) {
        Row(
            Modifier.background(Brush.horizontalGradient(listOf(
                if (selected) colors.accent.copy(alpha = .18f) else Color.Transparent,
                Color.Transparent,
            ))).padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.width(92.dp)) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    GemCutGeometryPreview(spec.id, definition.modes.main, Modifier.fillMaxWidth(), compact = true)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(spec.displayName, color = colors.text, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                Text(if (spec.id == FacetId.ME_LIKE) "Swipe-centered keys with a 3 × 3 core and surrounding controls."
                    else "Five compact touch rows with a desktop control layer.",
                    color = colors.textMuted, style = MaterialTheme.typography.bodySmall)
                Text(if (selected) "Selected" else if (spec.id == FacetId.HK_LIKE) "Experimental" else "Swipe geometry",
                    color = colors.accent, style = MaterialTheme.typography.labelSmall)
            }
            if (selected) Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = colors.accent)
        }
    }
}
