package com.example.fitnesstracker.ui.screens.history

import com.example.fitnesstracker.data.local.ActivityEntity

data class HistoryUiState(
    val filter: ActivityFilter = ActivityFilter(),
    val activities: List<ActivityEntity> = emptyList(),
    val totalActivityCount: Int = 0
)
