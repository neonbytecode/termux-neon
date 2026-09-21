package com.termux.styling

import org.junit.Assert.assertEquals
import org.junit.Test

class TermuxStyleTileServiceTest {

    @Test
    fun computeNextIndex_movesToNextItem() {
        assertEquals(1, TermuxStyleTileService.computeNextIndex(0, 3))
        assertEquals(2, TermuxStyleTileService.computeNextIndex(1, 3))
        assertEquals(0, TermuxStyleTileService.computeNextIndex(2, 3))
    }

    @Test
    fun computeNextIndex_handlesMissingOrEmptyCollections() {
        assertEquals(0, TermuxStyleTileService.computeNextIndex(-1, 3))
        assertEquals(0, TermuxStyleTileService.computeNextIndex(0, 0))
        assertEquals(0, TermuxStyleTileService.computeNextIndex(7, 0))
    }

    @Test
    fun resolveTileLabel_prefersPresetNameWhenMatchingCurrentScheme() {
        val presets = listOf(
            StylingPreset(name = "Night Pulse", schemeName = "dracula.properties", fontName = null),
            StylingPreset(name = "Work Mode", schemeName = "nord.properties", fontName = null),
        )

        assertEquals("Night Pulse", TermuxStyleTileService.resolveTileLabel("dracula.properties", presets))
        assertEquals("Work Mode", TermuxStyleTileService.resolveTileLabel("nord.properties", presets))
    }

    @Test
    fun resolveTileLabel_fallsBackToSchemeName() {
        val presets = listOf(
            StylingPreset(name = "Workspace", schemeName = "solarized_dark.properties", fontName = null),
        )

        assertEquals("catppuccin mocha", TermuxStyleTileService.resolveTileLabel("catppuccin_mocha.properties", emptyList()))
        assertEquals("Workspace", TermuxStyleTileService.resolveTileLabel("solarized_dark.properties", presets))
    }
}
