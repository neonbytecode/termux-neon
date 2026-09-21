package com.termux.styling

import android.content.Context
import androidx.core.content.edit

/**
 * Persists starred scheme/font names across launches. Backed by a plain
 * SharedPreferences string set — the catalog is small (15 schemes, 26 fonts)
 * so no heavier storage is warranted.
 */
class FavoritesStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun schemes(): Set<String> = prefs.getStringSet(KEY_SCHEMES, emptySet()).orEmpty()

    fun fonts(): Set<String> = prefs.getStringSet(KEY_FONTS, emptySet()).orEmpty()

    fun toggleScheme(name: String): Set<String> = toggle(KEY_SCHEMES, name)

    fun toggleFont(name: String): Set<String> = toggle(KEY_FONTS, name)

    private fun toggle(key: String, name: String): Set<String> {
        val current = prefs.getStringSet(key, emptySet()).orEmpty().toMutableSet()
        if (!current.add(name)) current.remove(name)
        prefs.edit { putStringSet(key, current) }
        return current
    }

    companion object {
        private const val PREFS_NAME = "neon_favorites"
        private const val KEY_SCHEMES = "schemes"
        private const val KEY_FONTS = "fonts"
    }
}
