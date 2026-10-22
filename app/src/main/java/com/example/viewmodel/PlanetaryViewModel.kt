package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.ChatMessage
import com.example.ai.MessageSender
import com.example.database.JournalEntryEntity
import com.example.database.PlannedActivityEntity
import com.example.model.LocationData
import com.example.model.Planet
import com.example.model.PlanetaryHour
import com.example.model.SolarCalculator
import com.example.repository.PlanetaryRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

enum class AppScreen(val title: String) {
    DASHBOARD("Dashboard"),
    HOURS_CALCULATOR("Hours Calculator"),
    ACTIVITY_PLANNER("Activity Planner"),
    JOURNAL("Magickal Journal"),
    AI_GUIDE("AI Guide & Chat"),
    SETTINGS("Settings & Ephemeris")
}

class PlanetaryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PlanetaryRepository(application)

    // Current Navigation Screen
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Location
    private val _location = MutableStateFlow(repository.getSavedLocation())
    val location: StateFlow<LocationData> = _location.asStateFlow()

    // Selected Date for Hours Calculator
    private val _selectedDate = MutableStateFlow(Date())
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()

    // Current Time in epoch ms (ticks every second for accurate countdown)
    private val _currentTimeMs = MutableStateFlow(System.currentTimeMillis())
    val currentTimeMs: StateFlow<Long> = _currentTimeMs.asStateFlow()

    // Ephemeris for Today (recalculated when location or day changes)
    private val _todayEphemeris = MutableStateFlow(
        repository.calculateEphemeris(Date(), _location.value)
    )
    val todayEphemeris: StateFlow<SolarCalculator.DayEphemeris> = _todayEphemeris.asStateFlow()

    // Ephemeris for Selected Date
    private val _selectedDateEphemeris = MutableStateFlow(
        repository.calculateEphemeris(_selectedDate.value, _location.value)
    )
    val selectedDateEphemeris: StateFlow<SolarCalculator.DayEphemeris> = _selectedDateEphemeris.asStateFlow()

    // Currently active planetary hour
    private val _currentHour = MutableStateFlow<PlanetaryHour?>(null)
    val currentHour: StateFlow<PlanetaryHour?> = _currentHour.asStateFlow()

    // Chat
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.AI_GUIDE,
                text = "Greetings, traveler of the cosmos. I am the AI Planetary Magick Engine. The planetary spheres turn in eternal harmony above you. Ask me about auspicious timings, ritual recommendations for the active hour, or when the heavens best align with your intention."
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _searchGroundingEnabled = MutableStateFlow(true)
    val searchGroundingEnabled: StateFlow<Boolean> = _searchGroundingEnabled.asStateFlow()

    private val _mapsGroundingEnabled = MutableStateFlow(true)
    val mapsGroundingEnabled: StateFlow<Boolean> = _mapsGroundingEnabled.asStateFlow()

    private val _hasApiKey = MutableStateFlow(repository.hasApiKey())
    val hasApiKey: StateFlow<Boolean> = _hasApiKey.asStateFlow()

    // Journal
    val journalEntries: StateFlow<List<JournalEntryEntity>> = repository.allJournalEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _journalPlanetFilter = MutableStateFlow<Planet?>(null)
    val journalPlanetFilter: StateFlow<Planet?> = _journalPlanetFilter.asStateFlow()

    private val _journalSearchQuery = MutableStateFlow("")
    val journalSearchQuery: StateFlow<String> = _journalSearchQuery.asStateFlow()

    val filteredJournalEntries: StateFlow<List<JournalEntryEntity>> = combine(
        journalEntries,
        _journalPlanetFilter,
        _journalSearchQuery
    ) { entries, filter, query ->
        var list = entries
        if (filter != null) {
            list = list.filter { it.planetName.equals(filter.name, ignoreCase = true) }
        }
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) ||
                        it.intentionSet.lowercase().contains(q) ||
                        it.activitiesPerformed.lowercase().contains(q) ||
                        it.reflections.lowercase().contains(q) ||
                        it.synchronicities.lowercase().contains(q) ||
                        it.outcomeOfIntentions.lowercase().contains(q) ||
                        it.tags.lowercase().contains(q)
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Planned Activities
    val plannedActivities: StateFlow<List<PlannedActivityEntity>> = repository.allPlannedActivities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        recalculateEphemeris()

        // Time ticker loop
        viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                _currentTimeMs.value = now
                val active = SolarCalculator.findActiveHour(_todayEphemeris.value, now)
                if (_currentHour.value != active) {
                    _currentHour.value = active
                }
                delay(1000)
            }
        }
    }

    fun selectScreen(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun updateLocation(newLocation: LocationData) {
        _location.value = newLocation
        repository.saveLocation(newLocation)
        recalculateEphemeris()
    }

    fun autoDetectLocation(context: android.content.Context, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val detected = com.example.util.LocationHelper.detectCurrentLocation(context)
            if (detected != null) {
                updateLocation(detected)
                onResult(true, "GPS Detected: ${detected.cityName}, ${detected.country}")
            } else {
                onResult(false, "Could not acquire GPS location. Check if Location is enabled on your device.")
            }
        }
    }

    fun updateSelectedDate(date: Date) {
        _selectedDate.value = date
        _selectedDateEphemeris.value = repository.calculateEphemeris(date, _location.value)
    }

    fun shiftSelectedDate(days: Int) {
        val cal = Calendar.getInstance().apply {
            time = _selectedDate.value
            add(Calendar.DAY_OF_MONTH, days)
        }
        updateSelectedDate(cal.time)
    }

    private fun recalculateEphemeris() {
        val today = Date()
        val loc = _location.value
        _todayEphemeris.value = repository.calculateEphemeris(today, loc)
        _selectedDateEphemeris.value = repository.calculateEphemeris(_selectedDate.value, loc)
        _currentHour.value = SolarCalculator.findActiveHour(_todayEphemeris.value, System.currentTimeMillis())
    }

    // Chat Actions
    fun sendChatMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val userMsg = ChatMessage(
            sender = MessageSender.USER,
            text = trimmed
        )
        _chatMessages.value = _chatMessages.value + userMsg
        _isChatLoading.value = true

        viewModelScope.launch {
            val response = repository.sendChatMessage(
                history = _chatMessages.value,
                userMessage = trimmed,
                currentHour = _currentHour.value,
                dayEphemeris = _todayEphemeris.value,
                locationName = _location.value.cityName,
                enableSearchGrounding = _searchGroundingEnabled.value,
                enableMapsGrounding = _mapsGroundingEnabled.value
            )

            val aiMsg = ChatMessage(
                sender = MessageSender.AI_GUIDE,
                text = response.text,
                groundingType = response.groundingType,
                searchQueries = response.searchQueries,
                sources = response.sources
            )
            _chatMessages.value = _chatMessages.value + aiMsg
            _isChatLoading.value = false
        }
    }

    fun toggleSearchGrounding() {
        _searchGroundingEnabled.value = !_searchGroundingEnabled.value
    }

    fun toggleMapsGrounding() {
        _mapsGroundingEnabled.value = !_mapsGroundingEnabled.value
    }

    fun saveCustomApiKey(key: String) {
        repository.saveCustomApiKey(key)
        _hasApiKey.value = repository.hasApiKey()
    }

    // Journal Actions
    fun saveJournalEntry(
        title: String,
        timestamp: Long = System.currentTimeMillis(),
        planet: Planet,
        hourNumber: Int,
        isDayHour: Boolean = true,
        hourTimeRange: String = "",
        moonPhase: String = "",
        activitiesPerformed: String,
        intentionSet: String,
        reflections: String,
        synchronicities: String,
        outcomeOfIntentions: String,
        rating: Int,
        tags: String
    ) {
        viewModelScope.launch {
            repository.saveJournalEntry(
                JournalEntryEntity(
                    title = title,
                    timestamp = timestamp,
                    planetName = planet.name,
                    planetaryHourNumber = hourNumber,
                    isDayHour = isDayHour,
                    hourTimeRange = hourTimeRange,
                    moonPhase = moonPhase,
                    activitiesPerformed = activitiesPerformed,
                    intentionSet = intentionSet,
                    reflections = reflections,
                    synchronicities = synchronicities,
                    outcomeOfIntentions = outcomeOfIntentions,
                    rating = rating,
                    tags = tags
                )
            )
        }
    }

    fun deleteJournalEntry(entry: JournalEntryEntity) {
        viewModelScope.launch {
            repository.deleteJournalEntry(entry)
        }
    }

    fun setJournalFilter(planet: Planet?) {
        _journalPlanetFilter.value = planet
    }

    fun setJournalSearchQuery(query: String) {
        _journalSearchQuery.value = query
    }

    fun requestAiInsightForJournal(entry: JournalEntryEntity) {
        val prompt = buildString {
            appendLine("🌟 [PLANETARY JOURNAL ENTRY ANALYSIS REQUEST]")
            appendLine("Planet: ${entry.planetName} (${if (entry.isDayHour) "Day" else "Night"} Hour #${entry.planetaryHourNumber}, ${entry.hourTimeRange})")
            appendLine("Moon Phase: ${entry.moonPhase}")
            appendLine("Intention Set: ${entry.intentionSet}")
            appendLine("Activities Undertaken: ${entry.activitiesPerformed}")
            appendLine("Personal Reflections: ${entry.reflections}")
            if (entry.synchronicities.isNotBlank()) {
                appendLine("Observed Synchronicities: ${entry.synchronicities}")
            }
            if (entry.outcomeOfIntentions.isNotBlank()) {
                appendLine("Observed Outcomes: ${entry.outcomeOfIntentions}")
            }
            appendLine("\nAs the AI Planetary Magick Engine, provide your mystical and practical interpretation of this working. What astrological synchronicities and celestial alignments are reflected in these experiences, and what advice do you offer for future workings?")
        }
        selectScreen(AppScreen.AI_GUIDE)
        sendChatMessage(prompt)
    }

    // Planned Activities Actions
    fun savePlannedActivity(
        title: String,
        description: String,
        targetPlanet: Planet,
        intentionCategory: String,
        scheduledEpochMs: Long,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.savePlannedActivity(
                PlannedActivityEntity(
                    title = title,
                    description = description,
                    targetPlanetName = targetPlanet.name,
                    intentionCategory = intentionCategory,
                    scheduledEpochMs = scheduledEpochMs,
                    notes = notes
                )
            )
        }
    }

    fun togglePlannedActivityCompleted(activity: PlannedActivityEntity) {
        viewModelScope.launch {
            repository.toggleActivityCompleted(activity.id, !activity.isCompleted)
        }
    }

    fun deletePlannedActivity(activity: PlannedActivityEntity) {
        viewModelScope.launch {
            repository.deletePlannedActivity(activity)
        }
    }

    // Intention Finder
    fun findUpcomingHoursForPlanet(targetPlanet: Planet, maxCount: Int = 5): List<PlanetaryHour> {
        val now = System.currentTimeMillis()
        val results = mutableListOf<PlanetaryHour>()

        // Check today's ephemeris
        val todayMatching = _todayEphemeris.value.allHours.filter {
            it.planet == targetPlanet && it.endEpochMs > now
        }
        results.addAll(todayMatching)

        // If not enough, calculate tomorrow
        if (results.size < maxCount) {
            val tomorrow = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 1)
            }.time
            val tomorrowEphemeris = repository.calculateEphemeris(tomorrow, _location.value)
            val tomorrowMatching = tomorrowEphemeris.allHours.filter { it.planet == targetPlanet }
            results.addAll(tomorrowMatching)
        }

        return results.take(maxCount)
    }
}
