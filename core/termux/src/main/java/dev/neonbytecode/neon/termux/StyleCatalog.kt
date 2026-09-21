package dev.neonbytecode.neon.termux

import android.content.res.AssetManager
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

/**
 * Read access to the color schemes and fonts bundled in the add-on APK.
 * Color scheme assets live under assets/colors as properties files; fonts
 * live under assets/fonts as ttf files.
 */
interface StyleCatalog {
    fun schemeNames(): List<String>
    fun schemeStream(name: String): InputStream
    fun schemeBytes(name: String): ByteArray

    /** Uncompressed size in bytes of the scheme asset, cheap to query for matching. */
    fun schemeLength(name: String): Int = schemeBytes(name).size

    fun fontNames(): List<String>
    fun fontStream(name: String): InputStream
    fun fontBytes(name: String): ByteArray

    /** Uncompressed size in bytes of the font asset, cheap to query for matching. */
    fun fontLength(name: String): Int = fontBytes(name).size
}

/**
 * Assets-backed catalog. Asset content is immutable, so each file is read at
 * most once and memoized; size lookups are O(1) after the first read.
 *
 * Scheme listing accepts `.properties` and legacy `.txt` files, preferring a
 * `.properties` sibling so the catalog never shows duplicates (e.g. both
 * `smyck.txt` and `smyck.properties` collapse to one entry).
 */
class BundledStyleCatalog(private val assets: AssetManager) : StyleCatalog {

    private val bytesCache = ConcurrentHashMap<String, ByteArray>()

    override fun schemeNames(): List<String> =
        listAssets("colors") { it.endsWith(".properties") || it.endsWith(".txt") }

    override fun schemeStream(name: String): InputStream =
        assets.open("colors/$name")

    override fun schemeBytes(name: String): ByteArray = cached("colors/$name") {
        schemeStream(name).use { it.readBytes() }
    }

    override fun schemeLength(name: String): Int = schemeBytes(name).size

    override fun fontNames(): List<String> =
        listAssets("fonts") { it.endsWith(".ttf") }

    override fun fontStream(name: String): InputStream =
        assets.open("fonts/$name")

    override fun fontBytes(name: String): ByteArray = cached("fonts/$name") {
        fontStream(name).use { it.readBytes() }
    }

    override fun fontLength(name: String): Int = fontBytes(name).size

    private fun cached(key: String, load: () -> ByteArray): ByteArray {
        bytesCache[key]?.let { return it }
        val value = load()
        bytesCache[key] = value
        return value
    }

    /**
     * Deduplicates asset file names by base name: `.properties` always wins
     * over a `.txt` with the same stem, `.txt`-only files are kept so legacy
     * schemes (e.g. `catppuccin.txt`) stay selectable.
     */
    private fun listAssets(dir: String, predicate: (String) -> Boolean): List<String> =
        dedupeByBaseName(assets.list(dir)?.filter(predicate).orEmpty())
}

/**
 * Collapses a list of asset file names so each base name appears once,
 * preferring `.properties` over `.txt` variants. Pure so it can be unit-tested
 * without an AssetManager.
 */
fun dedupeByBaseName(files: List<String>): List<String> {
    val chosen = LinkedHashMap<String, String>()
    for (file in files) {
        val base = file.substringBeforeLast('.')
        val isProperties = file.endsWith(".properties")
        val existing = chosen[base]
        if (existing == null || (isProperties && !existing.endsWith(".properties"))) {
            chosen[base] = file
        }
    }
    return chosen.values.toList()
}