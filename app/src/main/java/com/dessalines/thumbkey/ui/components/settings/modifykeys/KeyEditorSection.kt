package com.dessalines.thumbkey.ui.components.settings.modifykeys

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp

/** Keep editor bodies composed while hidden so even unsaved nested editors survive a toggle. */
@Composable
internal fun KeyEditorSection(
    title: String,
    colors: CardColors = CardDefaults.cardColors(),
    content: @Composable () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(true) }
    val focusManager = LocalFocusManager.current
    Card(colors = colors) {
        Column {
            Row(
                Modifier.fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("key-editor-header:$title")
                    .semantics { stateDescription = if (expanded) "Expanded" else "Collapsed" }
                    .clickable(
                        role = Role.Button,
                        onClickLabel = if (expanded) "Collapse $title" else "Expand $title",
                    ) {
                        focusManager.clearFocus()
                        expanded = !expanded
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
            }
            Layout(
                modifier = if (expanded) Modifier else Modifier.clearAndSetSemantics {},
                content = { Column { content() } },
            ) { measurables, constraints ->
                if (expanded) {
                    val body = measurables.single().measure(constraints.copy(minHeight = 0))
                    layout(body.width, body.height) { body.place(0, 0) }
                } else {
                    // Unplaced content has no touch/focus targets or visual footprint.
                    layout(constraints.minWidth, 0) {}
                }
            }
        }
    }
}
