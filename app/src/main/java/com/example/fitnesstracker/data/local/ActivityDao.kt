package com.example.fitnesstracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities ORDER BY startTime DESC")
    fun observeAll(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :id")
    fun observeById(id: Long): Flow<ActivityEntity?>

    @Query("SELECT * FROM activities WHERE startTime >= :fromTimestamp ORDER BY startTime DESC")
    fun observeFrom(fromTimestamp: Long): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities ORDER BY startTime DESC LIMIT 1")
    suspend fun getLatest(): ActivityEntity?

    @Insert
    suspend fun insert(activity: ActivityEntity): Long

    @Delete
    suspend fun delete(activity: ActivityEntity)
}
