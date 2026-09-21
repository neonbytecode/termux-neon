package com.termux.styling

import dev.neonbytecode.neon.themeengine.AnsiPalette
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeHealthCheckerTest {
    @Test
    fun inspect_flagsPoorContrast() {
        val palette = AnsiPalette.DEFAULT.copy(foreground = 0xFF222222.toInt(), background = 0xFF202020.toInt())

        assertFalse(ThemeHealthChecker.inspect(palette).readable)
    }

    @Test
    fun inspect_acceptsHighContrastPalette() {
        val palette = AnsiPalette.DEFAULT.copy(foreground = 0xFFFFFFFF.toInt(), background = 0xFF000000.toInt())

        assertTrue(ThemeHealthChecker.inspect(palette).readable)
    }
}
