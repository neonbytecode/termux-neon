package dev.neonbytecode.neon.termux

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StyleCatalogTest {

    @Test
    fun `properties wins over txt sibling`() {
        assertEquals(
            listOf("smyck.properties", "other.txt"),
            dedupeByBaseName(listOf("smyck.txt", "smyck.properties", "other.txt")),
        )
    }

    @Test
    fun `txt-only schemes are kept`() {
        assertEquals(listOf("catppuccin.txt"), dedupeByBaseName(listOf("catppuccin.txt")))
    }

    @Test
    fun `shared family prefixes are not collapsed`() {
        assertEquals(
            listOf("catppuccin-frappe.properties", "catppuccin.txt"),
            dedupeByBaseName(listOf("catppuccin-frappe.properties", "catppuccin.txt")),
        )
    }

    @Test
    fun `no duplicates when both variants present`() {
        val names = dedupeByBaseName(
            listOf("nord.txt", "nord.properties", "smyck.properties", "smyck.txt"),
        )
        assertEquals(setOf("nord", "smyck").size, names.size)
        assertTrue(names.containsAll(listOf("nord.properties", "smyck.properties")))
    }
}