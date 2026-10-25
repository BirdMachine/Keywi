package com.dessalines.thumbkey.ui.components.keyboard

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Physical-key/group color overrides apply to both center and directional glyphs. */
internal val LocalKeyGlyphColor = staticCompositionLocalOf<Color?> { null }
