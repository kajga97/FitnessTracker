package com.example.fitnesstracker.data.repository

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.fitnesstracker.data.preferences.AppSettings
import com.example.fitnesstracker.data.preferences.ThemeMode
import com.example.fitnesstracker.data.preferences.UnitSystem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val dataStore: DataStore<Preferences>
) {
    val settings: Flow<AppSettings> = dataStore.data.map { preferences ->
        AppSettings(
            languageCode = preferences[LANGUAGE_CODE] ?: "en",
            unitSystem = preferences[UNIT_SYSTEM]
                ?.let(UnitSystem::valueOf)
                ?: UnitSystem.METRIC,
            notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true,
            themeMode = preferences[THEME_MODE]
                ?.let(ThemeMode::valueOf)
                ?: ThemeMode.SYSTEM
        )
    }

    suspend fun updateLanguage(languageCode: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_CODE] = languageCode
        }

        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(languageCode)
        )
    }

    suspend fun updateUnitSystem(unitSystem: UnitSystem) {
        dataStore.edit { preferences ->
            preferences[UNIT_SYSTEM] = unitSystem.name
        }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = themeMode.name
        }
    }

    companion object {
        private val LANGUAGE_CODE = stringPreferencesKey("language_code")
        private val UNIT_SYSTEM = stringPreferencesKey("unit_system")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
