package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PlanetaryHour(
    val hourNumberInCycle: Int, // 1 to 24
    val hourNumberInPeriod: Int, // 1 to 12
    val isDayHour: Boolean,
    val planet: Planet,
    val startEpochMs: Long,
    val endEpochMs: Long
) {
    val durationMinutes: Long
        get() = (endEpochMs - startEpochMs) / 60000

    fun isCurrent(atEpochMs: Long = System.currentTimeMillis()): Boolean {
        return atEpochMs in startEpochMs until endEpochMs
    }

    fun formattedTimeRange(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return "${sdf.format(Date(startEpochMs))} - ${sdf.format(Date(endEpochMs))}"
    }

    fun remainingTimeFormatted(currentEpochMs: Long = System.currentTimeMillis()): String {
        if (!isCurrent(currentEpochMs)) return "Ended"
        val remainingMs = endEpochMs - currentEpochMs
        val mins = (remainingMs / 60000).coerceAtLeast(0)
        val secs = ((remainingMs % 60000) / 1000).coerceAtLeast(0)
        return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }

    fun progressFraction(currentEpochMs: Long = System.currentTimeMillis()): Float {
        if (currentEpochMs <= startEpochMs) return 0f
        if (currentEpochMs >= endEpochMs) return 1f
        val total = (endEpochMs - startEpochMs).toFloat()
        return if (total > 0) (currentEpochMs - startEpochMs) / total else 0f
    }
}
