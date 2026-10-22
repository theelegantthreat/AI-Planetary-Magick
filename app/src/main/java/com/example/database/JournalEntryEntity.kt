package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val planetName: String,
    val planetaryHourNumber: Int,
    val isDayHour: Boolean = true,
    val hourTimeRange: String = "",
    val moonPhase: String = "",
    val activitiesPerformed: String = "",
    val intentionSet: String = "",
    val reflections: String = "",
    val synchronicities: String = "",
    val outcomeOfIntentions: String = "",
    val rating: Int = 5, // 1 to 5 stars
    val tags: String = ""
) {
    // Convenience property for backward compatibility with general content searches
    val content: String
        get() = buildString {
            if (reflections.isNotBlank()) append(reflections)
            if (synchronicities.isNotBlank()) append("\n\nSynchronicities: ").append(synchronicities)
            if (outcomeOfIntentions.isNotBlank()) append("\n\nOutcomes: ").append(outcomeOfIntentions)
        }
}
