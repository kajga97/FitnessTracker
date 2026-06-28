package com.example.fitnesstracker.util

import com.example.fitnesstracker.data.preferences.UnitSystem
import java.util.Locale

object DistanceFormatter {
    fun formatDistance(distanceMeters: Double, unitSystem: UnitSystem): String {
        val value = when (unitSystem) {
            UnitSystem.METRIC -> distanceMeters / 1000.0
            UnitSystem.IMPERIAL -> distanceMeters / 1609.344
        }
        return String.format(Locale.getDefault(), "%.2f", value)
    }

    fun formatSpeed(speedMetersPerSecond: Double, unitSystem: UnitSystem): String {
        val value = when (unitSystem) {
            UnitSystem.METRIC -> speedMetersPerSecond * 3.6
            UnitSystem.IMPERIAL -> speedMetersPerSecond * 2.236936
        }
        return String.format(Locale.getDefault(), "%.1f", value)
    }

    fun distanceUnitLabel(unitSystem: UnitSystem): String {
        return when (unitSystem) {
            UnitSystem.METRIC -> "km"
            UnitSystem.IMPERIAL -> "mi"
        }
    }

    fun speedUnitLabel(unitSystem: UnitSystem): String {
        return when (unitSystem) {
            UnitSystem.METRIC -> "km/h"
            UnitSystem.IMPERIAL -> "mph"
        }
    }
}
