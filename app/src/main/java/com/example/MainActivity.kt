package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.PlanetaryHour
import com.example.ui.components.PlanetaryNavigationDrawerSheet
import com.example.ui.components.PlanetaryTopAppBar
import com.example.ui.screens.ActivityPlannerScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HoursCalculatorScreen
import com.example.ui.screens.JournalScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.PlanetaryMagickTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.PlanetaryViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlanetaryMagickTheme {
                PlanetaryApp()
            }
        }
    }
}

@Composable
fun PlanetaryApp(
    viewModel: PlanetaryViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val location by viewModel.location.collectAsStateWithLifecycle()
    val todayEphemeris by viewModel.todayEphemeris.collectAsStateWithLifecycle()
    val selectedDateEphemeris by viewModel.selectedDateEphemeris.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val currentHour by viewModel.currentHour.collectAsStateWithLifecycle()
    val currentTimeMs by viewModel.currentTimeMs.collectAsStateWithLifecycle()

    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    val searchGroundingEnabled by viewModel.searchGroundingEnabled.collectAsStateWithLifecycle()
    val mapsGroundingEnabled by viewModel.mapsGroundingEnabled.collectAsStateWithLifecycle()
    val hasApiKey by viewModel.hasApiKey.collectAsStateWithLifecycle()

    val journalEntries by viewModel.filteredJournalEntries.collectAsStateWithLifecycle()
    val journalFilter by viewModel.journalPlanetFilter.collectAsStateWithLifecycle()
    val journalSearchQuery by viewModel.journalSearchQuery.collectAsStateWithLifecycle()
    val plannedActivities by viewModel.plannedActivities.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var hourToScheduleDirectly by remember { mutableStateOf<PlanetaryHour?>(null) }

    // Android Predictive / BackHandler support:
    // If drawer is open, close it; else if on secondary screen, go back to Dashboard.
    BackHandler(enabled = drawerState.isOpen || currentScreen != AppScreen.DASHBOARD) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            viewModel.selectScreen(AppScreen.DASHBOARD)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PlanetaryNavigationDrawerSheet(
                currentScreen = currentScreen,
                currentPlanet = currentHour?.planet ?: todayEphemeris.dayRuler,
                moonPhase = todayEphemeris.moonPhase,
                onSelectScreen = { screen -> viewModel.selectScreen(screen) },
                drawerState = drawerState,
                scope = scope
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                PlanetaryTopAppBar(
                    currentScreen = currentScreen,
                    currentPlanet = currentHour?.planet,
                    location = location,
                    onMenuClick = {
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    },
                    onQuickLocationClick = {
                        viewModel.selectScreen(AppScreen.SETTINGS)
                    }
                )
            }
        ) { innerPadding ->
            Crossfade(
                targetState = currentScreen,
                modifier = Modifier.padding(innerPadding),
                label = "screen_crossfade"
            ) { screen ->
                when (screen) {
                    AppScreen.DASHBOARD -> {
                        DashboardScreen(
                            currentHour = currentHour,
                            ephemeris = todayEphemeris,
                            currentTimeMs = currentTimeMs,
                            onNavigate = { target -> viewModel.selectScreen(target) },
                            onIntentionSelected = { planet ->
                                viewModel.selectScreen(AppScreen.HOURS_CALCULATOR)
                            }
                        )
                    }

                    AppScreen.HOURS_CALCULATOR -> {
                        HoursCalculatorScreen(
                            ephemeris = selectedDateEphemeris,
                            selectedDate = selectedDate,
                            location = location,
                            currentTimeMs = currentTimeMs,
                            onShiftDate = { delta -> viewModel.shiftSelectedDate(delta) },
                            onResetDateToToday = { viewModel.updateSelectedDate(java.util.Date()) },
                            onNavigate = { target -> viewModel.selectScreen(target) },
                            onScheduleHour = { hour ->
                                hourToScheduleDirectly = hour
                                viewModel.selectScreen(AppScreen.ACTIVITY_PLANNER)
                            }
                        )
                    }

                    AppScreen.ACTIVITY_PLANNER -> {
                        ActivityPlannerScreen(
                            plannedActivities = plannedActivities,
                            initialHourToSchedule = hourToScheduleDirectly,
                            onSaveActivity = { title, desc, planet, intention, timeMs, notes ->
                                viewModel.savePlannedActivity(title, desc, planet, intention, timeMs, notes)
                                hourToScheduleDirectly = null
                            },
                            onToggleCompleted = { activity ->
                                viewModel.togglePlannedActivityCompleted(activity)
                            },
                            onDeleteActivity = { activity ->
                                viewModel.deletePlannedActivity(activity)
                            },
                            onFindUpcomingHoursForPlanet = { planet ->
                                viewModel.findUpcomingHoursForPlanet(planet)
                            }
                        )
                    }

                    AppScreen.JOURNAL -> {
                        JournalScreen(
                            journalEntries = journalEntries,
                            currentHour = currentHour,
                            todayEphemeris = todayEphemeris,
                            selectedFilterPlanet = journalFilter,
                            searchQuery = journalSearchQuery,
                            onSetFilterPlanet = { planet -> viewModel.setJournalFilter(planet) },
                            onSetSearchQuery = { query -> viewModel.setJournalSearchQuery(query) },
                            onSaveEntry = { title, timestamp, planet, hourNum, isDayHour, timeRange, moon, activities, intention, reflections, synchronicities, outcomes, rating, tags ->
                                viewModel.saveJournalEntry(
                                    title, timestamp, planet, hourNum, isDayHour, timeRange, moon, activities, intention, reflections, synchronicities, outcomes, rating, tags
                                )
                            },
                            onDeleteEntry = { entry ->
                                viewModel.deleteJournalEntry(entry)
                            },
                            onRequestAiInsight = { entry ->
                                viewModel.requestAiInsightForJournal(entry)
                            }
                        )
                    }

                    AppScreen.AI_GUIDE -> {
                        ChatScreen(
                            messages = chatMessages,
                            isLoading = isChatLoading,
                            searchGroundingEnabled = searchGroundingEnabled,
                            mapsGroundingEnabled = mapsGroundingEnabled,
                            currentHour = currentHour,
                            onSendMessage = { text -> viewModel.sendChatMessage(text) },
                            onToggleSearchGrounding = { viewModel.toggleSearchGrounding() },
                            onToggleMapsGrounding = { viewModel.toggleMapsGrounding() }
                        )
                    }

                    AppScreen.SETTINGS -> {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        SettingsScreen(
                            currentLocation = location,
                            ephemeris = todayEphemeris,
                            hasApiKey = hasApiKey,
                            onSaveLocation = { loc -> viewModel.updateLocation(loc) },
                            onAutoDetectLocation = { onResult ->
                                viewModel.autoDetectLocation(context, onResult)
                            },
                            onSaveApiKey = { key -> viewModel.saveCustomApiKey(key) }
                        )
                    }
                }
            }
        }
    }
}
