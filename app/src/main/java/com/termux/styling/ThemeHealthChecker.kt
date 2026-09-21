package com.termux.styling

import dev.neonbytecode.neon.themeengine.AnsiPalette
import kotlin.math.max
import kotlin.math.min

data class ThemeHealth(
    val contrastRatio: Double,
    val readable: Boolean,
    val warnings: List<String>,
)

object ThemeHealthChecker {
    fun inspect(palette: AnsiPalette): ThemeHealth {
        val ratio = contrastRatio(palette.foreground, palette.background)
        val warnings = buildList {
            if (ratio < 4.5) add("Foreground contrast is below WCAG AA for normal text.")
            if (palette.cursor == palette.background) add("Cursor color matches the background.")
            if (palette.colors.distinct().size < 8) add("The ANSI palette has low color variety.")
        }
        return ThemeHealth(ratio, warnings.isEmpty(), warnings)
    }

    private fun contrastRatio(first: Int, second: Int): Double {
        val lighter = max(relativeLuminance(first), relativeLuminance(second))
        val darker = min(relativeLuminance(first), relativeLuminance(second))
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun relativeLuminance(color: Int): Double {
        fun channel(value: Int): Double {
            val normalized = value / 255.0
            return if (normalized <= 0.03928) normalized / 12.92
            else ((normalized + 0.055) / 1.055).let { it * it * it }
        }

        val red = channel(color shr 16 and 0xFF)
        val green = channel(color shr 8 and 0xFF)
        val blue = channel(color and 0xFF)
        return 0.2126 * red + 0.7152 * green + 0.0722 * blue
    }
}
