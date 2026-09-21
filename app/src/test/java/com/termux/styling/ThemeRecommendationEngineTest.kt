package com.termux.styling

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeRecommendationEngineTest {

    @Test
    fun recommendSchemeName_usesDayModeDuringDaylightHours() {
        assertEquals("solarized-light.properties", ThemeRecommendationEngine.recommendSchemeName(9))
        assertEquals("solarized-light.properties", ThemeRecommendationEngine.recommendSchemeName(13))
    }

    @Test
    fun recommendSchemeName_usesNightModeOutsideDaylightHours() {
        assertEquals("dracula.properties", ThemeRecommendationEngine.recommendSchemeName(0))
        assertEquals("dracula.properties", ThemeRecommendationEngine.recommendSchemeName(22))
    }

    @Test
    fun recommendSchemeName_clampsToValidHourRange() {
        assertEquals("dracula.properties", ThemeRecommendationEngine.recommendSchemeName(-5))
        assertEquals("dracula.properties", ThemeRecommendationEngine.recommendSchemeName(99))
    }
}
