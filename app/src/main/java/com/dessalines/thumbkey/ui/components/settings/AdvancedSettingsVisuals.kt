package com.dessalines.thumbkey.ui.components.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

data class AdvancedSettingsColors(val background: Color=Color(0xFF071B21),val surface: Color=Color(0xFF102A32),val surfaceRaised: Color=Color(0xFF183841),val outline: Color=Color(0xFF315761),val text: Color=Color(0xFFF2FAFB),val textMuted: Color=Color(0xFFA8BEC4),val accent: Color=Color(0xFF6ED9DF),val onAccent: Color=Color(0xFF062126))
val LocalAdvancedSettingsColors=staticCompositionLocalOf{AdvancedSettingsColors()}

@Composable fun AdvancedSettingsSection(title:String,description:String?=null,modifier:Modifier=Modifier,content:@Composable()->Unit){val c=LocalAdvancedSettingsColors.current;Column(modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=12.dp)){Text(title,color=c.text,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);description?.let{Spacer(Modifier.height(4.dp));Text(it,color=c.textMuted,style=MaterialTheme.typography.bodyMedium)};Spacer(Modifier.height(14.dp));content()}}
@Composable fun AdvancedSettingsCard(modifier:Modifier=Modifier,selected:Boolean=false,onClick:(()->Unit)?=null,content:@Composable()->Unit){val c=LocalAdvancedSettingsColors.current;val shape=RoundedCornerShape(16.dp);val click=if(onClick!=null)Modifier.clickable(onClick=onClick)else Modifier;Surface(modifier.fillMaxWidth().then(click),shape=shape,color=if(selected)c.surfaceRaised else c.surface,border=BorderStroke(if(selected)1.5.dp else 1.dp,if(selected)c.accent else c.outline)){Column(Modifier.padding(16.dp)){content()}}}
@Composable fun AdvancedSettingsChoice(label:String,selected:Boolean,onClick:()->Unit,modifier:Modifier=Modifier){val c=LocalAdvancedSettingsColors.current;val shape=RoundedCornerShape(13.dp);Text(label,color=if(selected)c.onAccent else c.text,modifier.background(if(selected)c.accent else c.surfaceRaised,shape).border(1.dp,if(selected)c.accent else c.outline,shape).clickable(onClick=onClick).padding(horizontal=14.dp,vertical=11.dp),style=MaterialTheme.typography.labelLarge)}
