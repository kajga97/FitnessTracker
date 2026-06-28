package com.example.fitnesstracker.tracking

data class CompletedTrackingSession(
    val activityType: String,
    val startTimeMillis: Long,
    val elapsedSeconds: Long,
    val distanceMeters: Double,
    val averageSpeedMetersPerSecond: Double,
    val routePoints: List<TrackingRoutePoint>
)
