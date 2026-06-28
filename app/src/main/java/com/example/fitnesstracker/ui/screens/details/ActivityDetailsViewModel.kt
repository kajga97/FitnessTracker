package com.example.fitnesstracker.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.repository.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ActivityDetailsViewModel(
    private val activityId: Long,
    private val activityRepository: ActivityRepository
) : ViewModel() {
    private val deletedState = MutableStateFlow(false)

    val uiState: StateFlow<ActivityDetailsUiState> = combine(
        activityRepository.observeActivity(activityId),
        activityRepository.observeRoutePoints(activityId),
        deletedState
    ) { activity, routePoints, isDeleted ->
        ActivityDetailsUiState(
            isLoading = false,
            activity = activity,
            routePoints = routePoints,
            isDeleted = isDeleted
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ActivityDetailsUiState()
    )

    fun deleteActivity() {
        val activity = uiState.value.activity ?: return
        viewModelScope.launch {
            activityRepository.deleteActivity(activity)
            deletedState.value = true
        }
    }

    companion object {
        fun provideFactory(
            activityId: Long,
            activityRepository: ActivityRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ActivityDetailsViewModel::class.java)) {
                    return ActivityDetailsViewModel(
                        activityId = activityId,
                        activityRepository = activityRepository
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
