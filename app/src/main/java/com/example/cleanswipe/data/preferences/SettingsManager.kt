package com.example.cleanswipe.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class UserSettings(
    val isAutoPlayEnabled: Boolean = true,
    val isAutoMuteEnabled: Boolean = true,
    val retentionDays: Int = 30
)

class SettingsManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        return UserSettings(
            isAutoPlayEnabled = prefs.getBoolean(KEY_AUTO_PLAY, true),
            isAutoMuteEnabled = prefs.getBoolean(KEY_AUTO_MUTE, true),
            retentionDays = prefs.getInt(KEY_RETENTION_DAYS, 30)
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

    companion object {
        private const val PREFS_NAME = "cleanswipe_settings"
        private const val KEY_AUTO_PLAY = "key_auto_play"
        private const val KEY_AUTO_MUTE = "key_auto_mute"
        private const val KEY_RETENTION_DAYS = "key_retention_days"

        @Volatile
        private var instance: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return instance ?: synchronized(this) {
                instance ?: SettingsManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
