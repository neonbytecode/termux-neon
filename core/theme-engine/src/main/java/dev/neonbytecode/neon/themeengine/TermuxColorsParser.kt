package dev.neonbytecode.neon.themeengine

import java.io.InputStream
import java.util.Properties

/**
 * Parses Termux `colors.properties` files (and the compatible base16 /
 * developer `properties` variants) into an [AnsiPalette].
 *
 * Supported keys: `color0`..`color15`, `foreground`, `background`, `cursor`.
 * Values are `#RRGGBB`, `RRGGBB` or `0xRRGGBB` (8-digit `AARRGGBB` is also
 * accepted). Missing values fall back to the classic xterm defaults.
 */
object TermuxColorsParser {

    fun parseProperties(input: InputStream, name: String = AnsiPalette.DEFAULT.name): AnsiPalette {
        val props = Properties()
        props.load(input)
        return fromProperties(props, name)
    }

    fun fromProperties(props: Properties, name: String = AnsiPalette.DEFAULT.name): AnsiPalette {
        val colors = (0 until 16).map { i ->
            propIgnoreCase(props, "color$i")?.let { parseHexColor(it) } ?: AnsiPalette.DEFAULT_COLORS[i]
        }
        val foreground = propIgnoreCase(props, "foreground")?.let { parseHexColor(it) } ?: AnsiPalette.DEFAULT_FOREGROUND
        val background = propIgnoreCase(props, "background")?.let { parseHexColor(it) } ?: AnsiPalette.DEFAULT_BACKGROUND
        val cursor = propIgnoreCase(props, "cursor")?.let { parseHexColor(it) } ?: AnsiPalette.DEFAULT_CURSOR
        return AnsiPalette(colors, foreground, background, cursor, name)
    }

    /** Looks up a property ignoring key case (`BACKGROUND` ≡ `background`). */
    private fun propIgnoreCase(props: Properties, key: String): String? {
        props.getProperty(key)?.let { return it }
        for ((k, v) in props) {
            if (k is String && k.equals(key, ignoreCase = true)) return v as? String
        }
        return null
    }

    /** Parses `#RRGGBB`, `RRGGBB`, `0xRRGGBB` and 8-digit `AARRGGBB`. Returns null on failure. */
    fun parseHexColor(raw: String?): Int? {
        if (raw == null) return null
        var value = raw.trim()
        if (value.isEmpty()) return null
        if (value.startsWith("#")) value = value.substring(1)
        if (value.length >= 2 && (value[0] == '0') && (value[1] == 'x' || value[1] == 'X')) {
            value = value.substring(2)
        }
        if (value.length == 8) {
            val alpha = value.take(2).toIntOrNull(16) ?: return null
            val rgb = value.drop(2).toIntOrNull(16) ?: return null
            return (alpha shl 24) or rgb
        }
        if (value.length != 6) return null
        val rgb = value.toIntOrNull(16) ?: return null
        return 0xFF000000.toInt() or rgb
    }
}