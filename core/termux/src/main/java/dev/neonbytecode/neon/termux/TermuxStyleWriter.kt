package dev.neonbytecode.neon.termux

import android.content.Context
import android.content.Intent
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
            // Use canonicalFile to follow a possible symlink.
            val destinationFile = File(termuxDir, outputFile).canonicalFile

            // Idempotency fast-path: identical content already installed —
            // skip the rewrite and don't bump a running Termux.
            if (destinationFile.isFile &&
                destinationFile.length() == content.size.toLong() &&
                destinationFile.readBytes().contentEquals(content)
            ) {
                return@runCatching
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

            sendReloadStyleBroadcast(isColors)
        }
    }

    private fun targetContent(isColors: Boolean, defaultChoice: Boolean, assetName: String): ByteArray =
        when {
            !defaultChoice -> if (isColors) catalog.schemeBytes(assetName) else catalog.fontBytes(assetName)
            isColors -> TermuxEnvironment.DEFAULT_COLORS_MARKER.toByteArray(StandardCharsets.UTF_8)
            // Leaving an empty font file is the marker for Termux's default typeface.
            else -> ByteArray(0)
        }

    private fun sendReloadStyleBroadcast(isColors: Boolean) {
        // Must match Termux's TermuxActivity broadcast receiver (extra value is
        // "colors" or "font"). Scoping the implicit broadcast to the Termux
        // package keeps the style intent from leaking to other apps and is
        // immune to the Android 8+ manifest implicit-broadcast restrictions
        // (Termux registers this receiver dynamically).
        val executeIntent = Intent(TermuxEnvironment.RELOAD_STYLE_ACTION)
            .setPackage(TermuxEnvironment.TERMUX_PACKAGE)
            .putExtra(
                TermuxEnvironment.RELOAD_STYLE_ACTION,
                if (isColors) "colors" else "font",
            )
        context.sendBroadcast(executeIntent)
    }
}