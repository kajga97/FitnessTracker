package com.example.fitnesstracker.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.local.ActivityEntity
import com.example.fitnesstracker.data.repository.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.Locale

class HistoryViewModel(
    private val activityRepository: ActivityRepository
) : ViewModel() {
    private val filterState = MutableStateFlow(ActivityFilter())

    val uiState: StateFlow<HistoryUiState> = combine(
        activityRepository.observeActivities(),
        filterState
    ) { activities, filter ->
        HistoryUiState(
            filter = filter,
            activities = activities.filter { activity ->
                activity.matchesFilter(filter)
            },
            totalActivityCount = activities.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState()
    )

    fun updateSearchQuery(searchQuery: String) {
        filterState.update { currentFilter ->
            currentFilter.copy(searchQuery = searchQuery)
        }
    }

    fun updateType(type: String?) {
        filterState.update { currentFilter ->
            currentFilter.copy(type = type)
        }
    }

    fun updateDatePreset(datePreset: HistoryDatePreset) {
        val fromDate = when (datePreset) {
            HistoryDatePreset.ANY_TIME -> null
            HistoryDatePreset.LAST_7_DAYS -> System.currentTimeMillis() - SEVEN_DAYS_MILLIS
            HistoryDatePreset.LAST_30_DAYS -> System.currentTimeMillis() - THIRTY_DAYS_MILLIS
        }

        filterState.update { currentFilter ->
            currentFilter.copy(
                fromDate = fromDate,
                toDate = null,
                datePreset = datePreset
            )
        }
    }

    fun updateMinimumDuration(minimumDurationSeconds: Long?) {
        filterState.update { currentFilter ->
            currentFilter.copy(minimumDurationSeconds = minimumDurationSeconds)
        }
    }

    private fun ActivityEntity.matchesFilter(filter: ActivityFilter): Boolean {
        val normalizedQuery = filter.searchQuery.trim().lowercase(Locale.ROOT)
        if (normalizedQuery.isNotEmpty()) {
            val typeMatches = type.lowercase(Locale.ROOT).contains(normalizedQuery)
            val descriptionMatches = description
                .orEmpty()
                .lowercase(Locale.ROOT)
                .contains(normalizedQuery)

            if (!typeMatches && !descriptionMatches) {
                return false
            }
        }

        if (filter.type != null && !type.equals(filter.type, ignoreCase = true)) {
            return false
        }

        if (filter.fromDate != null && startTime < filter.fromDate) {
            return false
        }

        if (filter.toDate != null && startTime > filter.toDate) {
            return false
        }

        if (filter.minimumDurationSeconds != null && durationSeconds < filter.minimumDurationSeconds) {
            return false
        }

        return true
    }

    companion object {
        private const val SEVEN_DAYS_MILLIS = 7L * 24L * 60L * 60L * 1000L
        private const val THIRTY_DAYS_MILLIS = 30L * 24L * 60L * 60L * 1000L

        fun provideFactory(
            activityRepository: ActivityRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                    return HistoryViewModel(activityRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
