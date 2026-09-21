package com.termux.styling

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CustomFontStoreTest {

    @Test
    fun `CustomFont model properties`() {
        val fontFile = File("custom_fonts/MyFont.ttf")
        val customFont = CustomFont(name = "MyFont.ttf", file = fontFile)

        assertEquals("MyFont.ttf", customFont.name)
        assertEquals(fontFile, customFont.file)
    }

    @Test
    fun `sanitizes file names cleanly`() {
        val original = "My Custom Font (v1).otf"
        val clean = original.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        assertTrue(clean.endsWith(".otf"))
        assertEquals("My_Custom_Font__v1_.otf", clean)
    }
}
