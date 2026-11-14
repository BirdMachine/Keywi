package com.dessalines.thumbkey.ui.components.keyboard

import android.content.Context

object KeywiAppearancePreferences {
    private const val PREFS = "keywi_appearance_preferences"
    private const val KEY_ENABLED = "custom_keyboard_backgrounds_enabled"

    private var storedEnabled: Boolean = true
    val currentEnabled: Boolean get() = storedEnabled && KeywiPowerPreferences.advancedThemingEnabled

    fun load(context: Context): Boolean {
        storedEnabled =
            context
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_ENABLED, true)
        return storedEnabled
    }

    fun save(
        context: Context,
        enabled: Boolean,
    ) {
        storedEnabled = enabled
        context
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ENABLED, enabled)
            .apply()
    }
}
