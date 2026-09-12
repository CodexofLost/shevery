package moe.shizuku.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import moe.shizuku.manager.settings.AiManagerScreen
import moe.shizuku.manager.ui.compose.ShizukuExpressiveTheme

/**
 * Debug-only host activity. Opens the real AiManagerScreen so emulator test flows
 * can reach the AI provider/model management UI without scrolling Settings.
 * Registered only in src/debug AndroidManifest; absent from release builds.
 */
class TestHostActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShizukuExpressiveTheme {
                BackHandler { finish() }
                AiManagerScreen(
                    onNavigateUp = { finish() },
                    onChanged = { },
                )
            }
        }
    }
}