package com.example.fitnesstracker.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY type ASC")
    fun observeAll(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE type = :type LIMIT 1")
    fun observeByType(type: String): Flow<GoalEntity?>

    @Upsert
    suspend fun upsert(goal: GoalEntity)
}
