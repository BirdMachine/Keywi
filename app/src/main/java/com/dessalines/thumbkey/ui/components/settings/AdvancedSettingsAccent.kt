package com.dessalines.thumbkey.ui.components.settings
import androidx.compose.ui.graphics.Color

enum class AdvancedSettingsAccent(val label:String,val color:Color){AQUA("Aqua",Color(0xFF6ED9DF)),KIWI("Kiwi",Color(0xFFC8EE54)),PINK("Pink",Color(0xFFFF72CE)),VIOLET("Violet",Color(0xFFA889FF)),SKY("Sky",Color(0xFF65C8FF)),SUN("Sun",Color(0xFFFFD45F))}
fun AdvancedSettingsColors.withAccent(a:AdvancedSettingsAccent)=copy(accent=a.color,onAccent=if(a==AdvancedSettingsAccent.SUN||a==AdvancedSettingsAccent.KIWI)Color(0xFF172006)else Color(0xFF071B21))
