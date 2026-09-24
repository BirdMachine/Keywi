package com.dessalines.thumbkey.ui.components.settings.advancedsound
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dessalines.thumbkey.ui.components.settings.*

@Composable fun AdvancedSoundScreen(modifier:Modifier=Modifier){var mode by remember{mutableStateOf(SoundMode.PLAYLIST)};Column(modifier){AdvancedSettingsSection("Advanced Sound","Mix built-in clips and your own files, then decide how Keywi chooses what you hear."){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){SoundMode.entries.forEach{AdvancedSettingsChoice(it.label,mode==it,{mode=it},Modifier.weight(1f))}};Spacer(Modifier.height(14.dp));when(mode){SoundMode.PLAYLIST->panel("Your sound playlist","Built-in + custom audio • order, loop, or random playback");SoundMode.ACTION->panel("Sounds by action","Letters, numbers, emoji, Unicode, Shift, Enter & tools");SoundMode.POSITION->panel("Sounds by touch position","4 sides + center • corners + sides + center");SoundMode.KEY->panel("Assign sounds on the keyboard","Per-key tap/swipe exceptions override broader rules.")}}}}
private enum class SoundMode(val label:String){PLAYLIST("Playlist"),ACTION("Action"),POSITION("Position"),KEY("Key")}
@Composable private fun panel(title:String,body:String){val c=LocalAdvancedSettingsColors.current;AdvancedSettingsCard{Text(title,color=c.text);Text(body,color=c.textMuted)}}
