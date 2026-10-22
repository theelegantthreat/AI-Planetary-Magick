package com.example.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MagickDao {

    // Journal Operations
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllJournalEntries(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE planetName = :planetName ORDER BY timestamp DESC")
    fun getJournalEntriesByPlanet(planetName: String): Flow<List<JournalEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntryEntity): Long

    @Update
    suspend fun updateJournalEntry(entry: JournalEntryEntity)

    @Delete
    suspend fun deleteJournalEntry(entry: JournalEntryEntity)

    // Planned Activities Operations
    @Query("SELECT * FROM planned_activities ORDER BY scheduledEpochMs ASC")
    fun getAllPlannedActivities(): Flow<List<PlannedActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannedActivity(activity: PlannedActivityEntity): Long

    @Update
    suspend fun updatePlannedActivity(activity: PlannedActivityEntity)

    @Query("UPDATE planned_activities SET isCompleted = :completed WHERE id = :id")
    suspend fun setActivityCompleted(id: Long, completed: Boolean)

    @Delete
    suspend fun deletePlannedActivity(activity: PlannedActivityEntity)
}
