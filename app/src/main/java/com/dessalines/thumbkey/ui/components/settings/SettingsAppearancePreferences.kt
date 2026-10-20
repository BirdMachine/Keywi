package com.dessalines.thumbkey.ui.components.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import me.zhanghai.compose.preference.ProvidePreferenceTheme
import me.zhanghai.compose.preference.SwitchPreference

/** Settings chrome is independent of the active keyboard theme and power mode. */
object SettingsAppearancePreferences {
    private const val STORE = "keywi_settings_appearance"
    private const val FOLLOW_KEYBOARD = "follow_keyboard_theme"

    internal fun store(context: Context): SharedPreferences = context.getSharedPreferences(STORE, Context.MODE_PRIVATE)

    fun followsKeyboard(context: Context): Boolean = store(context).getBoolean(FOLLOW_KEYBOARD, true)

    fun setFollowsKeyboard(
        context: Context,
        enabled: Boolean,
    ) {
        store(context).edit().putBoolean(FOLLOW_KEYBOARD, enabled).apply()
    }
}

@Composable
internal fun rememberSettingsFollowKeyboard(): Boolean {
    val context = LocalContext.current
    val preferences = remember(context) { SettingsAppearancePreferences.store(context) }
    var followsKeyboard by remember(preferences) {
        mutableStateOf(SettingsAppearancePreferences.followsKeyboard(context))
    }
    DisposableEffect(preferences) {
        val listener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
                followsKeyboard = SettingsAppearancePreferences.followsKeyboard(context)
            }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        // Also refresh after registration so a change during composition is not missed.
        followsKeyboard = SettingsAppearancePreferences.followsKeyboard(context)
        onDispose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    return followsKeyboard
}

@Composable
fun SettingsAppearancePreference() {
    val context = LocalContext.current
    val followsKeyboard = rememberSettingsFollowKeyboard()
    ProvidePreferenceTheme {
        SwitchPreference(
            modifier = gemPreferenceModifier(),
            value = followsKeyboard,
            onValueChange = { SettingsAppearancePreferences.setFollowsKeyboard(context, it) },
            title = { Text("Follow keyboard theme in settings") },
            summary = {
                Text(
                    if (followsKeyboard) {
                        "Use your keyboard artwork and borders on supported settings pages. Settings animate only in Unbridled mode."
                    } else {
                        "Clean settings without theme artwork or effects. Your keyboard theme stays the same."
                    },
                )
            },
        )
    }
}
