package dev.neonbytecode.neon.termux

/** Capitalizes every word of a hyphen/underscore/space separated name for
 *  display, mapping separators to spaces and preserving existing capitals
 *  (e.g. `JetBrains-Mono-NG` → `JetBrains Mono NG`, `rosé-pine` → `Rosé Pine`). */
fun capitalizeName(str: String): String {
    val words = str.split(Regex("""[-_\s]+""")).filter { it.isNotBlank() }
    return words.joinToString(" ") { word ->
        word.replaceFirstChar { it.uppercaseChar() }
    }
}

/**
 * A bundle asset (color scheme or font) selectable by the user. The display
 * name is derived from the file name, e.g. `base16-cyberpunk-dark.properties`
 * → `Base16 Cyberpunk Dark`.
 */
class Selectable(private val fileName: String) {
    val displayName: String

    val name: String get() = fileName

    init {
        var name = fileName.replace('-', ' ')
        val dotIndex = name.lastIndexOf('.')
        if (dotIndex != -1) name = name.substring(0, dotIndex)
        this.displayName = if (name == DEFAULT_FILENAME) DEFAULT_DISPLAY_NAME else capitalizeName(name)
    }

    override fun toString(): String = displayName

    override fun equals(other: Any?): Boolean = other is Selectable && other.fileName == fileName

    override fun hashCode(): Int = fileName.hashCode()

    companion object {
        const val DEFAULT_FILENAME = "Default"
        const val DEFAULT_DISPLAY_NAME = "Default"
    }
}