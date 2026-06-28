package com.example.fitnesstracker.data.preferences

data class AppSettings(
    val languageCode: String = "en",
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val notificationsEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)
