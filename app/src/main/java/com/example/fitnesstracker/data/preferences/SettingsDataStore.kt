package com.example.fitnesstracker.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile

object SettingsDataStore {
    const val FILE_NAME = "settings.preferences_pb"

    fun create(context: Context) = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile(FILE_NAME) }
    )
}
