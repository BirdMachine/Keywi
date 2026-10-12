package com.dessalines.thumbkey.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import me.zhanghai.compose.preference.Preference

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.activity.compose.LocalActivity
import androidx.navigation.NavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import com.dessalines.thumbkey.R

/** Route-scoped chrome. It never applies a ThemeDocument or writes keyboard preferences. */
fun usesGemSettingsTheme(route: String?): Boolean =
    route != null && route != "setup" && route != "advancedKeyWordSelection"

val LocalKeyboardPreviewColorScheme = staticCompositionLocalOf<ColorScheme?> { null }

fun gemSettingsColors(dark: Boolean): AdvancedSettingsColors = if (dark) AdvancedSettingsColors(
    background = Color(0xFF050E1B), surface = Color(0xFF0B1B2E),
    surfaceRaised = Color(0xFF251333), outline = Color(0xFF344766),
    text = Color(0xFFF4EDFF), textMuted = Color(0xFFC5B6D9),
    accent = Color(0xFFD969FF), onAccent = Color(0xFF180B2A),
) else AdvancedSettingsColors(
    background = Color(0xFFFAF7FF), surface = Color(0xFFF1EAFB),
    surfaceRaised = Color(0xFFE4D4FF), outline = Color(0xFFC0AED5),
    text = Color(0xFF211332), textMuted = Color(0xFF665274),
    accent = Color(0xFF7434BD), onAccent = Color.White,
)

@Composable
fun GemSettingsTheme(enabled: Boolean, content: @Composable () -> Unit) {
    if (!enabled) { content(); return }
    val keyboardScheme = MaterialTheme.colorScheme
    val dark = keyboardScheme.background.luminance() < .5f
    val colors = gemSettingsColors(dark)
    val scheme = keyboardScheme.copy(
        primary = colors.accent, onPrimary = colors.onAccent,
        primaryContainer = colors.surfaceRaised, onPrimaryContainer = colors.text,
        secondary = if (dark) Color(0xFFA9A6FF) else Color(0xFF5952A4),
        secondaryContainer = colors.surfaceRaised, onSecondaryContainer = colors.text,
        tertiary = if (dark) Color(0xFF71DDE8) else Color(0xFF006875),
        background = colors.background, onBackground = colors.text,
        surface = colors.background, onSurface = colors.text,
        surfaceVariant = colors.surface, onSurfaceVariant = colors.textMuted,
        surfaceContainer = colors.surface, surfaceContainerLow = colors.surface,
        surfaceContainerLowest = colors.background, surfaceContainerHigh = colors.surfaceRaised,
        surfaceContainerHighest = colors.surfaceRaised,
        outline = colors.outline, outlineVariant = colors.outline.copy(alpha = .65f),
        inversePrimary = if (dark) Color(0xFF7434BD) else Color(0xFFD969FF),
        surfaceTint = colors.accent,
    )
    CompositionLocalProvider(
        LocalAdvancedSettingsColors provides colors,
        LocalKeyboardPreviewColorScheme provides keyboardScheme,
    ) {
        MaterialTheme(colorScheme = scheme, shapes = Shapes(
            extraSmall = CutCornerShape(3.dp), small = RoundedCornerShape(6.dp),
            medium = RoundedCornerShape(10.dp), large = RoundedCornerShape(12.dp),
            extraLarge = RoundedCornerShape(16.dp),
        ), content = content)
    }
}

/** Decorates existing preference controls without replacing their dialog or persistence logic. */
@Composable
fun gemPreferenceModifier(selected: Boolean = false): Modifier {
    val colors = LocalAdvancedSettingsColors.current
    val shape = RoundedCornerShape(10.dp)
    return Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
        .clip(shape)
        .background(Brush.horizontalGradient(listOf(
            if (selected) colors.surfaceRaised else colors.surface,
            colors.surface,
        )))
        .border(if (selected) 1.5.dp else 1.dp, if (selected) colors.accent else colors.outline, shape)
}

@Composable
fun GemNavigationPreference(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    summary: @Composable (() -> Unit)? = null,
    widgetContainer: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false,
    showChevron: Boolean = true,
) {
    val trailing: @Composable (() -> Unit)? = widgetContainer ?: if (showChevron && onClick != null) {
        { Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary) }
    } else null
    Preference(title = title, modifier = modifier.then(gemPreferenceModifier(selected)),
        enabled = enabled, icon = icon, summary = summary, onClick = onClick,
        widgetContainer = trailing,
    )
}

@Composable
fun GemSettingsCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        content = content)
}

@Composable
fun gemSectionModifier(): Modifier {
    val colors = LocalAdvancedSettingsColors.current
    val shape = RoundedCornerShape(10.dp)
    return Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 7.dp)
        .clip(shape).background(colors.surface)
        .border(1.dp, colors.outline, shape).padding(12.dp)
}

@Composable
fun GemSettingsGroupLabel(title: String, description: String) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(title, color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge)
        Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GemSettingsTopAppBar(
    text: String,
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    showBack: Boolean = true,
) {
    val activity = LocalActivity.current
    TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Outlined.Diamond, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        navigationIcon = {
            if (showBack) IconButton(onClick = {
                if (navController.previousBackStackEntry == null) activity?.finish() else navController.popBackStack()
            }) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.settings),
                    tint = MaterialTheme.colorScheme.primary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
        ),
    )
}
