package com.dessalines.thumbkey.ui.components.settings.lookandfeel

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.ui.components.keyboard.GradientDistribution
import com.dessalines.thumbkey.ui.components.keyboard.KeyDesignGroup
import com.dessalines.thumbkey.ui.components.keyboard.KeyDesignOverride
import com.dessalines.thumbkey.ui.components.keyboard.KeyThemePreferences
import com.dessalines.thumbkey.ui.components.keyboard.KeyThemeState
import com.dessalines.thumbkey.ui.components.keyboard.KeyboardBackdrop
import kotlin.math.roundToInt

@Composable
internal fun BorderDistributionEditor(
    state: KeyThemeState,
    onChange: (KeyThemeState) -> Unit,
) {
    Text("Border gradient distribution", style = MaterialTheme.typography.titleSmall)
    ChoiceChips(GradientDistribution.entries, state.borderDistribution, {
        when (it) {
            GradientDistribution.SAME -> "Same"
            GradientDistribution.STABLE_RANDOM -> "Stable random"
            GradientDistribution.INCREMENTAL -> "Step per key"
            GradientDistribution.SHARED -> "Across keyboard"
        }
    }) { onChange(state.copy(borderDistribution = it)) }
    Text(
        when (state.borderDistribution) {
            GradientDistribution.SAME -> "Every key starts its gradient at the same position."
            GradientDistribution.STABLE_RANDOM -> "Each key has its own starting phase, stable while typing."
            GradientDistribution.INCREMENTAL -> "Offset each consecutive key in row order."
            GradientDistribution.SHARED -> "A single gradient spans the keyboard; each key reveals its part."
        },
        style = MaterialTheme.typography.bodySmall,
    )
    if (state.borderDistribution == GradientDistribution.INCREMENTAL) {
        Text("Phase step: ${state.borderPhaseStepDegrees.roundToInt()}°")
        Slider(state.borderPhaseStepDegrees, { onChange(state.copy(borderPhaseStepDegrees = it)) }, valueRange = 0f..360f)
    }
    if (state.borderDistribution == GradientDistribution.STABLE_RANDOM) {
        OutlinedButton(onClick = { onChange(state.copy(borderRandomSeed = state.borderRandomSeed + 1)) }) {
            Text("Shuffle starting points")
        }
    }
}

/** Uses the remapper's real key matrix; appearance targets refer to physical row/column positions. */
@Composable
internal fun KeyDesignOverrideEditor(
    selectedIds: Set<String>,
    groupMode: Boolean,
    onSelectGroup: (Set<String>) -> Unit,
) {
    val context = LocalContext.current
    var state by remember { mutableStateOf(KeyThemePreferences.load(context)) }
    // Bind drafts to the saved group's identity so membership edits keep their settings.
    var editingGroupId by remember { mutableStateOf<String?>(null) }
    var newGroupRevision by remember(groupMode) { mutableIntStateOf(0) }
    val existingGroup = if (groupMode) state.keyGroups.firstOrNull { it.id == editingGroupId } else null
    val savedDesign = if (groupMode) existingGroup?.design else selectedIds.firstOrNull()?.let { state.keyOverrides[it] }
    val draftTarget = if (groupMode) editingGroupId else selectedIds.firstOrNull()
    var design by remember(groupMode, draftTarget, newGroupRevision, savedDesign) { mutableStateOf(savedDesign ?: KeyDesignOverride()) }
    var groupName by remember(groupMode, draftTarget, newGroupRevision) { mutableStateOf(existingGroup?.id.orEmpty()) }
    var sharedCanvas by remember(groupMode, draftTarget, newGroupRevision, existingGroup?.sharedGradientCanvas) {
        mutableStateOf(existingGroup?.sharedGradientCanvas ?: false)
    }
    var message by remember(selectedIds, groupMode) { mutableStateOf<String?>(null) }
    val nameCollision =
        groupMode && groupName.isNotBlank() &&
            state.keyGroups.any { it.id == groupName.trim() && it.id != existingGroup?.id }

    fun persist(next: KeyThemeState) {
        state = next
        KeyThemePreferences.save(context, next)
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Key appearance • ${if (groupMode) "group" else "individual key"}", style = MaterialTheme.typography.titleMedium)
            Text(
                "Selected: ${selectedIds.sorted().joinToString().ifBlank { "none — tap keys above" }}",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                "Positions apply across boards and keyboard modes. Individual overrides win over groups; later groups win over earlier ones. Inherit uses the group's or theme's design.",
                style = MaterialTheme.typography.bodySmall,
            )
            if (state.keyGroups.isNotEmpty()) {
                Text("Saved groups", style = MaterialTheme.typography.labelMedium)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.keyGroups.forEach { group ->
                        FilterChip(
                            selected = groupMode && editingGroupId == group.id,
                            onClick = {
                                editingGroupId = group.id
                                onSelectGroup(group.keyIds)
                            },
                            label = { Text(group.id) },
                        )
                    }
                }
            }
            if (groupMode) {
                OutlinedButton(onClick = {
                    editingGroupId = null
                    newGroupRevision++
                    message = null
                }) { Text("New group from selection") }
                OutlinedTextField(groupName, {
                    groupName = it
                }, label = { Text("Group name") }, singleLine = true, isError = nameCollision, modifier = Modifier.fillMaxWidth())
                if (nameCollision) {
                    Text(
                        "That group name is already in use. Choose another name or select the saved group to edit it.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(
                    if (existingGroup == null) {
                        "Create a group from the highlighted cells above."
                    } else {
                        "Editing ${existingGroup.id}. Tap cells above to change its membership, then apply."
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Share gradient across this group", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                    Switch(checked = sharedCanvas, onCheckedChange = { sharedCanvas = it })
                }
                Text(
                    if (sharedCanvas) {
                        "One gradient spans the selected keys' bounding rectangle, for both gradient fill and border. Gaps keep their place in that canvas; unselected keys keep their own design. Inherit can use the theme's gradient. Individual key overrides take priority for that fill or border."
                    } else {
                        "Each key keeps its own fill gradient and the theme's border distribution. Enable sharing to restrict a continuous gradient canvas to this group."
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            OverrideSurfaceEditor(
                "Border / highlight",
                design.borderColor,
                design.borderGradient,
                state.borderColor,
                state.borderGradient,
            ) { color, gradient ->
                design = design.copy(borderColor = color, borderGradient = gradient)
            }
            OverrideSurfaceEditor(
                "Key fill",
                design.surfaceColor,
                design.surfaceGradient,
                state.surfaceColor,
                state.surfaceGradient,
            ) { color, gradient ->
                design = design.copy(surfaceColor = color, surfaceGradient = gradient)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(enabled = selectedIds.isNotEmpty() && !nameCollision, onClick = {
                    // Read the latest theme so saving a key does not replace unrelated live edits.
                    val latest = KeyThemePreferences.current
                    if (groupMode) {
                        val id =
                            groupName.trim().ifBlank {
                                existingGroup?.id ?: generateSequence(1) { it + 1 }
                                    .map { "Group $it" }
                                    .first { candidate -> latest.keyGroups.none { it.id == candidate } }
                            }
                        val replacement =
                            KeyDesignGroup(id = id, keyIds = selectedIds, design = design, sharedGradientCanvas = sharedCanvas)
                        val groups = latest.keyGroups.toMutableList()
                        val index = groups.indexOfFirst { it.id == existingGroup?.id || it.id == id }
                        if (index >= 0) groups[index] = replacement else groups.add(replacement)
                        persist(latest.copy(keyGroups = groups))
                        editingGroupId = id
                    } else {
                        persist(latest.copy(keyOverrides = latest.keyOverrides + (selectedIds.first() to design)))
                    }
                    message = "Appearance saved ✓"
                }) { Text("Apply appearance") }
                OutlinedButton(enabled = savedDesign != null, onClick = {
                    val latest = KeyThemePreferences.current
                    persist(
                        if (groupMode) {
                            latest.copy(keyGroups = latest.keyGroups.filterNot { it.id == existingGroup?.id })
                        } else {
                            latest.copy(keyOverrides = latest.keyOverrides - selectedIds)
                        },
                    )
                    design = KeyDesignOverride()
                    if (groupMode) {
                        editingGroupId = null
                        newGroupRevision++
                    }
                    message = "Override removed; inherited appearance restored."
                }) { Text("Reset") }
            }
            message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun OverrideSurfaceEditor(
    title: String,
    color: Color?,
    gradient: KeyboardBackdrop?,
    defaultColor: Color,
    defaultGradient: KeyboardBackdrop,
    onChange: (Color?, KeyboardBackdrop?) -> Unit,
) {
    val mode =
        when {
            gradient != null -> "Gradient"
            color != null -> "Solid"
            else -> "Inherit"
        }
    Text(title, style = MaterialTheme.typography.titleSmall)
    ChoiceChips(listOf("Inherit", "Solid", "Gradient"), mode, { it }) {
        when (it) {
            "Solid" -> onChange(color ?: defaultColor, null)
            "Gradient" -> onChange(null, gradient ?: defaultGradient)
            else -> onChange(null, null)
        }
    }
    if (mode == "Solid") ColorEditor(title, color ?: defaultColor) { onChange(it, null) }
    if (mode == "Gradient") ManagedGradientEditor(title, gradient ?: defaultGradient) { onChange(null, it) }
}

@Composable
private fun <T> ChoiceChips(
    values: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        values.forEach { item -> FilterChip(selected == item, { onSelect(item) }, label = { Text(label(item)) }) }
    }
}
