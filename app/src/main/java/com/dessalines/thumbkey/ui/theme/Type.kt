package com.dessalines.thumbkey.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.dessalines.thumbkey.R

val Manrope = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
)

private val defaults = Typography()

// App chrome uses Manrope. Explicit keyboard and user-selected font families remain independent.
val Typography = Typography(
    displayLarge = defaults.displayLarge.copy(fontFamily = Manrope),
    displayMedium = defaults.displayMedium.copy(fontFamily = Manrope),
    displaySmall = defaults.displaySmall.copy(fontFamily = Manrope),
    headlineLarge = defaults.headlineLarge.copy(fontFamily = Manrope),
    headlineMedium = defaults.headlineMedium.copy(fontFamily = Manrope),
    headlineSmall = defaults.headlineSmall.copy(fontFamily = Manrope),
    titleLarge = defaults.titleLarge.copy(fontFamily = Manrope),
    titleMedium = defaults.titleMedium.copy(fontFamily = Manrope),
    titleSmall = defaults.titleSmall.copy(fontFamily = Manrope),
    bodyLarge = defaults.bodyLarge.copy(fontFamily = Manrope),
    bodyMedium = defaults.bodyMedium.copy(fontFamily = Manrope),
    bodySmall = defaults.bodySmall.copy(fontFamily = Manrope),
    labelLarge = defaults.labelLarge.copy(fontFamily = Manrope),
    labelMedium = defaults.labelMedium.copy(fontFamily = Manrope),
    labelSmall = defaults.labelSmall.copy(fontFamily = Manrope),
)
