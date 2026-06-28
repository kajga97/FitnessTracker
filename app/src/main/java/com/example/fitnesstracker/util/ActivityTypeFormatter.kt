package com.example.fitnesstracker.util

import androidx.annotation.StringRes
import com.example.fitnesstracker.R

object ActivityTypeFormatter {
    @StringRes
    fun labelResOrNull(type: String): Int? = when (type.lowercase()) {
        "walking" -> R.string.activity_type_walking
        "running" -> R.string.activity_type_running
        "cycling" -> R.string.activity_type_cycling
        else -> null
    }
}
