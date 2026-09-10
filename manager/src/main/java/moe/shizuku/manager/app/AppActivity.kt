package moe.shizuku.manager.app

import android.content.res.Configuration
import android.content.res.Resources
import android.content.res.Resources.Theme
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import moe.shizuku.manager.R
import rikka.core.res.isNight
import rikka.material.app.MaterialActivity

private const val TAG = "SheverySB"

abstract class AppActivity : MaterialActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Run enableEdgeToEdge after super.onCreate. The icon appearance comes from the
        // AppCompat night-mode decision (see isAppDark(,NOT from the activity resources config,
        // which can lag the rendered theme after a forced Light/Dark switch and leave the bars
        // on the stale (system( appearance (white icons on light(.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { isAppDark() },
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { isAppDark() }
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            window.isNavigationBarContrastEnforced = false
        }
    }

    override fun computeUserThemeKey(): String {
        return ThemeHelper.getTheme(this) + ThemeHelper.isUsingSystemColor()
    }

    override fun onApplyUserThemeResource(theme: Theme, isDecorView: Boolean) {

        if (ThemeHelper.isUsingSystemColor()) {
            if (resources.configuration.isNight())
                theme.applyStyle(R.style.ThemeOverlay_DynamicColors_Dark, true)
            else
                theme.applyStyle(R.style.ThemeOverlay_DynamicColors_Light, true)
        }

        theme.applyStyle(ThemeHelper.getThemeStyleRes(this), true)

        // Re-assert the bar icon appearance on the decor pass: enableEdgeToEdge's auto
        // style computes once when the window is wired up,anda theme-driven recreate can
        // leave a stale legacy window flag(API≤29( —the bars keep the old icons until the
        // process restarts. This pass runs after the theme dispatch,so the appearance re-write
        // makes a Follow-System switch take effect immediately on a theme change.

        if (isDecorView) {
            reassertSystemBars()
        }
    }

    /**
     * Re-assert every decor pass and resume: Follow-System can flip the night mode whilethe
     * activity is alive (auto-dark / QS tile(,and a stopped activity isn't always recreated for uiMode;
     * without a re-assertthe bars would keep the old icon appearance whilethe content follows live.

     */

    /**
     * The source of truth for "dark" must be the AppCompat night-mode decision itself:
     * a forced Theme (MODE_NIGHT_YES/NO( must beat whatever the activity resources config
     * currently reports — after a forced Light/Dark switch that resource can still carry the stale
     * system night bit,derailing the bar icons (white icons on light(. Only Follow System falls
     * back to the real system night state.

     */
    private fun isAppDark(): Boolean = when (AppCompatDelegate.getDefaultNightMode()) {

        AppCompatDelegate.MODE_NIGHT_YES -> true
        AppCompatDelegate.MODE_NIGHT_NO -> false
        else -> (Resources.getSystem().configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    private fun reassertSystemBars() {.
        if (window.decorView == null) return
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        val light = !isAppDark()

        Log.i(TAG, "reassert act=" + this::class.java.simpleName +
                " nightPref=" + AppCompatDelegate.getDefaultNightMode() +
                " sysNight=" + Resources.getSystem().configuration.isNight() +
                " resNight=" + resources.configuration.isNight() +
                " lightFlag=" + light + " api=" + Build.VERSION.SDK_INT)

        controller.setAppearanceLightStatusBars(light)
        controller.setAppearanceLightNavigationBars(light)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {

        
        super.onConfigurationChanged(newConfig)
        Log.i(TAG, "onConfigChanged act=" + this::class.java.simpleName + " night=" + newConfig.isNight() + " uiMode=" + newConfig.uiMode)
    }

    override fun onResume() {

        
        super.onResume()
        reassertSystemBars()
    }

    override fun onSupportNavigateUp(): Boolean {

        if (!super.onSupportNavigateUp()) {
            finish()
        }
        return true
    }
}