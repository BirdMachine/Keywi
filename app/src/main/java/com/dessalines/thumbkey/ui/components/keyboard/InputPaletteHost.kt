package com.dessalines.thumbkey.ui.components.keyboard

import android.content.Context
import android.media.AudioManager
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.IMEService
import com.dessalines.thumbkey.inputcontext.ContextEnginePolicy
import com.dessalines.thumbkey.inputcontext.ContextEnginePreferences

enum class InputPalette { KAOMOJI }

@Composable
fun ExpandedInputPaletteHost(palette:InputPalette,ime:IMEService,vibrateOnTap:Boolean,soundOnTap:Boolean,onDismiss:()->Unit,keyboardHeight:Dp,modifier:Modifier=Modifier) {
    if (!ContextEnginePolicy.evaluate(ime.inputContext, ContextEnginePreferences.load(ime)).canOfferInputPalettes) return
    val view=LocalView.current; val audio=ime.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val total=(LocalConfiguration.current.screenHeightDp*0.72f).coerceIn(420f,760f).dp
    val content=(total-keyboardHeight).coerceAtLeast(210.dp)
    Box(modifier.fillMaxWidth().height(total), contentAlignment=Alignment.TopCenter) {
        Box(Modifier.fillMaxWidth().height(content)) {
            val backdrop=BackdropThemePreferences.load(ime)
            if(backdrop.mode!=BackdropMode.NONE) BackdropVisualLayer(backdrop,Modifier.fillMaxSize()) else Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface.copy(alpha=.94f)))
            when(palette){ InputPalette.KAOMOJI -> KaomojiRoom(
                onCommit={ text -> if(vibrateOnTap)view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); if(soundOnTap)audio.playSoundEffect(AudioManager.FX_KEY_CLICK,.1f); PaletteSearchCapture.bypass{ime.currentInputConnection?.commitText(text,1)} },
                onBackToLetters=onDismiss,
                modifier=Modifier.fillMaxSize()
            ) }
        }
    }
}
