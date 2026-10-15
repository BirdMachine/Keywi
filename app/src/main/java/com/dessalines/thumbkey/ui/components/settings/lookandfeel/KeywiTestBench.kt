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
        Text("Background preview above · tap below to open the real keyboard", style = MaterialTheme.typography.labelSmall)
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
