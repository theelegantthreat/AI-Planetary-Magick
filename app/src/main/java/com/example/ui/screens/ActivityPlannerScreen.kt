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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.database.PlannedActivityEntity
import com.example.model.Planet
import com.example.model.PlanetaryHour
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActivityPlannerScreen(
    plannedActivities: List<PlannedActivityEntity>,
    onSaveActivity: (title: String, desc: String, planet: Planet, intention: String, scheduledEpochMs: Long, notes: String) -> Unit,
    onToggleCompleted: (PlannedActivityEntity) -> Unit,
    onDeleteActivity: (PlannedActivityEntity) -> Unit,
    onFindUpcomingHoursForPlanet: (Planet) -> List<PlanetaryHour>,
    initialHourToSchedule: PlanetaryHour? = null,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(initialHourToSchedule != null) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Upcoming, 1: Completed

    val upcoming = plannedActivities.filter { !it.isCompleted }
    val completed = plannedActivities.filter { it.isCompleted }
    val displayed = if (selectedTab == 0) upcoming else completed

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicVoid)
            .testTag("activity_planner_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CosmicSurface,
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
                    text = { Text("Upcoming (${upcoming.size})", fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Completed (${completed.size})", fontSize = 13.sp) }
                )
            }

            if (displayed.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(CosmicCard)
                                .border(1.dp, CosmicBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventNote,
                                contentDescription = null,
                                tint = AstralGold,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (selectedTab == 0) "No Upcoming Rituals Planned" else "No Completed Rituals Yet",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (selectedTab == 0)
                                "Tap the + button to align your intentions with the sacred planetary hours."
                            else
                                "Completed magickal workings and intentions will appear here for reflection.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayed) { activity ->
                        PlannedActivityCard(
                            activity = activity,
                            onToggle = { onToggleCompleted(activity) },
                            onDelete = { onDeleteActivity(activity) }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Plan
        FloatingActionButton(
            onClick = { showDialog = true },
            containerColor = AstralGold,
            contentColor = TextOnGold,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("plan_activity_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Plan Ritual")
        }
    }

    // Add Activity Dialog
    if (showDialog) {
        PlanActivityDialog(
            initialHour = initialHourToSchedule,
            onDismiss = { showDialog = false },
            onConfirm = { title, desc, planet, intention, timeMs, notes ->
                onSaveActivity(title, desc, planet, intention, timeMs, notes)
                showDialog = false
            },
            onFindUpcomingHoursForPlanet = onFindUpcomingHoursForPlanet
        )
    }
}

@Composable
fun PlannedActivityCard(
    activity: PlannedActivityEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val planet = try {
        Planet.valueOf(activity.targetPlanetName)
    } catch (e: Exception) {
        Planet.SUN
    }

    val timeFmt = SimpleDateFormat("EEE, MMM d • HH:mm", Locale.getDefault())

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CosmicCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (activity.isCompleted) CosmicBorder else planet.uiColor.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggle, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = if (activity.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle completed",
                    tint = if (activity.isCompleted) CelestialTeal else TextMuted
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${planet.glyph} ${planet.displayName}",
                        color = planet.uiColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = timeFmt.format(Date(activity.scheduledEpochMs)),
                        color = AstralGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = activity.title,
                    color = if (activity.isCompleted) TextMuted else TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (activity.isCompleted) TextDecoration.LineThrough else null,
                    modifier = Modifier.padding(top = 2.dp)
                )

                if (activity.description.isNotBlank()) {
                    Text(
                        text = activity.description,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 2,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = TextMuted.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun PlanActivityDialog(
    initialHour: PlanetaryHour?,
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, planet: Planet, intention: String, scheduledTimeMs: Long, notes: String) -> Unit,
    onFindUpcomingHoursForPlanet: (Planet) -> List<PlanetaryHour>
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPlanet by remember { mutableStateOf(initialHour?.planet ?: Planet.VENUS) }
    var scheduledEpochMs by remember { mutableStateOf(initialHour?.startEpochMs ?: System.currentTimeMillis()) }
    var selectedHourDisplay by remember { mutableStateOf(initialHour?.formattedTimeRange() ?: "Select Upcoming Hour") }

    val upcomingHours = remember(selectedPlanet) {
        onFindUpcomingHoursForPlanet(selectedPlanet)
    }

    val timeFmt = SimpleDateFormat("EEE, MMM d HH:mm", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CosmicSurface,
        title = {
            Text(
                text = "Plan Magickal Working",
                color = AstralGold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Working Title (e.g. Venus Self-Love Bath)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AstralGold,
                        unfocusedBorderColor = CosmicBorder,
                        focusedLabelColor = AstralGold,
                        unfocusedLabelColor = TextMuted,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Resonant Planetary Ruler:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(Planet.values()) { p ->
                        val isSelected = selectedPlanet == p
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) CosmicCardElevated else CosmicSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) p.uiColor else CosmicBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedPlanet = p
                                    val first = onFindUpcomingHoursForPlanet(p).firstOrNull()
                                    if (first != null) {
                                        scheduledEpochMs = first.startEpochMs
                                        selectedHourDisplay = "${timeFmt.format(Date(first.startEpochMs))} (${first.formattedTimeRange()})"
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = p.glyph, color = p.uiColor, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = p.displayName,
                                    color = if (isSelected) AstralGold else TextPrimary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Select Upcoming Auspicious Hour:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(upcomingHours) { h ->
                        val isSelected = scheduledEpochMs == h.startEpochMs
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) AstralGold else CosmicCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) AstralGold else CosmicBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    scheduledEpochMs = h.startEpochMs
                                    selectedHourDisplay = "${timeFmt.format(Date(h.startEpochMs))} (${h.formattedTimeRange()})"
                                }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(
                                    text = timeFmt.format(Date(h.startEpochMs)),
                                    color = if (isSelected) TextOnGold else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = h.formattedTimeRange(),
                                    color = if (isSelected) TextOnGold else TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Ritual Notes, Offerings, or Intentions") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AstralGold,
                        unfocusedBorderColor = CosmicBorder,
                        focusedLabelColor = AstralGold,
                        unfocusedLabelColor = TextMuted,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            title.trim(),
                            description.trim(),
                            selectedPlanet,
                            selectedPlanet.primaryIntentions.firstOrNull() ?: "Magick",
                            scheduledEpochMs,
                            ""
                        )
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AstralGold)
            ) {
                Text("Save to Schedule", color = TextOnGold, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
