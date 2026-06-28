package com.example.fitnesstracker.tracking

import android.location.Location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max

class TrackingManager(
    private val locationClient: LocationClient
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutableState = MutableStateFlow(TrackingState())

    val state: StateFlow<TrackingState> = mutableState.asStateFlow()

    private var timerJob: Job? = null
    private var locationJob: Job? = null
    private var lastAcceptedLocation: Location? = null
    private var completedSession: CompletedTrackingSession? = null

    fun startTracking(activityType: String) {
        completedSession = null
        lastAcceptedLocation = null
        mutableState.value = TrackingState(
            activityType = activityType,
            startTimeMillis = System.currentTimeMillis(),
            isTracking = true
        )

        startTimer()
        observeLocations()
    }

    fun pauseTracking() {
        mutableState.value = mutableState.value.copy(
            isPaused = true,
            currentSpeedMetersPerSecond = 0.0
        )
    }

    fun resumeTracking() {
        mutableState.value = mutableState.value.copy(
            isPaused = false,
            errorMessage = null
        )
    }

    fun stopTracking(): CompletedTrackingSession? {
        stopInternal()
        val stateValue = mutableState.value
        val averageSpeed = if (stateValue.elapsedSeconds > 0) {
            stateValue.distanceMeters / stateValue.elapsedSeconds.toDouble()
        } else {
            0.0
        }

        completedSession = CompletedTrackingSession(
            activityType = stateValue.activityType,
            startTimeMillis = stateValue.startTimeMillis,
            elapsedSeconds = stateValue.elapsedSeconds,
            distanceMeters = stateValue.distanceMeters,
            averageSpeedMetersPerSecond = averageSpeed,
            routePoints = stateValue.routePoints
        )

        mutableState.value = mutableState.value.copy(
            isTracking = false,
            isPaused = false,
            currentSpeedMetersPerSecond = 0.0
        )

        return completedSession
    }

    fun currentCompletedSession(): CompletedTrackingSession? = completedSession

    fun clearTracking() {
        stopInternal()
        completedSession = null
        lastAcceptedLocation = null
        mutableState.value = TrackingState()
    }

    fun markLocationPermissionDenied() {
        mutableState.value = mutableState.value.copy(
            locationPermissionDenied = true
        )
    }

    fun clearPermissionDenied() {
        mutableState.value = mutableState.value.copy(
            locationPermissionDenied = false
        )
    }

    fun shutdown() {
        stopInternal()
        scope.cancel()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                delay(1_000L)
                val currentState = mutableState.value
                if (currentState.isTracking && !currentState.isPaused) {
                    mutableState.value = currentState.copy(
                        elapsedSeconds = currentState.elapsedSeconds + 1
                    )
                }
            }
        }
    }

    private fun observeLocations() {
        locationJob?.cancel()
        locationJob = scope.launch {
            locationClient.getLocationUpdates().collect { location ->
                val currentState = mutableState.value
                if (!currentState.isTracking || currentState.isPaused) {
                    return@collect
                }

                if (location.hasAccuracy() && location.accuracy > 50f) {
                    return@collect
                }

                val previousLocation = lastAcceptedLocation
                val segmentDistance = if (previousLocation != null) {
                    max(0.0, previousLocation.distanceTo(location).toDouble())
                } else {
                    0.0
                }

                val calculatedSpeed = if (location.hasSpeed()) {
                    location.speed.toDouble()
                } else if (previousLocation != null) {
                    val timeDeltaSeconds =
                        (location.time - previousLocation.time).coerceAtLeast(1L) / 1000.0
                    if (timeDeltaSeconds > 0.0) segmentDistance / timeDeltaSeconds else 0.0
                } else {
                    0.0
                }

                val routePoint = TrackingRoutePoint(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timestamp = location.time
                )

                lastAcceptedLocation = location
                mutableState.value = currentState.copy(
                    distanceMeters = currentState.distanceMeters + segmentDistance,
                    currentSpeedMetersPerSecond = calculatedSpeed,
                    routePoints = currentState.routePoints + routePoint,
                    errorMessage = null
                )
            }
        }
    }

    private fun stopInternal() {
        timerJob?.cancel()
        locationJob?.cancel()
        timerJob = null
        locationJob = null
        lastAcceptedLocation = null
    }
}
