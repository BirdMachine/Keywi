package com.dessalines.thumbkey.ui.components.keyboard.shader

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Experimental, independent shader controls. Opt-in until GPU composition is verified.
 * Kept separate from theme schema so existing imports remain compatible.
 */
object ShaderExperimentPreferences {
    private const val FILE = "keywi_shader_experiment"
    fun material(context: Context) = context.getSharedPreferences(FILE, 0).getBoolean("material", false)
    fun overlay(context: Context) = context.getSharedPreferences(FILE, 0).getBoolean("overlay", false)
    fun setMaterial(context: Context, enabled: Boolean) {
        context.getSharedPreferences(FILE, 0).edit().putBoolean("material", enabled).apply()
    }
    fun setOverlay(context: Context, enabled: Boolean) {
        context.getSharedPreferences(FILE, 0).edit().putBoolean("overlay", enabled).apply()
    }
}

@Composable
fun ShaderExperimentLayer(overlay: Boolean, modifier: Modifier = Modifier) {
    val source = if (overlay) KeywiShaderPresets.SHIMMER else KeywiShaderPresets.MOLTEN
    AndroidView(
        modifier = modifier,
        factory = { context -> KeywiShaderView(context, source) },
    )
}
