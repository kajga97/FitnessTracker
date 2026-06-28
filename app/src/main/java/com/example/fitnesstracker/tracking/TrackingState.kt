package com.example.fitnesstracker.tracking

data class TrackingState(
    val activityType: String = "",
    val startTimeMillis: Long = 0L,
    val isTracking: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedSeconds: Long = 0L,
    val distanceMeters: Double = 0.0,
    val currentSpeedMetersPerSecond: Double = 0.0,
    val routePoints: List<TrackingRoutePoint> = emptyList(),
    val locationPermissionDenied: Boolean = false,
    val errorMessage: String? = null
)
