package dev.neonbytecode.neon.termux

import dev.neonbytecode.neon.themeengine.AnsiPalette
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Files

/** In-memory catalog for hermetic reader tests. */
private open class FakeCatalog(
    private val schemes: Map<String, ByteArray>,
    private val fonts: Map<String, ByteArray>,
) : StyleCatalog {
    override fun schemeNames(): List<String> = schemes.keys.sorted()
    override fun schemeStream(name: String): InputStream = ByteArrayInputStream(schemeBytes(name))
    override fun schemeBytes(name: String): ByteArray = schemes[name] ?: error("no scheme $name")
    override fun schemeLength(name: String): Int = schemeBytes(name).size
    override fun fontNames(): List<String> = fonts.keys.sorted()
    override fun fontStream(name: String): InputStream = ByteArrayInputStream(fontBytes(name))
    override fun fontBytes(name: String): ByteArray = fonts[name] ?: error("no font $name")
    override fun fontLength(name: String): Int = fontBytes(name).size
}

/** Catalog whose reads explode — proves the cheap palette path never scans assets. */
private class ThrowingCatalog : StyleCatalog {
    override fun schemeNames(): List<String> = listOf("boom.properties")
    override fun schemeStream(name: String): InputStream = error("scan")
    override fun schemeBytes(name: String): ByteArray = error("scan")
    override fun fontNames(): List<String> = listOf("boom.ttf")
    override fun fontStream(name: String): InputStream = error("scan")
    override fun fontBytes(name: String): ByteArray = error("scan")
}

/**
 * Catalog whose [schemeLength] reports the real size only for the exact
 * install, lying for every other candidate; reading any wrong-size candidate
 * explodes. If matching consulted byte content instead of the size gate first,
 * a wrong-size candidate would be read and the test would crash.
 */
private class LyingLengthCatalog : FakeCatalog(
    schemes = mapOf(
        "neon.properties" to "background=#0a0a12\n".toByteArray(),
        "other.properties" to "background=#ff0000\nforeground=#0000ff\n".toByteArray(),
    ),
    fonts = emptyMap(),
) {
    private val neonSize = 19
    override fun schemeLength(name: String): Int =
        if (name == "neon.properties") neonSize else 1_000_000
    override fun schemeBytes(name: String): ByteArray =
        if (name == "neon.properties") super.schemeBytes(name) else error("byte-compare consulted wrong-size candidate")
}

class AppliedStyleReaderTest {

    private lateinit var catalog: FakeCatalog
    private lateinit var termuxDir: File
    private lateinit var reader: AppliedStyleReader

    @Before
    fun setup() {
        catalog = FakeCatalog(
            schemes = mapOf(
                "dracula.properties" to "background=#282a36\nforeground=#f8f8f2\n".toByteArray(),
                "neon.properties" to "background=#0a0a12\nforeground=#00e5ff\n".toByteArray(),
            ),
            fonts = mapOf("Hack.ttf" to ByteArray(64) { it.toByte() }),
        )
        termuxDir = Files.createTempDirectory("termux-reader").toFile()
        reader = AppliedStyleReader(catalog)
    }

    private fun termuxState(): TermuxEnvironment.State =
        TermuxEnvironment.State(accessible = true, termuxContext = null, termuxDir = termuxDir)

    private fun install(relativeName: String, bytes: ByteArray) {
        File(termuxDir, relativeName).writeBytes(bytes)
    }

    @Test
    fun `read identifies installed bundled scheme and returns its palette`() {
        install(TermuxEnvironment.COLORS_PROPERTIES, catalog.schemeBytes("dracula.properties"))

        val applied = reader.read(termuxState())

        assertEquals("dracula.properties", applied.schemeName)
        assertEquals(0xFF282A36.toInt(), applied.installedPalette?.background)
        assertEquals(0xFFF8F8F2.toInt(), applied.installedPalette?.foreground)
    }

    @Test
    fun `read matches installed font`() {
        install(TermuxEnvironment.FONT_FILE, catalog.fontBytes("Hack.ttf"))

        val applied = reader.read(termuxState())

        assertEquals("Hack.ttf", applied.fontName)
        assertNull(applied.schemeName)
    }

    @Test
    fun `default marker file reads as default`() {
        install(
            TermuxEnvironment.COLORS_PROPERTIES,
            TermuxEnvironment.DEFAULT_COLORS_MARKER.toByteArray(),
        )
        install(TermuxEnvironment.FONT_FILE, ByteArray(0))

        val applied = reader.read(termuxState())

        assertNull(applied.schemeName)
        assertNull(applied.fontName)
        assertNull(applied.installedPalette)
        assertTrue(applied.isDefaultColor)
        assertTrue(applied.isDefaultFont)
    }

    @Test
    fun `read is clean when nothing is installed`() {
        val applied = reader.read(termuxState())
        assertTrue(applied.isDefaultColor)
        assertTrue(applied.isDefaultFont)
        assertNull(applied.schemeName)
        assertNull(applied.fontName)
    }

    @Test
    fun `matching short-circuits candidate sizes before reading bytes`() {
        val lying = LyingLengthCatalog()
        install(TermuxEnvironment.COLORS_PROPERTIES, lying.schemeBytes("neon.properties"))

        val applied = AppliedStyleReader(lying).read(termuxState())

        assertEquals("neon.properties", applied.schemeName)
    }

    @Test
    fun `installedPalette parses the live file without touching the catalog`() {
        install(TermuxEnvironment.COLORS_PROPERTIES, "background=#123456\nforeground=#abcdef\n".toByteArray())

        val palette = AppliedStyleReader(ThrowingCatalog()).installedPalette(termuxState())

        assertEquals(0xFF123456.toInt(), palette?.background)
        assertEquals(0xFFABCDEF.toInt(), palette?.foreground)
    }

    @Test
    fun `installedPalette honors the default marker`() {
        install(TermuxEnvironment.COLORS_PROPERTIES, TermuxEnvironment.DEFAULT_COLORS_MARKER.toByteArray())

        assertNull(AppliedStyleReader(ThrowingCatalog()).installedPalette(termuxState()))
    }

    @Test
    fun `installedPalette returns null when colors file is missing`() {
        assertNull(reader.installedPalette(termuxState()))
    }

    @Test
    fun `applying a bundled scheme matches what the reader reports`() {
        install(TermuxEnvironment.COLORS_PROPERTIES, catalog.schemeBytes("neon.properties"))

        val applied = reader.read(termuxState())

        assertEquals("neon.properties", applied.schemeName)
        assertEquals(0xFF0A0A12.toInt(), applied.installedPalette?.background)
        assertEquals(0xFF00E5FF.toInt(), applied.installedPalette?.foreground)
    }

    @Test
    fun `unbundled custom scheme parses without a name match`() {
        install(TermuxEnvironment.COLORS_PROPERTIES, "background=#010203\nforeground=#040506\n".toByteArray())

        val applied = reader.read(termuxState())

        assertNull(applied.schemeName)
        assertEquals(0xFF010203.toInt(), applied.installedPalette?.background)
    }

    @Test
    fun `parser errors fall back to default palette not a crash`() {
        install(TermuxEnvironment.COLORS_PROPERTIES, "color0 = not-a-color\n".toByteArray())

        val applied = reader.read(termuxState())

        assertNull(applied.schemeName)
        assertEquals(AnsiPalette.DEFAULT.background, applied.installedPalette?.background)
    }
}