package com.example.cleanswipe.util

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import com.example.cleanswipe.data.preferences.AppThemeMode

object ThemeHelper {
    fun applySystemNightMode(context: Context, themeMode: AppThemeMode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
            val targetMode = when (themeMode) {
                AppThemeMode.LIGHT -> UiModeManager.MODE_NIGHT_NO
                AppThemeMode.DARK -> UiModeManager.MODE_NIGHT_YES
                AppThemeMode.SYSTEM -> UiModeManager.MODE_NIGHT_AUTO
            }
            uiModeManager?.setApplicationNightMode(targetMode)
        }
    }
}
