package com.termux.styling

import java.time.LocalTime

object ThemeRecommendationEngine {
    private val dayModeNames = listOf(
        "solarized-light.properties",
        "catppuccin-latte.properties",
        "gotham.properties",
    )

    private val nightModeNames = listOf(
        "dracula.properties",
        "tokyo-night.properties",
        "nord.properties",
        "rose-pine.properties",
        "catppuccin-mocha.properties",
    )

    fun recommendSchemeName(hourOfDay: Int = LocalTime.now().hour): String? {
        val normalizedHour = hourOfDay.coerceIn(0, 23)
        val candidates = if (normalizedHour in 6..18) dayModeNames else nightModeNames
        return candidates.firstOrNull()
    }
}
