package com.example.fitnesstracker.data.repository

import com.example.fitnesstracker.data.local.GoalDao
import com.example.fitnesstracker.data.local.GoalEntity
import kotlinx.coroutines.flow.Flow

class GoalRepository(
    private val goalDao: GoalDao
) {
    fun observeGoals(): Flow<List<GoalEntity>> = goalDao.observeAll()

    fun observeGoal(type: String): Flow<GoalEntity?> = goalDao.observeByType(type)

    suspend fun upsertGoal(goal: GoalEntity) {
        goalDao.upsert(goal)
    }
}
