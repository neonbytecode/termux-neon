package com.termux.styling

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.neonbytecode.neon.designsystem.NeonTheme
import dev.neonbytecode.neon.termux.TermuxEnvironment

/**
 * Termux launches this exact component by class name from its context menu;
 * it is also the launcher activity. The whole UI is Compose.
 */
class TermuxStyleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )

        setContent {
            NeonTheme {
                val viewModel: MainViewModel = viewModel()
                NeonScreen(viewModel, onBack = ::returnToTermux)
            }
        }
    }

    /**
     * Explicitly brings Termux's own task to front instead of relying on the
     * default back-stack pop: depending on how this activity was launched
     * (Termux's menu, the launcher, adb) it isn't always sitting on top of
     * Termux's task, so a plain finish() can land the user somewhere other
     * than Termux (e.g. the home screen) instead of back where they came from.
     */
    private fun returnToTermux() {
        val launchTermux = packageManager.getLaunchIntentForPackage(TermuxEnvironment.TERMUX_PACKAGE)
        if (launchTermux != null) {
            launchTermux.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(launchTermux)
            // Termux only registers its style-reload receiver in onStart, so
            // the moment it's actually resumed is the one guaranteed window
            // where a nudge lands — re-send it here so an already-open
            // terminal session picks up the newly applied colors/font live,
            // rather than only on Termux's next cold start.
            val appContext = applicationContext
            Handler(Looper.getMainLooper()).postDelayed(
                { TermuxEnvironment.requestStyleReload(appContext) },
                350L,
            )
        }
        finish()
    }
}