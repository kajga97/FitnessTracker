package com.example.fitnesstracker.data.repository

import androidx.room.withTransaction
import com.example.fitnesstracker.data.local.ActivityEntity
import com.example.fitnesstracker.data.local.FitnessDatabase
import com.example.fitnesstracker.data.local.RoutePointEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class ActivityRepository(
    private val database: FitnessDatabase
) {
    private val activityDao = database.activityDao()
    private val routePointDao = database.routePointDao()

    fun observeActivities(): Flow<List<ActivityEntity>> = activityDao.observeAll()

    fun observeActivity(activityId: Long): Flow<ActivityEntity?> = activityDao.observeById(activityId)

    fun observeActivitiesFrom(fromTimestamp: Long): Flow<List<ActivityEntity>> =
        activityDao.observeFrom(fromTimestamp)

    fun observeRoutePoints(activityId: Long): Flow<List<RoutePointEntity>> =
        routePointDao.observeByActivityId(activityId)

    suspend fun getLatestActivity(): ActivityEntity? = activityDao.getLatest()

    suspend fun saveActivity(
        activity: ActivityEntity,
        routePoints: List<RoutePointEntity>
    ): Long {
        return database.withTransaction {
            val activityId = activityDao.insert(activity)
            if (routePoints.isNotEmpty()) {
                routePointDao.insertAll(
                    routePoints.map { routePoint ->
                        routePoint.copy(activityId = activityId)
                    }
                )
            }
            activityId
        }
    }

    suspend fun deleteActivity(activity: ActivityEntity) {
        activityDao.delete(activity)
    }

    fun observeActivitiesForLastDays(dayCount: Long): Flow<List<ActivityEntity>> {
        val fromDate = LocalDate.now()
            .minusDays(dayCount - 1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        return observeActivitiesFrom(fromDate)
    }

    fun activityDate(activity: ActivityEntity): LocalDate {
        return Instant.ofEpochMilli(activity.startTime)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
}
