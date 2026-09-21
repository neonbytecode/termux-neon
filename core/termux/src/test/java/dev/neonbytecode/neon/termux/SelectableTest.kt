package dev.neonbytecode.neon.termux

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectableTest {

    @Test
    fun `base16 scheme names become title case`() {
        assertEquals("Base16 Cyberpunk Dark", Selectable("base16-cyberpunk-dark.properties").displayName)
    }

    @Test
    fun `rosé pine keeps its accent`() {
        assertEquals("Rosé Pine", Selectable("rosé-pine.properties").displayName)
    }

    @Test
    fun `font names drop extension and hyphenate`() {
        assertEquals("JetBrains Mono", Selectable("JetBrains-Mono.ttf").displayName)
    }

    @Test
    fun `default filename capitalized`() {
        assertEquals("Default", Selectable(Selectable.DEFAULT_FILENAME).displayName)
    }

    @Test
    fun `capitalizeName preserves existing capitals`() {
        assertEquals("JetBrains Mono NG", capitalizeName("JetBrains-Mono-NG"))
    }

    @Test
    fun `equality is by filename`() {
        assertEquals(Selectable("Foo.ttf"), Selectable("Foo.ttf"))
        assertTrue(Selectable("Foo.ttf") == Selectable("Foo.ttf"))
        assertFalse(Selectable("Foo.ttf") == Selectable("Bar.ttf"))
    }
}