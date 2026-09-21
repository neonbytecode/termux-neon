package com.termux.styling

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.neonbytecode.neon.designsystem.NeonTheme

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
                NeonScreen(viewModel)
            }
        }
    }
}