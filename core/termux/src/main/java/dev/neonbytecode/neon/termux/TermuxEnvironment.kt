package dev.neonbytecode.neon.termux

import android.content.Context
import java.io.File

/**
 * Represents the on-device Termux installation this add-on talks to.
 *
 * All Termux add-ons share `sharedUserId="com.termux"`, which lets us acquire
 * Termux's package context (and therefore its private `filesDir`) provided the
 * APKs are signed with the same key. This is the legacy plugin contract; it is
 * intentionally isolated in this module so a future migration to the plugin
 * API / Storage Access Framework is a swap here and nowhere else.
 */
object TermuxEnvironment {

    const val TERMUX_PACKAGE = "com.termux"
    const val RELOAD_STYLE_ACTION = "com.termux.app.reload_style"

    const val COLORS_PROPERTIES = "colors.properties"
    const val FONT_FILE = "font.ttf"
    const val TERMUX_DIR_NAME = ".termux"
    const val DEFAULT_COLORS_MARKER = "# Using default color theme."

    data class State(
        val accessible: Boolean,
        val termuxContext: Context?,
        val termuxDir: File?,
    ) {
        companion object {
            val NONE = State(accessible = false, termuxContext = null, termuxDir = null)
        }
    }

    /** Probes Termux access. Cheap enough to call on every resume. */
    fun evaluate(context: Context): State {
        return try {
            val termuxContext = context.createPackageContext(
                TERMUX_PACKAGE,
                Context.CONTEXT_IGNORE_SECURITY,
            )
            val homeDir = File(termuxContext.filesDir, "home")
            val termuxDir = File(homeDir, TERMUX_DIR_NAME)
            State(accessible = true, termuxContext = termuxContext, termuxDir = termuxDir)
        } catch (e: Exception) {
            State.NONE
        }
    }

    fun ensureTermuxDir(termuxDir: File): Boolean =
        termuxDir.isDirectory || termuxDir.mkdirs()
}