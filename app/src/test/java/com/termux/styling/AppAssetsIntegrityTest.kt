package com.termux.styling

import dev.neonbytecode.neon.termux.Selectable
import dev.neonbytecode.neon.themeengine.TermuxColorsParser
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Integrity checks against the real bundled assets (run with the :app module
 * as working directory, as Gradle unit tests do). Guard-rails the shipped
 * color schemes and font files so a bad asset can never ship silently.
 */
class AppAssetsIntegrityTest {

    private val assetsDir = File("src/main/assets")

    private fun schemeFiles(): List<File> {
        val dir = File(assetsDir, "colors")
        assertTrue("colors dir missing: ${dir.absolutePath}", dir.isDirectory)
        return dir.listFiles { f -> f.isFile && (f.extension == "properties" || f.extension == "txt") }
            ?.sortedBy { it.name }
            ?: emptyList()
    }

    @Test
    fun everyBundledSchemeSurvivesDedupeAndParses() {
        val files = schemeFiles()
        assertTrue("no scheme assets found", files.isNotEmpty())
        assertEquals("curated scheme set must stay at exactly 15", 15, files.size)

        val selectables = files.map { Selectable(it.nameWithoutExtension) }
        assertEquals("duplicate selectable flavors after dedupe", files.size, selectables.distinct().size)

        for (file in files) {
            val selectable = Selectable(file.nameWithoutExtension)
            val palette = runCatching {
                TermuxColorsParser.parseProperties(file.inputStream(), selectable.displayName)
            }
            assertTrue("scheme '${file.name}' must parse cleanly, got: ${palette.exceptionOrNull()?.message}", palette.isSuccess)
            assertEquals("scheme '${file.name}' must expose the full 16 ANSI colors", 16, palette.getOrNull()?.colors?.size)
        }
    }

    @Test
    fun everyBundledFontHasANonEmptyFontFile() {
        val dir = File(assetsDir, "fonts")
        assertTrue("fonts dir missing: ${dir.absolutePath}", dir.isDirectory)
        val ttf = dir.listFiles { f -> f.isFile && f.extension == "ttf" }?.sortedBy { it.name } ?: emptyList()
        assertTrue("no font assets found", ttf.isNotEmpty())
        assertTrue("font files must not be empty", ttf.all { it.length() > 0L })
        assertTrue("font files must stay below 12 MiB", ttf.all { it.length() <= MAX_FONT_BYTES })

        val displayNames = ttf.map { Selectable(it.nameWithoutExtension).displayName }
        assertEquals("font display names must be unique", ttf.size, displayNames.distinct().size)

        val hashes = ttf.map { file ->
            assertTrue(
                "font '${file.name}' must have a valid sfnt structure",
                hasValidSfntStructure(file),
            )
            sha256(file)
        }
        assertEquals("font files must not be duplicate byte-for-byte copies", hashes.size, hashes.toSet().size)
    }

    private fun hasValidSfntStructure(file: File): Boolean {
        val bytes = file.readBytes()
        if (bytes.size < SFNT_HEADER_BYTES) return false
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
        val signature = buffer.int
        if (signature != 0x00010000 && signature != 0x4F54544F && signature != 0x74727565) return false
        val tableCount = buffer.short.toInt() and 0xFFFF
        val directoryBytes = tableCount * SFNT_RECORD_BYTES
        if (SFNT_HEADER_BYTES + directoryBytes > bytes.size) return false
        buffer.position(SFNT_HEADER_BYTES)
        repeat(tableCount) {
            buffer.int // tag
            buffer.int // checksum
            val offset = buffer.int.toLong() and 0xFFFFFFFFL
            val length = buffer.int.toLong() and 0xFFFFFFFFL
            if (offset > bytes.size || length > bytes.size - offset) return false
        }
        return true
    }

    private fun sha256(file: File): String =
        MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }

    companion object {
        private const val MAX_FONT_BYTES = 12L * 1024L * 1024L
        private const val SFNT_HEADER_BYTES = 12
        private const val SFNT_RECORD_BYTES = 16
    }
}