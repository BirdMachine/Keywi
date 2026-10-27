package com.dessalines.thumbkey.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.dessalines.thumbkey.db.AppSettings
import com.dessalines.thumbkey.utils.ThemeColor
import com.dessalines.thumbkey.utils.ThemeMode

@Composable
fun ThumbkeyTheme(
    settings: AppSettings?,
    content: @Composable () -> Unit,
) {
    val power = com.dessalines.thumbkey.ui.components.keyboard.rememberKeywiPowerMode()
    if (power == com.dessalines.thumbkey.ui.components.keyboard.KeywiPowerMode.DESPERATE) {
        val colors = if (isSystemInDarkTheme()) androidx.compose.material3.darkColorScheme() else androidx.compose.material3.lightColorScheme()
        MaterialTheme(colorScheme = colors, typography = androidx.compose.material3.Typography(), content = content)
        return
    }
    val themeMode = ThemeMode.entries[settings?.theme ?: 0]
    val themeColor = ThemeColor.entries[settings?.themeColor ?: 0]

    val ctx = LocalContext.current
    val android12OrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    // Dynamic schemes crash on lower than android 12
    val dynamicPair =
        if (android12OrLater) {
            Pair(dynamicLightColorScheme(ctx), dynamicDarkColorScheme(ctx))
        } else {
            pink()
        }

    val colorPair =
        when (themeColor) {
            ThemeColor.Dynamic -> dynamicPair
            ThemeColor.Green -> green()
            ThemeColor.Pink -> pink()
            ThemeColor.Matrix -> matrix()
            ThemeColor.Srcery -> srcery()
            ThemeColor.Blue -> blue()
            ThemeColor.Dracula -> dracula()
            ThemeColor.Twilight -> twilight()
            ThemeColor.HighContrast -> highContrast()
            ThemeColor.HighContrastColorful -> highContrastColorful()
            ThemeColor.Ancom -> ancom()
            ThemeColor.Neon -> neon()
        }

    val systemTheme =
        if (!isSystemInDarkTheme()) {
            colorPair.first
        } else {
            colorPair.second
        }

    val colors =
        when (themeMode) {
            ThemeMode.System -> systemTheme
            ThemeMode.Light -> colorPair.first
            ThemeMode.Dark -> colorPair.second
        }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}
