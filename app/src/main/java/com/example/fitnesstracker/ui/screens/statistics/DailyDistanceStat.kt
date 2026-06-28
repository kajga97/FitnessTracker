package com.example.fitnesstracker.ui.screens.statistics

import java.time.LocalDate

data class DailyDistanceStat(
    val date: LocalDate,
    val totalDistanceMeters: Double
)
