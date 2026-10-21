package com.dessalines.thumbkey.ui.components.settings.lookandfeel

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.ui.components.common.TestOutTextField
import com.dessalines.thumbkey.ui.components.settings.gemSectionModifier

/** Summon the installed keyboard so theme changes are tested on the real IME. */
@Composable
fun KeywiThemeTestBench(modifier: Modifier = Modifier) {
    Column(modifier.then(gemSectionModifier())) {
        Text("Try your keyboard", style = MaterialTheme.typography.titleMedium)
        Text(
            "Tap the field to type with your active keyboard, theme and effects.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 2.dp, bottom = 6.dp),
        )
        TestOutTextField()
    }
}
