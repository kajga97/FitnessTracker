package com.example.fitnesstracker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.fitnesstracker.ui.navigation.FitnessTrackerApp
import com.example.fitnesstracker.ui.theme.FitnessTrackerTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val application = application as FitnessTrackerApplication
        setContent {
            val settings by application.settingsRepository.settings.collectAsState(
                initial = com.example.fitnesstracker.data.preferences.AppSettings()
            )

            FitnessTrackerTheme(themeMode = settings.themeMode) {
                FitnessTrackerApp(settings = settings)
            }
        }
    }
}
