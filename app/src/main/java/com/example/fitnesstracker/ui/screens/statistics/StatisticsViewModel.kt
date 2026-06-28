package com.example.fitnesstracker.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.local.GoalEntity
import com.example.fitnesstracker.data.local.GoalType
import com.example.fitnesstracker.data.preferences.UnitSystem
import com.example.fitnesstracker.data.repository.ActivityRepository
import com.example.fitnesstracker.data.repository.GoalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

class StatisticsViewModel(
    private val activityRepository: ActivityRepository,
    private val goalRepository: GoalRepository
) : ViewModel() {
    private val inputState = MutableStateFlow(StatisticsInputState())

    val uiState: StateFlow<StatisticsUiState> = combine(
        activityRepository.observeActivities(),
        goalRepository.observeGoals(),
        inputState
    ) { activities, goals, input ->
        val today = LocalDate.now()
        val currentWeekStart = today.with(DayOfWeek.MONDAY)

        val sevenDayStats = (0L..6L).map { dayOffset ->
            val date = today.minusDays(6L - dayOffset)
            DailyDistanceStat(
                date = date,
                totalDistanceMeters = activities
                    .filter { activityRepository.activityDate(it) == date }
                    .sumOf { it.distanceMeters }
            )
        }

        val dailyGoal = goals.firstOrNull { it.type == GoalType.DAILY_DISTANCE }
        val weeklyGoal = goals.firstOrNull { it.type == GoalType.WEEKLY_ACTIVE_TIME }

        val todayDistanceMeters = activities
            .filter { activityRepository.activityDate(it) == today }
            .sumOf { it.distanceMeters }

        val currentWeekActiveSeconds = activities
            .filter { activity ->
                val activityDate = activityRepository.activityDate(activity)
                !activityDate.isBefore(currentWeekStart) && !activityDate.isAfter(today)
            }
            .sumOf { it.durationSeconds }

        StatisticsUiState(
            dailyDistanceGoalInput = input.dailyDistanceGoalInput.ifBlank {
                dailyGoal?.targetValue?.div(1000.0)?.let { formatGoalInput(it) }.orEmpty()
            },
            weeklyActiveTimeGoalInputHours = input.weeklyActiveTimeGoalInputHours.ifBlank {
                weeklyGoal?.targetValue?.div(3600.0)?.let { formatGoalInput(it) }.orEmpty()
            },
            dailyDistanceGoalProgress = GoalProgressState(
                targetValue = dailyGoal?.targetValue ?: 0.0,
                currentValue = todayDistanceMeters
            ),
            weeklyActiveTimeGoalProgress = GoalProgressState(
                targetValue = weeklyGoal?.targetValue ?: 0.0,
                currentValue = currentWeekActiveSeconds.toDouble()
            ),
            sevenDayDistanceStats = sevenDayStats
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatisticsUiState()
    )

    fun updateDailyDistanceGoalInput(value: String) {
        inputState.update { currentState ->
            currentState.copy(dailyDistanceGoalInput = value)
        }
    }

    fun updateWeeklyActiveTimeGoalInputHours(value: String) {
        inputState.update { currentState ->
            currentState.copy(weeklyActiveTimeGoalInputHours = value)
        }
    }

    fun saveDailyDistanceGoal(unitSystem: UnitSystem) {
        val parsedDistance = inputState.value.dailyDistanceGoalInput.toDoubleOrNull() ?: return
        val targetMeters = when (unitSystem) {
            UnitSystem.METRIC -> parsedDistance * 1000.0
            UnitSystem.IMPERIAL -> parsedDistance * 1609.344
        }

        viewModelScope.launch {
            goalRepository.upsertGoal(
                GoalEntity(
                    type = GoalType.DAILY_DISTANCE,
                    targetValue = targetMeters
                )
            )
            inputState.update { currentState ->
                currentState.copy(dailyDistanceGoalInput = "")
            }
        }
    }

    fun saveWeeklyActiveTimeGoal() {
        val parsedHours = inputState.value.weeklyActiveTimeGoalInputHours.toDoubleOrNull() ?: return
        viewModelScope.launch {
            goalRepository.upsertGoal(
                GoalEntity(
                    type = GoalType.WEEKLY_ACTIVE_TIME,
                    targetValue = parsedHours * 3600.0
                )
            )
            inputState.update { currentState ->
                currentState.copy(weeklyActiveTimeGoalInputHours = "")
            }
        }
    }

    companion object {
        fun provideFactory(
            activityRepository: ActivityRepository,
            goalRepository: GoalRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
                    return StatisticsViewModel(activityRepository, goalRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }

        private fun formatGoalInput(value: Double): String {
            return if (value % 1.0 == 0.0) {
                value.toInt().toString()
            } else {
                String.format(Locale.getDefault(), "%.1f", value)
            }
        }
    }
}

private data class StatisticsInputState(
    val dailyDistanceGoalInput: String = "",
    val weeklyActiveTimeGoalInputHours: String = ""
)
