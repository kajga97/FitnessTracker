package com.example.fitnesstracker.ui.screens.statistics

data class StatisticsUiState(
    val dailyDistanceGoalInput: String = "",
    val weeklyActiveTimeGoalInputHours: String = "",
    val dailyDistanceGoalProgress: GoalProgressState = GoalProgressState(),
    val weeklyActiveTimeGoalProgress: GoalProgressState = GoalProgressState(),
    val sevenDayDistanceStats: List<DailyDistanceStat> = emptyList()
)
