package com.dessalines.thumbkey.ui.components.settings.lookandfeel

import com.dessalines.thumbkey.ui.components.settings.gemSectionModifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import com.dessalines.thumbkey.ui.components.keyboard.BackdropThemePreferences
import com.dessalines.thumbkey.ui.components.keyboard.BackdropVisualLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.dessalines.thumbkey.ui.components.keyboard.KeyThemePreferences
import com.dessalines.thumbkey.ui.components.keyboard.KeySurfaceStyle
import com.dessalines.thumbkey.ui.components.keyboard.KeyBorderStyle
import com.dessalines.thumbkey.ui.components.keyboard.SuggestionLozengeThemePreferences
import com.dessalines.thumbkey.ui.components.keyboard.SuggestionLozengeSurfaceStyle
import com.dessalines.thumbkey.ui.components.keyboard.keyboardGradientBackground
import com.dessalines.thumbkey.ui.components.keyboard.keyboardGradientBorder
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** A real text editor used to summon Keywi while tuning or comparing themes. */
@Composable
fun KeywiThemeTestBench(
    modifier: Modifier = Modifier,
) {
    var testText by remember { mutableStateOf("") }

    Column(modifier = modifier.then(gemSectionModifier())) {
        Text("Test out Keywi", style = MaterialTheme.typography.titleMedium)
        Text(
            "Tap below to open the keyboard and try the active theme live.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 2.dp, bottom = 6.dp),
        )
        Box(Modifier.fillMaxWidth().height(72.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
            BackdropVisualLayer(BackdropThemePreferences.load(LocalContext.current))
        }
        Text("Material inspector · actual saved key and suggestion styling", style = MaterialTheme.typography.labelSmall)
        val context = LocalContext.current
        val keyTheme = KeyThemePreferences.load(context)
        val lozenge = SuggestionLozengeThemePreferences.load(context)
        Row(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val keyRadius = 18.dp
            val keyModifier = Modifier.size(104.dp).clip(RoundedCornerShape(keyRadius))
                .then(if (keyTheme.surfaceStyle == KeySurfaceStyle.GRADIENT)
                    Modifier.keyboardGradientBackground(keyTheme.surfaceGradient) else Modifier)
                .then(if (keyTheme.borderStyle == KeyBorderStyle.GRADIENT)
                    Modifier.keyboardGradientBorder(backdrop = keyTheme.borderGradient, width = 2.dp, radius = keyRadius) else Modifier)
            Surface(
                modifier = keyModifier,
                shape = RoundedCornerShape(keyRadius),
                color = when (keyTheme.surfaceStyle) {
                    KeySurfaceStyle.SOLID -> keyTheme.surfaceColor
                    KeySurfaceStyle.GRADIENT -> Color.Transparent
                    KeySurfaceStyle.NONE -> MaterialTheme.colorScheme.surface.copy(alpha = 0.18f)
                },
                border = if (keyTheme.borderStyle == KeyBorderStyle.SOLID) BorderStroke(2.dp, keyTheme.borderColor) else null,
                shadowElevation = if (keyTheme.borderStyle == KeyBorderStyle.SHADOW) keyTheme.shadowElevation.dp else 0.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("A", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                }
            }
            val chipModifier = Modifier.clip(RoundedCornerShape(16.dp))
                .then(if (lozenge.surfaceStyle == SuggestionLozengeSurfaceStyle.GRADIENT)
                    Modifier.keyboardGradientBackground(lozenge.surfaceGradient) else Modifier)
            Surface(
                modifier = chipModifier,
                shape = RoundedCornerShape(16.dp),
                color = if (lozenge.surfaceStyle == SuggestionLozengeSurfaceStyle.SOLID) lozenge.surfaceColor else Color.Transparent,
                border = BorderStroke(lozenge.borderWidth.dp, lozenge.borderColor),
            ) {
                Text("suggestion", Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
            }
        }
        Text("Tap below to try the full keyboard", style = MaterialTheme.typography.labelSmall)
        OutlinedTextField(
            value = testText,
            onValueChange = { testText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Type something sparkly… ✨") },
            minLines = 2,
            maxLines = 4,
        )
    }
}
