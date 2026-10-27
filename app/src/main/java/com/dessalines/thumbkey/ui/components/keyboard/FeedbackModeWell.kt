package com.dessalines.thumbkey.ui.components.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.effects.FeedbackMode
import com.dessalines.thumbkey.effects.FeedbackPreferences
import com.dessalines.thumbkey.effects.rememberFeedbackMode

/** Three explicit choices in a shallow inset; saved sound sources remain independent. */
@Composable
internal fun FeedbackModeWell(onChanged: () -> Unit = {}) {
    val context = LocalContext.current
    val selected = rememberFeedbackMode()
    val colors = MaterialTheme.colorScheme
    val themed = KeywiPowerPreferences.advancedThemingEnabled
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Feedback", style = MaterialTheme.typography.titleSmall)
        Row(
            Modifier.fillMaxWidth().selectableGroup().clip(RoundedCornerShape(14.dp))
                .background(colors.surfaceVariant.copy(alpha = .3f))
                .then(if (themed) Modifier.drawWithCache {
                    val shade = Brush.verticalGradient(listOf(Color.Black.copy(alpha = .35f), Color.Transparent), endY = 7.dp.toPx())
                    onDrawBehind {
                        drawRect(shade)
                        drawRoundRect(Color.White.copy(alpha = .12f), topLeft = Offset(0f, 1.dp.toPx()),
                            cornerRadius = CornerRadius(14.dp.toPx()), style = Stroke(1.dp.toPx()))
                    }
                } else Modifier)
                .padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            FeedbackMode.entries.forEach { mode ->
                val choose = { FeedbackPreferences.save(context, mode); onChanged() }
                // One radio target per segment, including its icon and label.
                ToolPageSurface(
                    onClick = choose,
                    selected = mode == selected,
                    designId = "feedback:${mode.name}",
                    modifier = Modifier.weight(1f).semantics { role = Role.RadioButton },
                ) {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 3.dp, vertical = 9.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(when (mode) {
                        FeedbackMode.SOUND -> Icons.AutoMirrored.Outlined.VolumeUp
                        FeedbackMode.VIBRATE -> Icons.Outlined.Vibration
                        FeedbackMode.SILENT -> Icons.Outlined.VolumeOff
                    }, contentDescription = null, modifier = Modifier.size(20.dp),
                        tint = if (mode == selected) colors.onSurface else colors.onSurfaceVariant)
                    Text(mode.title, style = MaterialTheme.typography.labelMedium,
                        color = if (mode == selected) colors.onSurface else colors.onSurfaceVariant)
                }
                }
            }
        }
    }
}
