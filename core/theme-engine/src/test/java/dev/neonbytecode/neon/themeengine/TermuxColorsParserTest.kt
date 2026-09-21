package dev.neonbytecode.neon.themeengine

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.ByteArrayInputStream
import java.util.Properties

class TermuxColorsParserTest {

    @Test
    fun `parses standard termux properties file`() {
        val source = """
            # Comment line
            background=#101014
            foreground=#cccccc
            cursor=#00e5ff
            color0=#000000
            color1=#b03060
            color2=#70b030
            color3=#c0a000
            color4=#4060a0
            color5=#c030b0
            color6=#00b0b0
            color7=#a0a0a0
            color8=#404040
            color9=#d05070
            color10=#80d040
            color11=#d0b040
            color12=#5080c0
            color13=#d050c0
            color14=#40c0c0
            color15=#d0d0d0
        """.trimIndent()
        val palette = TermuxColorsParser.parseProperties(ByteArrayInputStream(source.toByteArray()), "ruby")

        assertEquals(0xFF101014.toInt(), palette.background)
        assertEquals(0xFFCCCCCC.toInt(), palette.foreground)
        assertEquals(0xFF00E5FF.toInt(), palette.cursor)
        assertEquals(0xFFB03060.toInt(), palette.colors[1])
        assertEquals(0xFF40C0C0.toInt(), palette.colors[14])
        assertEquals("ruby", palette.name)
    }

    @Test
    fun `falls back to defaults for missing keys`() {
        val palette = TermuxColorsParser.parseProperties(ByteArrayInputStream("# empty\n".toByteArray()))
        assertEquals(AnsiPalette.DEFAULT, palette)
    }

    @Test
    fun `accepts hex formats with and without prefix`() {
        assertEquals(0xFF00E5FF.toInt(), TermuxColorsParser.parseHexColor("#00e5ff"))
        assertEquals(0xFF00E5FF.toInt(), TermuxColorsParser.parseHexColor("00E5FF"))
        assertEquals(0xFF00E5FF.toInt(), TermuxColorsParser.parseHexColor("0x00e5ff"))
        assertEquals(0x1200E5FF.toInt(), TermuxColorsParser.parseHexColor("1200E5FF"))
    }

    @Test
    fun `rejects malformed values`() {
        assertNull(TermuxColorsParser.parseHexColor(null))
        assertNull(TermuxColorsParser.parseHexColor(""))
        assertNull(TermuxColorsParser.parseHexColor("#12345"))
        assertNull(TermuxColorsParser.parseHexColor("#gggggg"))
        assertNull(TermuxColorsParser.parseHexColor("not a color"))
    }

    @Test
    fun `fromProperties handles uppercase keys`() {
        val props = Properties().apply {
            setProperty("BACKGROUND", "#ffffff")
            setProperty("FOREground", "#000000")
            setProperty("CURSOR", "#00e5ff")
            setProperty("COLOR0", "#111111")
            setProperty("color8", "#222222")
        }
        val palette = TermuxColorsParser.fromProperties(props)
        assertEquals(0xFFFFFFFF.toInt(), palette.background)
        assertEquals(0xFF000000.toInt(), palette.foreground)
        assertEquals(0xFF111111.toInt(), palette.colors[0])
        assertEquals(0xFF222222.toInt(), palette.colors[8])
    }

    @Test
    fun `DemoScreen builds lines for all preview modes`() {
        val palette = AnsiPalette.DEFAULT
        PreviewMode.entries.forEach { mode ->
            val lines = DemoScreen.build(palette, "cyberpunk", "Fira Code", mode, customText = "Hello Neon")
            Assert.assertTrue("Lines should not be empty for $mode", lines.isNotEmpty())
        }
    }

    @Test
    fun `toPropertiesString exports valid termux properties format`() {
        val original = AnsiPalette.DEFAULT.copy(name = "test_theme")
        val propertiesStr = TermuxColorsParser.toPropertiesString(original)

        val reParsed = TermuxColorsParser.parseProperties(
            ByteArrayInputStream(propertiesStr.toByteArray(Charsets.UTF_8)),
            "test_theme",
        )

        assertEquals(original.background, reParsed.background)
        assertEquals(original.foreground, reParsed.foreground)
        assertEquals(original.cursor, reParsed.cursor)
        assertEquals(original.colors, reParsed.colors)
    }
}