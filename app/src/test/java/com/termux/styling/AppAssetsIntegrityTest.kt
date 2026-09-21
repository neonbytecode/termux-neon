package com.termux.styling

import dev.neonbytecode.neon.termux.Selectable
import dev.neonbytecode.neon.themeengine.TermuxColorsParser
import java.io.File
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

        val displayNames = ttf.map { Selectable(it.nameWithoutExtension).displayName }
        assertEquals("font display names must be unique", ttf.size, displayNames.distinct().size)
    }
}