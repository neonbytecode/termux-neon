package dev.neonbytecode.neon.termux

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TermuxStyleWriterTest {

    @Test
    fun `replaces an existing foreground line in place`() {
        val content = "# Dracula\nbackground=#282a36\nforeground=#f8f8f2\ncursor=#f8f8f2\n"
        val patched = patchForegroundColor(content, "#ff00ff")

        assertTrue(patched.contains("foreground=#ff00ff"))
        assertTrue(!patched.contains("foreground=#f8f8f2"))
        // background/cursor and the comment survive untouched.
        assertTrue(patched.contains("# Dracula"))
        assertTrue(patched.contains("background=#282a36"))
        assertTrue(patched.contains("cursor=#f8f8f2"))
    }

    @Test
    fun `prepends foreground when absent`() {
        val patched = patchForegroundColor("# Using default color theme.", "#ffffff")

        assertEquals("foreground=#ffffff\n# Using default color theme.", patched)
    }

    @Test
    fun `handles empty existing content`() {
        val patched = patchForegroundColor("", "#123456")

        assertEquals("foreground=#123456\n", patched)
    }

    @Test
    fun `is case insensitive and matches colon separator`() {
        val patched = patchForegroundColor("Foreground: #000000\nbackground: #ffffff", "#abcdef")

        assertTrue(patched.contains("foreground=#abcdef"))
        assertTrue(!patched.contains("#000000"))
        assertTrue(patched.contains("background: #ffffff"))
    }
}
