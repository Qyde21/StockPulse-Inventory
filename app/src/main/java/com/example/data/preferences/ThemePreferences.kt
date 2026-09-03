package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode(
    val title: String,
    val subtitle: String
) {
    SYSTEM("System Default", "Follows device appearance settings"),
    LIGHT("Light Mode", "Crisp bright appearance for daylight conditions"),
    DARK("Dark Mode", "High-contrast dark palette for low-light comfort")
}

class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("stockpulse_theme_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(readStoredThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private fun readStoredThemeMode(): AppThemeMode {
        val stored = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        return try {
            AppThemeMode.valueOf(stored)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun toggleDarkMode(currentIsDark: Boolean) {
        val newMode = if (currentIsDark) AppThemeMode.LIGHT else AppThemeMode.DARK
        setThemeMode(newMode)
    }

    companion object {
        private const val KEY_THEME_MODE = "pref_theme_mode"

        @Volatile
        private var INSTANCE: ThemePreferences? = null

        fun getInstance(context: Context): ThemePreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThemePreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
