package com.dessalines.thumbkey.ui.components.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class AdvancedSettingsColors(
    val background: Color = Color(0xFF071B21),
    val surface: Color = Color(0xFF102A32),
    val surfaceRaised: Color = Color(0xFF183841),
    val outline: Color = Color(0xFF315761),
    val text: Color = Color(0xFFF2FAFB),
    val textMuted: Color = Color(0xFFA8BEC4),
    val accent: Color = Color(0xFF6ED9DF),
    val onAccent: Color = Color(0xFF062126),
)

val LocalAdvancedSettingsColors = staticCompositionLocalOf { AdvancedSettingsColors() }

@Composable
fun AdvancedSettingsSection(
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = LocalAdvancedSettingsColors.current
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Text(
            text = title,
            color = colors.text,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        description?.let {
            Spacer(Modifier.height(4.dp))
            Text(
                text = it,
                color = colors.textMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(Modifier.height(14.dp))
        content()
    }
}

@Composable
fun AdvancedSettingsCard(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = LocalAdvancedSettingsColors.current
    val shape = RoundedCornerShape(16.dp)
    val clickableModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    Surface(
        modifier = modifier.fillMaxWidth().then(clickableModifier),
        shape = shape,
        color = if (selected) colors.surfaceRaised else colors.surface,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) colors.accent else colors.outline,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun AdvancedSettingsChoice(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAdvancedSettingsColors.current
    val shape = RoundedCornerShape(13.dp)
    Text(
        text = label,
        color = if (selected) colors.onAccent else colors.text,
        modifier = modifier
            .background(if (selected) colors.accent else colors.surfaceRaised, shape)
            .border(
                width = 1.dp,
                color = if (selected) colors.accent else colors.outline,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        style = MaterialTheme.typography.labelLarge,
    )
}
