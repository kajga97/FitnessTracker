package com.example.fitnesstracker.ui.screens.history

data class ActivityFilter(
    val searchQuery: String = "",
    val type: String? = null,
    val fromDate: Long? = null,
    val toDate: Long? = null,
    val minimumDurationSeconds: Long? = null,
    val datePreset: HistoryDatePreset = HistoryDatePreset.ANY_TIME
)
