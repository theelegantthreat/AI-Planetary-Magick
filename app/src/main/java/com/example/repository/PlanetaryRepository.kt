package com.example.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.ai.ChatMessage
import com.example.ai.ChatResponseResult
import com.example.ai.GeminiApiClient
import com.example.database.JournalEntryEntity
import com.example.database.MagickDao
import com.example.database.MagickDatabase
import com.example.database.PlannedActivityEntity
import com.example.model.LocationData
import com.example.model.PlanetaryHour
import com.example.model.SolarCalculator
import kotlinx.coroutines.flow.Flow
import java.util.Date

class PlanetaryRepository(context: Context) {

    private val db = MagickDatabase.getDatabase(context)
    private val dao: MagickDao = db.magickDao()
    private val geminiClient = GeminiApiClient()
    private val prefs: SharedPreferences = context.getSharedPreferences("planetary_prefs", Context.MODE_PRIVATE)

    // Location preferences
    fun getSavedLocation(): LocationData {
        val name = prefs.getString("city_name", LocationData.DEFAULT.cityName) ?: LocationData.DEFAULT.cityName
        val country = prefs.getString("country", LocationData.DEFAULT.country) ?: LocationData.DEFAULT.country
        val lat = prefs.getFloat("lat", LocationData.DEFAULT.latitude.toFloat()).toDouble()
        val lng = prefs.getFloat("lng", LocationData.DEFAULT.longitude.toFloat()).toDouble()
        val tz = prefs.getString("tz", LocationData.DEFAULT.timeZoneId) ?: LocationData.DEFAULT.timeZoneId
        return LocationData(name, country, lat, lng, tz)
    }

    fun saveLocation(location: LocationData) {
        prefs.edit()
            .putString("city_name", location.cityName)
            .putString("country", location.country)
            .putFloat("lat", location.latitude.toFloat())
            .putFloat("lng", location.longitude.toFloat())
            .putString("tz", location.timeZoneId)
            .apply()
    }

    fun getCustomApiKey(): String? {
        val key = prefs.getString("custom_api_key", null)
        return if (!key.isNullOrBlank()) key else null
    }

    fun saveCustomApiKey(key: String) {
        prefs.edit().putString("custom_api_key", key.trim()).apply()
    }

    fun calculateEphemeris(date: Date, location: LocationData): SolarCalculator.DayEphemeris {
        return SolarCalculator.calculateHoursForDate(date, location)
    }

    // Journal
    val allJournalEntries: Flow<List<JournalEntryEntity>> = dao.getAllJournalEntries()

    fun getJournalEntriesByPlanet(planet: String): Flow<List<JournalEntryEntity>> =
        dao.getJournalEntriesByPlanet(planet)

    suspend fun saveJournalEntry(entry: JournalEntryEntity): Long =
        dao.insertJournalEntry(entry)

    suspend fun deleteJournalEntry(entry: JournalEntryEntity) =
        dao.deleteJournalEntry(entry)

    // Planned Activities
    val allPlannedActivities: Flow<List<PlannedActivityEntity>> = dao.getAllPlannedActivities()

    suspend fun savePlannedActivity(activity: PlannedActivityEntity): Long =
        dao.insertPlannedActivity(activity)

    suspend fun toggleActivityCompleted(id: Long, isCompleted: Boolean) =
        dao.setActivityCompleted(id, isCompleted)

    suspend fun deletePlannedActivity(activity: PlannedActivityEntity) =
        dao.deletePlannedActivity(activity)

    // Gemini AI Chat
    suspend fun sendChatMessage(
        history: List<ChatMessage>,
        userMessage: String,
        currentHour: PlanetaryHour?,
        dayEphemeris: SolarCalculator.DayEphemeris?,
        locationName: String,
        enableSearchGrounding: Boolean,
        enableMapsGrounding: Boolean
    ): ChatResponseResult {
        val apiKey = getCustomApiKey()
        return geminiClient.sendMessage(
            history = history,
            userMessage = userMessage,
            currentHour = currentHour,
            dayEphemeris = dayEphemeris,
            locationName = locationName,
            customApiKey = apiKey,
            enableSearchGrounding = enableSearchGrounding,
            enableMapsGrounding = enableMapsGrounding
        )
    }

    fun hasApiKey(): Boolean {
        return geminiClient.getEffectiveApiKey(getCustomApiKey()).isNotBlank()
    }
}
