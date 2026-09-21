package dev.neonbytecode.neon.termux

import android.content.Context
import android.util.AtomicFile
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * Writes the selected color scheme / font into Termux's `~/.termux`
 * directory using atomic file writes, then broadcasts the style-reload action
 * so a running Termux picks the change up immediately.
 *
 * This is the direct port of the legacy Termux:Styling copy mechanism
 * (kept behavior-identical), hardened with a proper fail-write path, an
 * idempotency fast-path and a package-scoped reload broadcast.
 */
class TermuxStyleWriter(
    private val context: Context,
    private val catalog: StyleCatalog,
) {

    /**
     * Applies a bundled scheme or font.
     *
     * @param isColors true for a color scheme, false for a font.
     * @param bundleName the asset file name, or null/`Default` to restore Termux defaults.
     */
    fun apply(state: TermuxEnvironment.State, isColors: Boolean, bundleName: String?): Result<Unit> {
        val termuxDir = state.termuxDir
        if (state.termuxContext == null || termuxDir == null) {
            return Result.failure(IllegalStateException("Termux is not accessible on this device."))
        }
        val outputFile = if (isColors) TermuxEnvironment.COLORS_PROPERTIES else TermuxEnvironment.FONT_FILE
        val defaultChoice = bundleName.isNullOrEmpty() || bundleName == Selectable.DEFAULT_FILENAME
        // Only consulted when !defaultChoice; placeholder keeps the type non-null.
        val assetName: String = bundleName ?: Selectable.DEFAULT_FILENAME

        return runCatching {
            if (!TermuxEnvironment.ensureTermuxDir(termuxDir)) {
                throw RuntimeException("Cannot create termux dir=${termuxDir.absolutePath}")
            }
            val content = targetContent(isColors, defaultChoice, assetName)
            val destinationFile = File(termuxDir, outputFile).canonicalFile
            if (writeIfChanged(destinationFile, content)) {
                TermuxEnvironment.requestStyleReload(context)
            }
        }
    }

    /**
     * Overlays just the `foreground=` line of the currently installed
     * `colors.properties` with [hexColor], leaving every other line (whatever
     * scheme is applied, any hand-edited colors) untouched. Termux fills in
     * sensible defaults for any key that isn't present, so this works even
     * when nothing has been applied yet.
     */
    fun applyForegroundColor(state: TermuxEnvironment.State, hexColor: String): Result<Unit> {
        val termuxDir = state.termuxDir
        if (state.termuxContext == null || termuxDir == null) {
            return Result.failure(IllegalStateException("Termux is not accessible on this device."))
        }
        return runCatching {
            if (!TermuxEnvironment.ensureTermuxDir(termuxDir)) {
                throw RuntimeException("Cannot create termux dir=${termuxDir.absolutePath}")
            }
            val destinationFile = File(termuxDir, TermuxEnvironment.COLORS_PROPERTIES).canonicalFile
            val existing = if (destinationFile.isFile) destinationFile.readText(StandardCharsets.UTF_8) else ""
            val content = patchForegroundColor(existing, hexColor).toByteArray(StandardCharsets.UTF_8)
            if (writeIfChanged(destinationFile, content)) {
                TermuxEnvironment.requestStyleReload(context)
            }
        }
    }

    /** Writes atomically unless [content] already matches what's on disk. Returns whether it wrote. */
    private fun writeIfChanged(destinationFile: File, content: ByteArray): Boolean {
        // Idempotency fast-path: identical content already installed — skip
        // the rewrite and don't bump a running Termux.
        if (destinationFile.isFile &&
            destinationFile.length() == content.size.toLong() &&
            destinationFile.readBytes().contentEquals(content)
        ) {
            return false
        }

        // Fix for users who have messed up with chmod.
        destinationFile.setWritable(true)
        destinationFile.parentFile?.setWritable(true)
        destinationFile.parentFile?.setExecutable(true)

        val atomicFile = AtomicFile(destinationFile)
        val out = atomicFile.startWrite()
        try {
            out.write(content)
        } catch (e: Exception) {
            atomicFile.failWrite(out)
            throw e
        }
        atomicFile.finishWrite(out)
        return true
    }

    private fun targetContent(isColors: Boolean, defaultChoice: Boolean, assetName: String): ByteArray =
        when {
            !defaultChoice -> if (isColors) catalog.schemeBytes(assetName) else catalog.fontBytes(assetName)
            isColors -> TermuxEnvironment.DEFAULT_COLORS_MARKER.toByteArray(StandardCharsets.UTF_8)
            // Leaving an empty font file is the marker for Termux's default typeface.
            else -> ByteArray(0)
        }
}

private val FOREGROUND_LINE = Regex("""(?i)^\s*foreground\s*[:=].*$""")

/**
 * Replaces the first `foreground` line in [content] with `foreground=<hexColor>`,
 * or prepends one if none exists. Pure so it's unit-testable without touching
 * the filesystem.
 */
fun patchForegroundColor(content: String, hexColor: String): String {
    val lines = content.lines().toMutableList()
    val newLine = "foreground=$hexColor"
    val index = lines.indexOfFirst { FOREGROUND_LINE.matches(it) }
    if (index >= 0) {
        lines[index] = newLine
    } else {
        lines.add(0, newLine)
    }
    return lines.joinToString("\n")
}
