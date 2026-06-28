package com.example.fitnesstracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ActivityEntity::class,
        RoutePointEntity::class,
        GoalEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FitnessDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun routePointDao(): RoutePointDao
    abstract fun goalDao(): GoalDao

    companion object {
        const val DATABASE_NAME = "fitness_tracker.db"
    }
}
