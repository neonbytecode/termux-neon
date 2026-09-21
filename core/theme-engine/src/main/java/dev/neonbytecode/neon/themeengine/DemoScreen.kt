package dev.neonbytecode.neon.themeengine

/** A colored text run. [color] is an ANSI palette index (0..15) or null = default foreground. */
data class Seg(val text: String, val color: Int? = null, val bold: Boolean = false)

/** A single rendered terminal line made of colored segments. */
data class ScreenLine(val segments: List<Seg>)

/**
 * Generates the fake-but-believable terminal output shown in the live preview.
 * Uses only glyphs that render in standard monospace fonts (no Nerd Font-only
 * symbols), so the demo is safe across every selectable typeface.
 */
object DemoScreen {

    fun build(palette: AnsiPalette, schemeName: String, fontName: String): List<ScreenLine> {
        val cyan = palette.colors[6]
        val brightCyan = palette.colors[14]
        val green = palette.colors[2]
        val brightGreen = palette.colors[10]
        val yellow = palette.colors[3]
        val brightYellow = palette.colors[11]
        val magenta = palette.colors[5]
        val brightMagenta = palette.colors[13]
        val brightWhite = palette.colors[15]
        val dim = palette.colors[7]

        return buildList {
            add(ScreenLine(listOf(Seg("termux-neon ", brightCyan), Seg("style engine ▸ 1.0.0", dim))))
            add(ScreenLine(listOf(Seg("boot seq", dim), Seg("[", dim), Seg("ok", brightGreen), Seg("]", dim), Seg(" module: ", dim), Seg("styling-studio", brightWhite))))
            add(ScreenLine(listOf(Seg("boot seq", dim), Seg("[", dim), Seg("ok", brightGreen), Seg("]", dim), Seg(" theme:  ", dim), Seg(schemeName, brightCyan))))
            add(ScreenLine(listOf(Seg("boot seq", dim), Seg("[", dim), Seg("ok", brightGreen), Seg("]", dim), Seg(" font:   ", dim), Seg(fontName, brightCyan))))
            add(ScreenLine(listOf(Seg(""))))
            add(ScreenLine(listOf(Seg("neon@android", brightGreen), Seg(":", brightWhite), Seg("~", brightWhite), Seg("\$ ", dim), Seg("colors --16", brightWhite))))
            add(ScreenLine(listOf(Seg(" "))))
            add(colorTableRow(palette, 0))
            add(colorTableRow(palette, 8))
            add(ScreenLine(listOf(Seg(" "))))
            add(ScreenLine(listOf(Seg("bg ", dim), Seg("████", palette.background), Seg("  fg ", dim), Seg("██▀ ", palette.foreground), Seg(" cursor ", dim), Seg("▮", palette.cursor))))
            add(ScreenLine(listOf(Seg(" "))))
            add(ScreenLine(listOf(Seg("The quick brown fox jumps over the lazy dog 0123456789", palette.foreground))))
            // Blinking-cursor final line: bold block + cursor glyph.
            add(ScreenLine(listOf(Seg("\$ ", dim), Seg("█", palette.cursor, bold = true))))
        }
    }

    private fun colorTableRow(palette: AnsiPalette, start: Int): ScreenLine {
        val segments = mutableListOf(Seg("  "))
        for (i in start until start + 8) {
            val label = i.toString().padStart(2, '0')
            segments.add(Seg(" $label ", palette.colors[i]))
        }
        return ScreenLine(segments)
    }
}