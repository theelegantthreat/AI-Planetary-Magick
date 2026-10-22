package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LocationData
import com.example.model.Planet
import com.example.model.PlanetaryHour
import com.example.model.SolarCalculator
import com.example.ui.theme.AstralGold
import com.example.ui.theme.CelestialTeal
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicCard
import com.example.ui.theme.CosmicCardElevated
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicSurfaceVariant
import com.example.ui.theme.CosmicVoid
import com.example.ui.theme.StarlightPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextOnGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AppScreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoursCalculatorScreen(
    ephemeris: SolarCalculator.DayEphemeris,
    selectedDate: Date,
    location: LocationData,
    currentTimeMs: Long,
    onShiftDate: (Int) -> Unit,
    onResetDateToToday: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onScheduleHour: (PlanetaryHour) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Day (1-12), 2: Night (1-12)
    var selectedPlanetFilter by remember { mutableStateOf<Planet?>(null) }
    var detailHour by remember { mutableStateOf<PlanetaryHour?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val dateFmt = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    val filteredHours = remember(ephemeris, selectedTab, selectedPlanetFilter) {
        val base = when (selectedTab) {
            1 -> ephemeris.dayHours
            2 -> ephemeris.nightHours
            else -> ephemeris.allHours
        }
        if (selectedPlanetFilter != null) {
            base.filter { it.planet == selectedPlanetFilter }
        } else {
            base
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicVoid)
            .testTag("hours_calculator_screen")
    ) {
        // Date Selector Bar
        Surface(
            color = CosmicSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { onShiftDate(-1) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Day",
                            tint = AstralGold
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = dateFmt.format(selectedDate),
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "Day Ruler: ",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${ephemeris.dayRuler.displayName} (${ephemeris.dayRuler.glyph})",
                                color = ephemeris.dayRuler.uiColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = { onShiftDate(1) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Day",
                            tint = AstralGold
                        )
                    }
                }

                // Solar Ephemeris Summary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = AstralGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Rise: ${timeFmt.format(Date(ephemeris.sunriseEpochMs))}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NightsStay,
                            contentDescription = null,
                            tint = StarlightPurple,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Set: ${timeFmt.format(Date(ephemeris.sunsetEpochMs))}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = "Moon: ${ephemeris.moonPhase}",
                        color = CelestialTeal,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Tabs: All / Day / Night
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CosmicSurfaceVariant,
            contentColor = AstralGold,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = AstralGold
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("All 24h", fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Day (1-12)", fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Night (1-12)", fontSize = 13.sp) }
            )
        }

        // Planet Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedPlanetFilter == null,
                    onClick = { selectedPlanetFilter = null },
                    label = { Text("All Planets", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CosmicCardElevated,
                        selectedLabelColor = AstralGold,
                        containerColor = CosmicSurface,
                        labelColor = TextSecondary
                    )
                )
            }
            items(Planet.values()) { planet ->
                val isSelected = selectedPlanetFilter == planet
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedPlanetFilter = if (isSelected) null else planet
                    },
                    leadingIcon = {
                        Text(
                            text = planet.glyph,
                            color = planet.uiColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    label = { Text(planet.displayName, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CosmicCardElevated,
                        selectedLabelColor = planet.uiColor,
                        containerColor = CosmicSurface,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        // Hours List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
        ) {
            items(filteredHours) { hour ->
                val isCurrent = hour.isCurrent(currentTimeMs)
                PlanetaryHourListItem(
                    hour = hour,
                    isCurrent = isCurrent,
                    onClick = { detailHour = hour }
                )
            }
        }
    }

    // Detail Bottom Sheet
    detailHour?.let { hour ->
        ModalBottomSheet(
            onDismissRequest = { detailHour = null },
            sheetState = sheetState,
            containerColor = CosmicSurface,
            contentColor = TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(hour.planet.uiColor.copy(alpha = 0.2f))
                            .border(1.5.dp, hour.planet.uiColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = hour.planet.glyph,
                            color = hour.planet.uiColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hour of ${hour.planet.displayName}",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${if (hour.isDayHour) "Day" else "Night"} Hour #${hour.hourNumberInPeriod} • ${hour.formattedTimeRange()} (${hour.durationMinutes} min)",
                            color = AstralGold,
                            fontSize = 13.sp
                        )
                    }

                    IconButton(onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion { detailHour = null }
                    }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Vibe: ${hour.planet.energyTheme}",
                    color = AstralGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(14.dp))

                CorrespondenceRow(label = "Colors", values = hour.planet.colors)
                Spacer(modifier = Modifier.height(6.dp))
                CorrespondenceRow(label = "Incenses", values = hour.planet.incenses)
                Spacer(modifier = Modifier.height(6.dp))
                CorrespondenceRow(label = "Gemstones", values = hour.planet.gemstones)
                Spacer(modifier = Modifier.height(6.dp))
                CorrespondenceRow(label = "Element", values = listOf(hour.planet.element))

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Auspicious Intentions:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = hour.planet.primaryIntentions.joinToString(" • "),
                    color = TextPrimary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                onScheduleHour(hour)
                                detailHour = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AstralGold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = null,
                            tint = TextOnGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Schedule Ritual", color = TextOnGold, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                onNavigate(AppScreen.AI_GUIDE)
                                detailHour = null
                            }
                        },
                        border = androidx.compose.foundation.BorderStroke(1.dp, StarlightPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ask AI Guide", color = StarlightPurple, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PlanetaryHourListItem(
    hour: PlanetaryHour,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isCurrent) CosmicCardElevated else CosmicCard,
        border = androidx.compose.foundation.BorderStroke(
            if (isCurrent) 1.5.dp else 1.dp,
            if (isCurrent) AstralGold else CosmicBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("hour_item_${hour.hourNumberInCycle}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Planet Glyph Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(hour.planet.uiColor.copy(alpha = 0.2f))
                    .border(1.dp, hour.planet.uiColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = hour.planet.glyph,
                    color = hour.planet.uiColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Main Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = hour.planet.displayName,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hour.isDayHour) "Day #${hour.hourNumberInPeriod}" else "Night #${hour.hourNumberInPeriod}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    if (isCurrent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AstralGold,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = TextOnGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = hour.planet.primaryIntentions.take(3).joinToString(", "),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Time & Duration
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = hour.formattedTimeRange(),
                    color = if (isCurrent) AstralGold else TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${hour.durationMinutes} min",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
