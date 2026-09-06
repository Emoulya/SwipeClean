package com.example.cleanswipe.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AppThemeMode(val label: String, val description: String) {
    SYSTEM("Ikuti Sistem", "Menyesuaikan otomatis dengan tema perangkat"),
    LIGHT("Mode Terang", "Tampilan cerah dengan kontras bersih"),
    DARK("Mode Gelap", "Tampilan gelap elegan dan nyaman di mata")
}

data class UserSettings(
    val isAutoPlayEnabled: Boolean = true,
    val isAutoMuteEnabled: Boolean = true,
    val retentionDays: Int = 30,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM
)

class SettingsManager(context: Context) {

    private val appContext: Context = context.applicationContext
    private val prefs: SharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        val savedThemeName = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name)
        val themeMode = try {
            AppThemeMode.valueOf(savedThemeName ?: AppThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
        com.example.cleanswipe.util.ThemeHelper.applySystemNightMode(appContext, themeMode)
        return UserSettings(
            isAutoPlayEnabled = prefs.getBoolean(KEY_AUTO_PLAY, true),
            isAutoMuteEnabled = prefs.getBoolean(KEY_AUTO_MUTE, true),
            retentionDays = prefs.getInt(KEY_RETENTION_DAYS, 30),
            themeMode = themeMode
        )
    }

    fun setAutoPlay(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_PLAY, enabled).apply()
        _settings.update { it.copy(isAutoPlayEnabled = enabled) }
    }

    fun setAutoMute(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_MUTE, enabled).apply()
        _settings.update { it.copy(isAutoMuteEnabled = enabled) }
    }

    fun setRetentionDays(days: Int) {
        prefs.edit().putInt(KEY_RETENTION_DAYS, days).apply()
        _settings.update { it.copy(retentionDays = days) }
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _settings.update { it.copy(themeMode = mode) }
        com.example.cleanswipe.util.ThemeHelper.applySystemNightMode(appContext, mode)
    }

    companion object {
        private const val PREFS_NAME = "cleanswipe_settings"
        private const val KEY_AUTO_PLAY = "key_auto_play"
        private const val KEY_AUTO_MUTE = "key_auto_mute"
        private const val KEY_RETENTION_DAYS = "key_retention_days"
        private const val KEY_THEME_MODE = "key_theme_mode"

        @Volatile
        private var instance: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return instance ?: synchronized(this) {
                instance ?: SettingsManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
