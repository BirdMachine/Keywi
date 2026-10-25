package com.dessalines.thumbkey.ui.components.keyboard

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.IMEService
import com.dessalines.thumbkey.inputcontext.ContextEnginePolicy
import com.dessalines.thumbkey.inputcontext.ContextEnginePreferences

/** Tool pages hosted above the character keyboard and selected by the toolbar cycler. */
enum class InputPalette {
    KAOMOJI,
    FANCY_TEXT,
    QUICK_SETTINGS,
}

@Composable
fun ExpandedInputPaletteHost(
    palette: InputPalette,
    ime: IMEService,
    vibrateOnTap: Boolean,
    soundOnTap: Boolean,
    onDismiss: () -> Unit,
    keyboardHeight: Dp,
    modifier: Modifier = Modifier,
    onFancyApply: (String) -> Unit = {},
    onSwitchPalette: (InputPalette) -> Unit = {},
    onPreferencesChanged: () -> Unit = {},
) {
    val capabilities =
        ContextEnginePolicy.evaluate(
            context = ime.inputContext,
            settings = ContextEnginePreferences.load(ime),
        )
    if (!capabilities.canOfferInputPalettes && palette != InputPalette.QUICK_SETTINGS) return

    val configuration = LocalConfiguration.current
    val view = LocalView.current

    val searchActive = palette == InputPalette.KAOMOJI && PaletteSearchCapture.active
    val browseHeight =
        (configuration.screenHeightDp * 0.62f)
            .coerceIn(380f, 680f)
            .dp
    val searchHeight =
        (configuration.screenHeightDp * 0.78f)
            .coerceIn(460f, 780f)
            .dp
    val targetHeight = if (searchActive) searchHeight else browseHeight
    val targetContentHeight =
        if (searchActive) {
            (searchHeight - keyboardHeight).coerceAtLeast(190.dp)
        } else {
            browseHeight
        }
    val animatedHeight by
        animateDpAsState(
            targetValue = targetHeight,
            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
            label = "palette-height",
        )
    val animatedContentHeight by
        animateDpAsState(
            targetValue = targetContentHeight,
            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
            label = "palette-content-height",
        )

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(animatedHeight),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(animatedContentHeight),
        ) {
            ToolPageTheme {
            Column(modifier = Modifier.fillMaxSize().toolPageBackground()) {
                when (palette) {
                    InputPalette.QUICK_SETTINGS -> {
                        QuickSettingsWorkspace(onPreferencesChanged = onPreferencesChanged)
                    }

                    InputPalette.FANCY_TEXT -> {
                        FancyTextWorkspace(onApply = onFancyApply, modifier = Modifier.fillMaxSize())
                    }

                    InputPalette.KAOMOJI -> {
                        KaomojiRoom(
                            onCommit = { text ->
                                if ((vibrateOnTap || com.dessalines.thumbkey.effects.FeedbackPreferences.get(ime) == com.dessalines.thumbkey.effects.FeedbackMode.VIBRATE) && com.dessalines.thumbkey.effects.FeedbackPreferences.hapticsAllowed(ime)) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                com.dessalines.thumbkey.effects.ActionSoundEngine.fire(ime)
                                PaletteSearchCapture.bypass {
                                    ime.currentInputConnection?.commitText(text, 1)
                                }
                            },
                            onBackToLetters = onDismiss,
                            onGoToEmoji = null,
                            showDismissButton = false,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
            }
        }
    }
}
