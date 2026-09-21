package dev.neonbytecode.neon.themeengine

/** A colored text run. [color] is an ANSI palette index (0..15) or null = default foreground. */
data class Seg(val text: String, val color: Int? = null, val bold: Boolean = false)

/** A single rendered terminal line made of colored segments. */
data class ScreenLine(val segments: List<Seg>)

enum class PreviewMode(val label: String) {
    PROMPT("Prompt"),
    FASTFETCH("System Info"),
    CODE("Code"),
    CUSTOM("Custom"),
}

/**
 * Generates the fake-but-believable terminal output shown in the live preview.
 * Uses only glyphs that render in standard monospace fonts (no Nerd Font-only
 * symbols), so the demo is safe across every selectable typeface.
 */
object DemoScreen {

    fun build(
        palette: AnsiPalette,
        schemeName: String,
        fontName: String,
        mode: PreviewMode = PreviewMode.PROMPT,
        customText: String = "",
    ): List<ScreenLine> = when (mode) {
        PreviewMode.PROMPT -> buildPrompt(palette, schemeName, fontName)
        PreviewMode.FASTFETCH -> buildFastfetch(palette, schemeName, fontName)
        PreviewMode.CODE -> buildCode(palette, schemeName)
        PreviewMode.CUSTOM -> buildCustom(palette, customText)
    }

    private fun buildPrompt(palette: AnsiPalette, schemeName: String, fontName: String): List<ScreenLine> {
        val brightCyan = palette.colors[14]
        val brightGreen = palette.colors[10]
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

    private fun buildFastfetch(palette: AnsiPalette, schemeName: String, fontName: String): List<ScreenLine> {
        val green = palette.colors[2]
        val yellow = palette.colors[3]
        val magenta = palette.colors[5]
        val cyan = palette.colors[6]
        val dim = palette.colors[7]

        return buildList {
            add(ScreenLine(listOf(Seg("neon@android", cyan, bold = true))))
            add(ScreenLine(listOf(Seg("------------", dim))))
            add(ScreenLine(listOf(Seg("OS:       ", cyan, bold = true), Seg("Android / Termux (aarch64)", palette.foreground))))
            add(ScreenLine(listOf(Seg("Host:     ", cyan, bold = true), Seg("Termux Neon App", palette.foreground))))
            add(ScreenLine(listOf(Seg("Kernel:   ", cyan, bold = true), Seg("Linux 6.1-android14", palette.foreground))))
            add(ScreenLine(listOf(Seg("Shell:    ", cyan, bold = true), Seg("zsh 5.9", palette.foreground))))
            add(ScreenLine(listOf(Seg("Theme:    ", cyan, bold = true), Seg(schemeName, green))))
            add(ScreenLine(listOf(Seg("Font:     ", cyan, bold = true), Seg(fontName, yellow))))
            add(ScreenLine(listOf(Seg("Terminal: ", cyan, bold = true), Seg("com.termux.styling", magenta))))
            add(ScreenLine(listOf(Seg(""))))
            add(ScreenLine(listOf(Seg("Palette: ", dim))))
            add(colorTableRow(palette, 0))
            add(colorTableRow(palette, 8))
            add(ScreenLine(listOf(Seg(""))))
            add(ScreenLine(listOf(Seg("\$ ", dim), Seg("█", palette.cursor, bold = true))))
        }
    }

    private fun buildCode(palette: AnsiPalette, schemeName: String): List<ScreenLine> {
        val red = palette.colors[1]
        val green = palette.colors[2]
        val yellow = palette.colors[3]
        val blue = palette.colors[4]
        val magenta = palette.colors[5]
        val cyan = palette.colors[6]
        val dim = palette.colors[7]
        val brightWhite = palette.colors[15]

        return buildList {
            add(ScreenLine(listOf(Seg("#!/usr/bin/env python3", dim))))
            add(ScreenLine(listOf(Seg("# Termux Neon - $schemeName", dim))))
            add(ScreenLine(listOf(Seg("import", magenta, bold = true), Seg(" sys, os", brightWhite))))
            add(ScreenLine(listOf(Seg(""))))
            add(ScreenLine(listOf(Seg("def ", blue, bold = true), Seg("apply_style", yellow), Seg("(scheme: ", brightWhite), Seg("str", cyan), Seg(") -> ", brightWhite), Seg("bool", cyan), Seg(":", brightWhite))))
            add(ScreenLine(listOf(Seg("    print", blue), Seg("(", brightWhite), Seg("f\"[OK] Theme applied: {scheme}\"", green), Seg(")", brightWhite))))
            add(ScreenLine(listOf(Seg("    return ", magenta, bold = true), Seg("True", red, bold = true))))
            add(ScreenLine(listOf(Seg(""))))
            add(ScreenLine(listOf(Seg("if ", magenta, bold = true), Seg("__name__ == \"__main__\":", brightWhite))))
            add(ScreenLine(listOf(Seg("    apply_style", yellow), Seg("(", brightWhite), Seg("\"$schemeName\"", green), Seg(")", brightWhite))))
            add(ScreenLine(listOf(Seg(""))))
            add(ScreenLine(listOf(Seg("\$ ", dim), Seg("python3 theme.py", brightWhite))))
            add(ScreenLine(listOf(Seg("[OK] Theme applied: $schemeName", green))))
            add(ScreenLine(listOf(Seg("\$ ", dim), Seg("█", palette.cursor, bold = true))))
        }
    }

    private fun buildCustom(palette: AnsiPalette, customText: String): List<ScreenLine> {
        val textToRender = customText.ifBlank {
            "The quick brown fox jumps over the lazy dog\n0123456789 ~!@#$%^&*()_+\nABCDEFGHIJKLMNOPQRSTUVWXYZ\nabcdefghijklmnopqrstuvwxyz"
        }
        val dim = palette.colors[7]
        val lines = textToRender.split("\n")

        return buildList {
            lines.forEach { lineText ->
                add(ScreenLine(listOf(Seg(lineText, palette.foreground))))
            }
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