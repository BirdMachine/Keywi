package com.dessalines.thumbkey.ui.components.keyboard

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import java.io.File

object FontPreferences {
    private const val PREFS = "keyboard_font_preferences"
    private const val KEY_FILE = "font_file"
    private const val KEY_NAME = "font_name"

    fun displayName(context: Context): String =
        context
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_NAME, null) ?: "System default"

    fun importFont(
        context: Context,
        uri: Uri,
    ): Boolean =
        runCatching {
            val name = uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() } ?: "Custom font"
            val extension = if (name.lowercase().endsWith(".otf")) "otf" else "ttf"
            val ownedUri = ThemeAssetStore.copySelected(context, uri, extension)
            val target = File(requireNotNull(Uri.parse(ownedUri).path))
            Typeface.createFromFile(target)
            context
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_FILE, target.absolutePath)
                .putString(KEY_NAME, name)
                .apply()
            true
        }.getOrDefault(false)

    fun clear(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    fun snapshot(context: Context): ThemeFont? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_FILE, null)?.let { ThemeFont(Uri.fromFile(File(it)).toString(), displayName(context)) }
    }

    fun applyTheme(
        context: Context,
        font: ThemeFont?,
    ) {
        if (font == null) {
            clear(context)
            return
        }
        val path = Uri.parse(font.asset).path ?: return
        context
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FILE, path)
            .putString(KEY_NAME, font.name)
            .apply()
    }

    fun typeface(context: Context): Typeface? {
        val path = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_FILE, null) ?: return null
        return runCatching { Typeface.createFromFile(path) }.getOrNull()
    }
}
