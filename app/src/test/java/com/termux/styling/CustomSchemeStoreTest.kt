package com.termux.styling

import dev.neonbytecode.neon.themeengine.AnsiPalette
import dev.neonbytecode.neon.themeengine.TermuxColorsParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File

class CustomSchemeStoreTest {

    @Test
    fun `CustomScheme model properties`() {
        val file = File("custom_schemes/neon_dark.properties")
        val customScheme = CustomScheme(name = "neon_dark.properties", palette = AnsiPalette.DEFAULT, file = file)

        assertEquals("neon_dark.properties", customScheme.name)
        assertEquals(file, customScheme.file)
    }

    @Test
    fun `sanitizes scheme file names`() {
        val original = "My Neon Scheme (Dark)"
        val clean = original.replace(Regex("[^a-zA-Z0-9._-]"), "_") + ".properties"

        assertTrue(clean.endsWith(".properties"))
        assertEquals("My_Neon_Scheme__Dark_.properties", clean)
    }

    @Test
    fun `exports and parses custom palette string`() {
        val palette = AnsiPalette.DEFAULT.copy(name = "custom_neon")
        val propertiesStr = TermuxColorsParser.toPropertiesString(palette)
        val reParsed = TermuxColorsParser.parseProperties(ByteArrayInputStream(propertiesStr.toByteArray()), "custom_neon")

        assertEquals(palette.background, reParsed.background)
        assertEquals(palette.foreground, reParsed.foreground)
        assertEquals(palette.cursor, reParsed.cursor)
    }
}
