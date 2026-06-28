package com.example.fitnesstracker.util

object DurationFormatter {
    fun formatDuration(durationSeconds: Long): String {
        val hours = durationSeconds / 3600
        val minutes = (durationSeconds % 3600) / 60
        val seconds = durationSeconds % 60

        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }
}
