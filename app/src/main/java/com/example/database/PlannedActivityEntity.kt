package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "planned_activities")
data class PlannedActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val targetPlanetName: String,
    val intentionCategory: String,
    val scheduledEpochMs: Long,
    val isCompleted: Boolean = false,
    val notes: String = ""
)
