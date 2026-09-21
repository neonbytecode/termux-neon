package dev.neonbytecode.neon.termux

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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

    /**
     * Boolean extra TermuxActivity actually reads off [RELOAD_STYLE_ACTION]
     * (`intent.getBooleanExtra(EXTRA_RECREATE_ACTIVITY, true)`); its receiver
     * always reloads colors/font live before optionally recreating the
     * activity, so we pass `false` to avoid a jarring full restart.
     */
    const val RELOAD_STYLE_EXTRA_RECREATE_ACTIVITY = "com.termux.app.TermuxActivity.EXTRA_RECREATE_ACTIVITY"

    const val COLORS_PROPERTIES = "colors.properties"
    const val FONT_FILE = "font.ttf"
    const val TERMUX_DIR_NAME = ".termux"
    const val DEFAULT_COLORS_MARKER = "# Using default color theme."

    enum class AccessProblem {
        NONE,
        NOT_INSTALLED,
        INCOMPATIBLE_SIGNATURE,
        ACCESS_ERROR,
    }

    data class State(
        val accessible: Boolean,
        val termuxContext: Context?,
        val termuxDir: File?,
        val problem: AccessProblem = AccessProblem.NONE,
    ) {
        companion object {
            val NONE = State(
                accessible = false,
                termuxContext = null,
                termuxDir = null,
                problem = AccessProblem.ACCESS_ERROR,
            )
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
        } catch (_: PackageManager.NameNotFoundException) {
            State(false, null, null, AccessProblem.NOT_INSTALLED)
        } catch (_: SecurityException) {
            State(false, null, null, AccessProblem.INCOMPATIBLE_SIGNATURE)
        } catch (_: Exception) {
            State.NONE
        }
    }

    fun ensureTermuxDir(termuxDir: File): Boolean =
        termuxDir.isDirectory || termuxDir.mkdirs()

    /**
     * Asks a running Termux to reload colors/font from disk. Termux only
     * registers this receiver while its activity is started (`onStart` /
     * `onStop`), so this is a no-op if Termux is currently stopped (e.g.
     * fully covered by this add-on's own full-screen UI) — callers that
     * need a guaranteed live refresh should call this again once Termux is
     * actually back in front.
     */
    fun requestStyleReload(context: Context) {
        val intent = Intent(RELOAD_STYLE_ACTION)
            .setPackage(TERMUX_PACKAGE)
            .putExtra(RELOAD_STYLE_EXTRA_RECREATE_ACTIVITY, false)
        context.sendBroadcast(intent)
    }
}