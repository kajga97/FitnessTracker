package com.example.fitnesstracker.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.preferences.AppSettings
import com.example.fitnesstracker.data.preferences.ThemeMode
import com.example.fitnesstracker.data.preferences.UnitSystem
import com.example.fitnesstracker.data.repository.SettingsRepository
import com.example.fitnesstracker.worker.InactivityReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val uiState: StateFlow<AppSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings()
    )

    fun updateLanguage(languageCode: String) {
        viewModelScope.launch {
            settingsRepository.updateLanguage(languageCode)
        }
    }

    fun updateUnitSystem(unitSystem: UnitSystem) {
        viewModelScope.launch {
            settingsRepository.updateUnitSystem(unitSystem)
        }
    }

    fun updateNotificationsEnabled(context: Context, enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateNotificationsEnabled(enabled)
            if (enabled) {
                InactivityReminderScheduler.schedule(context)
            } else {
                InactivityReminderScheduler.cancel(context)
            }
        }
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(themeMode)
        }
    }

    companion object {
        fun provideFactory(
            settingsRepository: SettingsRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                    return SettingsViewModel(settingsRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
