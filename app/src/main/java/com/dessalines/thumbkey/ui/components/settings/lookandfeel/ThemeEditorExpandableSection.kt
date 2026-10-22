package com.dessalines.thumbkey.ui.components.settings.lookandfeel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.ui.components.keyboard.KeywiPowerMode
import com.dessalines.thumbkey.ui.components.keyboard.KeywiPowerPreferences

/** One outline owns the heading, summary and expanding body so their relationship remains visible. */
@Composable
internal fun ThemeEditorExpandableSection(
    title: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailingLabel: String? = null,
    summary: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val animate = KeywiPowerPreferences.load(LocalContext.current) == KeywiPowerMode.UNBRIDLED
    Surface(
        modifier = modifier.fillMaxWidth().testTag("theme-section:$title"),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .30f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .28f)),
    ) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = if (expanded) .08f else .035f))
                    .testTag("theme-section-header:$title")
                    .semantics { stateDescription = if (expanded) "Expanded" else "Collapsed" }
                    .clickable(
                        role = Role.Button,
                        onClickLabel = if (expanded) "Collapse $title" else "Expand $title",
                    ) { onExpandedChange(!expanded) }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(title, style = MaterialTheme.typography.titleSmall)
                    subtitle?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(
                    trailingLabel ?: if (expanded) "⌃" else "⌄",
                    style = if (trailingLabel == null) MaterialTheme.typography.titleLarge else MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (summary != null) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) { summary() }
            }
            AnimatedVisibility(
                visible = expanded,
                enter =
                    if (animate) {
                        expandVertically(tween(220), expandFrom = Alignment.Top) + fadeIn(tween(150))
                    } else {
                        EnterTransition.None
                    },
                exit =
                    if (animate) {
                        shrinkVertically(tween(180), shrinkTowards = Alignment.Top) + fadeOut(tween(100))
                    } else {
                        ExitTransition.None
                    },
            ) {
                Column(Modifier.fillMaxWidth()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = .18f))
                    Column(
                        Modifier.fillMaxWidth().padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) { content() }
                }
            }
        }
    }
}
