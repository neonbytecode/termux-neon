package dev.neonbytecode.neon.themeengine

/**
 * Root color palette used by a terminal: the 16 ANSI colors plus the default
 * foreground, background and cursor colors, as read from Termux
 * `colors.properties`.
 */
data class AnsiPalette(
    /** ANSI colors, index 0..7 = normal, 8..15 = bright. ARGB ints. */
    val colors: List<Int>,
    val foreground: Int,
    val background: Int,
    val cursor: Int,
    val name: String,
) {
    val normal: List<Int> get() = colors.subList(0, 8)
    val bright: List<Int> get() = colors.subList(8, 16)

    companion object {
        /** Terminal.app / classic xterm defaults. */
        val DEFAULT_COLORS: List<Int> = listOf(
            0xFF000000.toInt(), 0xFFAA0000.toInt(), 0xFF00AA00.toInt(), 0xFFAA5500.toInt(),
            0xFF0000AA.toInt(), 0xFFAA00AA.toInt(), 0xFF00AAAA.toInt(), 0xFFAAAAAA.toInt(),
            0xFF555555.toInt(), 0xFFFF5555.toInt(), 0xFF55FF55.toInt(), 0xFFFFFF55.toInt(),
            0xFF5555FF.toInt(), 0xFFFF55FF.toInt(), 0xFF55FFFF.toInt(), 0xFFFFFFFF.toInt(),
        )
        val DEFAULT_FOREGROUND: Int = 0xFFC0C0C0.toInt()
        val DEFAULT_BACKGROUND: Int = 0xFF000000.toInt()
        val DEFAULT_CURSOR: Int = 0xFFC0C0C0.toInt()

        val DEFAULT = AnsiPalette(DEFAULT_COLORS, DEFAULT_FOREGROUND, DEFAULT_BACKGROUND, DEFAULT_CURSOR, "Default")
    }
}