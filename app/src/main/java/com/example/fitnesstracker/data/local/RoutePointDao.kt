package com.example.fitnesstracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutePointDao {
    @Insert
    suspend fun insertAll(routePoints: List<RoutePointEntity>)

    @Query("SELECT * FROM route_points WHERE activityId = :activityId ORDER BY timestamp ASC")
    fun observeByActivityId(activityId: Long): Flow<List<RoutePointEntity>>
}
