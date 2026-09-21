package dev.neonbytecode.neon.termux

import dev.neonbytecode.neon.themeengine.AnsiPalette
import dev.neonbytecode.neon.themeengine.TermuxColorsParser
import java.io.File

/**
 * What Termux is currently configured to use, derived from the live
 * `~/.termux/colors.properties` and `~/.termux/font.ttf` files.
 *
 * A "Default" state means the file is missing, empty, or contains the default
 * marker written by [TermuxStyleWriter].
 */
data class AppliedStyle(
    val schemeName: String? = null,
    val fontName: String? = null,
    val installedColorsFile: File? = null,
    val installedFontFile: File? = null,
    val installedPalette: AnsiPalette? = null,
) {
    val isDefaultColor: Boolean get() = schemeName == null
    val isDefaultFont: Boolean get() = fontName == null

    companion object {
        val NONE = AppliedStyle()
    }
}

/**
 * Reads and matches the currently installed style against the bundled catalog.
 * Matching is exact (byte equality) so an "APPLIED" badge is never wrong.
 *
 * Candidate assets are short-circuited by size before any byte comparison, and
 * asset reads are memoized by the catalog, so repeated scans stay cheap. Use
 * [installedPalette] (no catalog scan at all) when only the live colors are
 * needed for a preview.
 */
class AppliedStyleReader(private val catalog: StyleCatalog) {

    fun read(state: TermuxEnvironment.State): AppliedStyle {
        val termuxDir = state.termuxDir
        val colorsFile = termuxDir?.let { File(it, TermuxEnvironment.COLORS_PROPERTIES) }
        val fontFile = termuxDir?.let { File(it, TermuxEnvironment.FONT_FILE) }

        val scheme = readScheme(colorsFile)
        val font = readFont(fontFile)

        return AppliedStyle(
            schemeName = scheme?.first,
            fontName = font?.first,
            installedColorsFile = colorsFile,
            installedFontFile = fontFile,
            installedPalette = scheme?.second,
        )
    }

    /** Parses only the installed colors file into a palette; touches no catalog. */
    fun installedPalette(state: TermuxEnvironment.State): AnsiPalette? {
        val file = state.termuxDir?.let { File(it, TermuxEnvironment.COLORS_PROPERTIES) }
        if (file == null || !file.isFile || file.length() == 0L) return null
        val bytes = file.readBytes()
        if (bytes.isEmpty()) return null
        return when {
            String(bytes, Charsets.UTF_8).trimStart().startsWith(TermuxEnvironment.DEFAULT_COLORS_MARKER) -> null
            else -> runCatching {
                TermuxColorsParser.parseProperties(bytes.inputStream(), "Installed")
            }.getOrNull()
        }
    }

    private fun readScheme(file: File?): Pair<String?, AnsiPalette?>? {
        if (file == null || !file.isFile || file.length() == 0L) return null
        val bytes = file.readBytes()
        if (String(bytes, Charsets.UTF_8).trimStart().startsWith(TermuxEnvironment.DEFAULT_COLORS_MARKER)) {
            return null
        }
        val palette = runCatching {
            TermuxColorsParser.parseProperties(bytes.inputStream(), "Installed")
        }.getOrNull()
        return match(bytes, catalog.schemeNames(), catalog::schemeLength, catalog::schemeBytes) to palette
    }

    private fun readFont(file: File?): Pair<String?, Unit>? {
        if (file == null || !file.isFile || file.length() == 0L) return null
        val bytes = file.readBytes()
        return match(bytes, catalog.fontNames(), catalog::fontLength, catalog::fontBytes) to Unit
    }

    private fun match(
        installed: ByteArray,
        candidates: List<String>,
        length: (String) -> Int,
        loader: (String) -> ByteArray,
    ): String? {
        for (name in candidates) {
            if (runCatching { length(name) }.getOrDefault(-1) != installed.size) continue
            val candidate = runCatching { loader(name) }.getOrNull() ?: continue
            if (candidate.contentEquals(installed)) return name
        }
        return null
    }
}