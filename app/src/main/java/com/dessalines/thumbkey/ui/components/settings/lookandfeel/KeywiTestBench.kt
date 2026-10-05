package com.dessalines.thumbkey.ui.components.settings.lookandfeel

import androidx.compose.foundation.layout.Column
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

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp)) {
        Text("Test out Keywi", style = MaterialTheme.typography.titleMedium)
        Text(
            "Tap below to open the keyboard and try the active theme live.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 2.dp, bottom = 6.dp),
        )
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
