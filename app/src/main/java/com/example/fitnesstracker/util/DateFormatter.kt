package com.example.fitnesstracker.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

object DateFormatter {
    fun formatDateTime(timestampMillis: Long): String {
        val formatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault())

        return formatter.format(Instant.ofEpochMilli(timestampMillis))
    }
}
