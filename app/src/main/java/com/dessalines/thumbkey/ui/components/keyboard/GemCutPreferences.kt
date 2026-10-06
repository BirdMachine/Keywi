package com.dessalines.thumbkey.ui.components.keyboard

import android.content.Context

/**
 * Persists the physical keyboard geometry independently from layout and theme.
 *
 * ME-Like remains the safe default for existing installs. Layout selection and
 * visual theme are intentionally stored elsewhere.
 */
object GemCutPreferences {
    private const val PREFS = "keywi_gem_cut_preferences"
    private const val KEY_FACET = "active_facet"

    fun load(context: Context): FacetId {
        val saved = context
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_FACET, FacetId.ME_LIKE.name)
        return runCatching { FacetId.valueOf(saved ?: FacetId.ME_LIKE.name) }
            .getOrDefault(FacetId.ME_LIKE)
    }

    fun save(context: Context, facet: FacetId) {
        context
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FACET, facet.name)
            .apply()
    }
}
