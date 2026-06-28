package com.example.fitnesstracker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.room.Room
import com.example.fitnesstracker.data.local.FitnessDatabase
import com.example.fitnesstracker.data.preferences.SettingsDataStore
import com.example.fitnesstracker.data.repository.ActivityRepository
import com.example.fitnesstracker.data.repository.GoalRepository
import com.example.fitnesstracker.data.repository.SettingsRepository
import com.example.fitnesstracker.tracking.DefaultLocationClient
import com.example.fitnesstracker.tracking.TrackingManager
import com.example.fitnesstracker.worker.InactivityReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FitnessTrackerApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: FitnessDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            FitnessDatabase::class.java,
            FitnessDatabase.DATABASE_NAME
        ).build()
    }

    val activityRepository: ActivityRepository by lazy {
        ActivityRepository(database)
    }

    val goalRepository: GoalRepository by lazy {
        GoalRepository(database.goalDao())
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(SettingsDataStore.create(applicationContext))
    }

    val trackingManager: TrackingManager by lazy {
        TrackingManager(
            locationClient = DefaultLocationClient(applicationContext)
        )
    }

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            val settings = settingsRepository.settings.first()
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(settings.languageCode)
            )

            if (settings.notificationsEnabled) {
                InactivityReminderScheduler.schedule(applicationContext)
            } else {
                InactivityReminderScheduler.cancel(applicationContext)
            }
        }
    }
}
