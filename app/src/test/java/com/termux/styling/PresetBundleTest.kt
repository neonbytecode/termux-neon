package com.termux.styling

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PresetBundleTest {
    @Test
    fun roundTrip_preservesPresets() {
        val presets = listOf(
            StylingPreset(id = "one", name = "Night", schemeName = "dracula.properties", fontName = "Fira-Code.ttf"),
            StylingPreset(id = "two", name = "Reading", schemeName = null, fontName = null, textColorOverride = 0xFF00FF),
        )

        val decoded = PresetBundle.decode(PresetBundle.encode(presets)).getOrThrow()

        assertEquals(presets, decoded)
    }

    @Test
    fun decode_rejectsUnknownVersions() {
        val result = PresetBundle.decode("""{"version":99,"presets":[]}""")

        assertTrue(result.isFailure)
    }
}
