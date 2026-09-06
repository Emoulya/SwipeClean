package com.example.cleanswipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.cleanswipe.data.preferences.SettingsManager
import com.example.cleanswipe.ui.CleanSwipeApp
import com.example.cleanswipe.ui.theme.CleanSwipeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val settingsManager = SettingsManager.getInstance(applicationContext)
        com.example.cleanswipe.util.ThemeHelper.applySystemNightMode(this, settingsManager.settings.value.themeMode)
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by settingsManager.settings.collectAsState()
            CleanSwipeTheme(themeMode = settings.themeMode) {
                CleanSwipeApp()
            }
        }
    }
}