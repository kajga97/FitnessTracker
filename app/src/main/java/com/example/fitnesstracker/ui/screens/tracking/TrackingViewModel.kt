package com.example.fitnesstracker.ui.screens.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.local.ActivityEntity
import com.example.fitnesstracker.data.local.RoutePointEntity
import com.example.fitnesstracker.data.repository.ActivityRepository
import com.example.fitnesstracker.tracking.CompletedTrackingSession
import com.example.fitnesstracker.tracking.TrackingManager
import com.example.fitnesstracker.tracking.TrackingState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrackingViewModel(
    private val activityRepository: ActivityRepository,
    private val trackingManager: TrackingManager
) : ViewModel() {
    val trackingState: StateFlow<TrackingState> = trackingManager.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = trackingManager.state.value
    )

    fun startTracking(activityType: String) {
        val currentState = trackingState.value
        if (currentState.isTracking || currentState.routePoints.isNotEmpty()) {
            return
        }

        trackingManager.clearPermissionDenied()
        trackingManager.startTracking(activityType)
    }

    fun pauseTracking() {
        trackingManager.pauseTracking()
    }

    fun resumeTracking() {
        trackingManager.resumeTracking()
    }

    fun markPermissionDenied() {
        trackingManager.markLocationPermissionDenied()
    }

    fun clearPermissionDenied() {
        trackingManager.clearPermissionDenied()
    }

    fun stopAndSave(
        description: String,
        onSaved: () -> Unit
    ) {
        val completedSession = trackingManager.stopTracking() ?: trackingManager.currentCompletedSession()
        if (completedSession == null) {
            return
        }

        viewModelScope.launch {
            activityRepository.saveActivity(
                activity = completedSession.toActivityEntity(description),
                routePoints = completedSession.toRoutePointEntities()
            )
            trackingManager.clearTracking()
            onSaved()
        }
    }

    fun discardTracking() {
        trackingManager.clearTracking()
    }

    companion object {
        fun provideFactory(
            activityRepository: ActivityRepository,
            trackingManager: TrackingManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(TrackingViewModel::class.java)) {
                    return TrackingViewModel(activityRepository, trackingManager) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }

        private fun CompletedTrackingSession.toActivityEntity(description: String): ActivityEntity {
            return ActivityEntity(
                type = activityType,
                startTime = startTimeMillis,
                durationSeconds = elapsedSeconds,
                distanceMeters = distanceMeters,
                averageSpeedMetersPerSecond = averageSpeedMetersPerSecond,
                description = description.ifBlank { null }
            )
        }

        private fun CompletedTrackingSession.toRoutePointEntities(): List<RoutePointEntity> {
            return routePoints.map { routePoint ->
                RoutePointEntity(
                    activityId = 0L,
                    latitude = routePoint.latitude,
                    longitude = routePoint.longitude,
                    timestamp = routePoint.timestamp
                )
            }
        }
    }
}
