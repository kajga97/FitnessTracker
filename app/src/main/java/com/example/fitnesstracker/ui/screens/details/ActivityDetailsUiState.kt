package com.example.fitnesstracker.ui.screens.details

import com.example.fitnesstracker.data.local.ActivityEntity
import com.example.fitnesstracker.data.local.RoutePointEntity

data class ActivityDetailsUiState(
    val isLoading: Boolean = true,
    val activity: ActivityEntity? = null,
    val routePoints: List<RoutePointEntity> = emptyList(),
    val isDeleted: Boolean = false
)
