package com.example.fitnesstracker.ui.screens.statistics

data class GoalProgressState(
    val targetValue: Double = 0.0,
    val currentValue: Double = 0.0
) {
    val progressFraction: Float
        get() = if (targetValue > 0.0) {
            (currentValue / targetValue).coerceIn(0.0, 1.0).toFloat()
        } else {
            0f
        }

    val progressPercent: Int
        get() = if (targetValue > 0.0) {
            (currentValue / targetValue * 100.0).toInt()
        } else {
            0
        }
}
