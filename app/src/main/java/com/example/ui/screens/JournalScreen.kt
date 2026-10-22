package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.database.JournalEntryEntity
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    journalEntries: List<JournalEntryEntity>,
    currentHour: PlanetaryHour?,
    todayEphemeris: SolarCalculator.DayEphemeris,
    selectedFilterPlanet: Planet?,
    searchQuery: String,
    onSetFilterPlanet: (Planet?) -> Unit,
    onSetSearchQuery: (String) -> Unit,
    onSaveEntry: (
        title: String,
        timestamp: Long,
        planet: Planet,
        hourNumber: Int,
        isDayHour: Boolean,
        hourTimeRange: String,
        moonPhase: String,
        activitiesPerformed: String,
        intentionSet: String,
        reflections: String,
        synchronicities: String,
        outcomeOfIntentions: String,
        rating: Int,
        tags: String
    ) -> Unit,
    onDeleteEntry: (JournalEntryEntity) -> Unit,
    onRequestAiInsight: (JournalEntryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var viewingEntry by remember { mutableStateOf<JournalEntryEntity?>(null) }
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Calculate quick stats
    val totalEntries = journalEntries.size
    val synchronicitiesCount = journalEntries.count { it.synchronicities.isNotBlank() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicVoid)
            .testTag("journal_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search & Stats Bar
            Surface(
                color = CosmicSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSetSearchQuery,
                        placeholder = {
                            Text(
                                "Search reflections, synchronicities, intentions...",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = AstralGold,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { onSetSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AstralGold,
                            unfocusedBorderColor = CosmicBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = AstralGold
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("journal_search_field")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Synchronicity & Magick Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Planetary Chronicles",
                            color = AstralGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "$totalEntries Entries",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "•",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "⚡ $synchronicitiesCount Synchronicities",
                                color = CelestialTeal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Planet Filter Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CosmicSurfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilterPlanet == null,
                        onClick = { onSetFilterPlanet(null) },
                        label = { Text("All Planets (${journalEntries.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CosmicCardElevated,
                            selectedLabelColor = AstralGold,
                            containerColor = CosmicSurface,
                            labelColor = TextSecondary
                        )
                    )
                }

                items(Planet.values()) { p ->
                    val isSelected = selectedFilterPlanet == p
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSetFilterPlanet(if (isSelected) null else p) },
                        leadingIcon = {
                            Text(text = p.glyph, color = p.uiColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        },
                        label = { Text(p.displayName, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CosmicCardElevated,
                            selectedLabelColor = p.uiColor,
                            containerColor = CosmicSurface,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            // Journal Entries List
            if (journalEntries.isEmpty()) {
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
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = AstralGold,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (searchQuery.isNotBlank() || selectedFilterPlanet != null)
                                "No Entries Matched Filter"
                            else
                                "Your Magickal Journal Awaits",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (searchQuery.isNotBlank() || selectedFilterPlanet != null)
                                "Try searching for a different keyword or resetting the planetary filter."
                            else
                                "Document your intentions, rituals performed, observed synchronicities, and outcomes for each planetary hour.",
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(journalEntries) { entry ->
                        StructuredJournalEntryCard(
                            entry = entry,
                            onViewDetail = { viewingEntry = entry },
                            onDelete = { onDeleteEntry(entry) },
                            onRequestAi = { onRequestAiInsight(entry) }
                        )
                    }
                }
            }
        }

        // Add Journal Entry FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = AstralGold,
            contentColor = TextOnGold,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_journal_entry_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Journal Entry")
        }
    }

    // New Guided Journal Entry Dialog
    if (showAddDialog) {
        GuidedJournalEntryDialog(
            activeHour = currentHour,
            todayHours = todayEphemeris.allHours,
            currentMoonPhase = todayEphemeris.moonPhase,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, timestamp, planet, hourNum, isDayHour, timeRange, moon, activities, intention, reflections, synchronicities, outcomes, rating, tags ->
                onSaveEntry(title, timestamp, planet, hourNum, isDayHour, timeRange, moon, activities, intention, reflections, synchronicities, outcomes, rating, tags)
                showAddDialog = false
            }
        )
    }

    // Detail Bottom Sheet
    viewingEntry?.let { entry ->
        ModalBottomSheet(
            onDismissRequest = { viewingEntry = null },
            sheetState = detailSheetState,
            containerColor = CosmicSurface,
            contentColor = TextPrimary
        ) {
            JournalEntryDetailContent(
                entry = entry,
                onRequestAi = {
                    scope.launch { detailSheetState.hide() }.invokeOnCompletion {
                        onRequestAiInsight(entry)
                        viewingEntry = null
                    }
                },
                onClose = {
                    scope.launch { detailSheetState.hide() }.invokeOnCompletion {
                        viewingEntry = null
                    }
                }
            )
        }
    }
}

@Composable
fun StructuredJournalEntryCard(
    entry: JournalEntryEntity,
    onViewDetail: () -> Unit,
    onDelete: () -> Unit,
    onRequestAi: () -> Unit
) {
    val planet = try {
        Planet.valueOf(entry.planetName)
    } catch (e: Exception) {
        Planet.SUN
    }

    val dateFmt = SimpleDateFormat("EEE, MMM d, yyyy • HH:mm", Locale.getDefault())

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, planet.uiColor.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onViewDetail)
            .testTag("journal_card_${entry.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Planet Badge & Date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(planet.uiColor.copy(alpha = 0.2f))
                            .border(1.2.dp, planet.uiColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = planet.glyph,
                            color = planet.uiColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Hour of ${planet.displayName} (${if (entry.isDayHour) "Day" else "Night"} #${entry.planetaryHourNumber})",
                            color = planet.uiColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${entry.hourTimeRange.ifBlank { dateFmt.format(Date(entry.timestamp)) }} • ${entry.moonPhase}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete entry",
                        tint = TextMuted.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = entry.title,
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            // Intention Set
            if (entry.intentionSet.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CosmicSurfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Intention: ",
                        color = AstralGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = entry.intentionSet,
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                }
            }

            // Activities Undertaken
            if (entry.activitiesPerformed.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Activities: ",
                        color = StarlightPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.width(70.dp)
                    )
                    Text(
                        text = entry.activitiesPerformed,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 2
                    )
                }
            }

            // Synchronicity Highlight (if noted)
            if (entry.synchronicities.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CelestialTeal.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CelestialTeal.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ Synchronicity: ",
                            color = CelestialTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = entry.synchronicities,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            maxLines = 2
                        )
                    }
                }
            }

            // Outcomes
            if (entry.outcomeOfIntentions.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = "Outcome: ",
                        color = AstralGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.width(70.dp)
                    )
                    Text(
                        text = entry.outcomeOfIntentions,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Rating and AI Insight Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Star Rating
                Row {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= entry.rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (i <= entry.rating) AstralGold else TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                // AI Insight Button
                Surface(
                    onClick = onRequestAi,
                    shape = RoundedCornerShape(12.dp),
                    color = CosmicSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StarlightPurple.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = StarlightPurple,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI Astrological Insight",
                            color = StarlightPurple,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JournalEntryDetailContent(
    entry: JournalEntryEntity,
    onRequestAi: () -> Unit,
    onClose: () -> Unit
) {
    val planet = try {
        Planet.valueOf(entry.planetName)
    } catch (e: Exception) {
        Planet.SUN
    }

    val dateFmt = SimpleDateFormat("EEEE, MMMM d, yyyy • HH:mm", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(planet.uiColor.copy(alpha = 0.2f))
                        .border(1.5.dp, planet.uiColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = planet.glyph,
                        color = planet.uiColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Hour of ${planet.displayName}",
                        color = planet.uiColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${if (entry.isDayHour) "Day" else "Night"} Hour #${entry.planetaryHourNumber} (${entry.hourTimeRange})",
                        color = AstralGold,
                        fontSize = 12.sp
                    )
                }
            }

            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = dateFmt.format(Date(entry.timestamp)),
            color = TextMuted,
            fontSize = 12.sp
        )

        Text(
            text = "Moon Phase: ${entry.moonPhase}",
            color = CelestialTeal,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = entry.title,
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Structured Field: Intention Set
        DetailSection(
            label = "Intention Set",
            color = AstralGold,
            content = entry.intentionSet.ifBlank { "No intention recorded" }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Structured Field: Activities Performed
        DetailSection(
            label = "Activities & Rituals Performed",
            color = StarlightPurple,
            content = entry.activitiesPerformed.ifBlank { "No activities specified" }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Structured Field: Personal Reflections
        DetailSection(
            label = "Personal Reflections & Observations",
            color = TextPrimary,
            content = entry.reflections.ifBlank { "No personal reflections noted" }
        )

        // Structured Field: Synchronicities Observed
        if (entry.synchronicities.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            DetailSection(
                label = "Synchronicities & Omens Observed ⚡",
                color = CelestialTeal,
                content = entry.synchronicities
            )
        }

        // Structured Field: Outcomes of Intentions
        if (entry.outcomeOfIntentions.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            DetailSection(
                label = "Outcomes of Intentions",
                color = AstralGold,
                content = entry.outcomeOfIntentions
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Star Rating
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Resonance Rating: ",
                color = TextSecondary,
                fontSize = 12.sp
            )
            for (i in 1..5) {
                Icon(
                    imageVector = if (i <= entry.rating) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = null,
                    tint = if (i <= entry.rating) AstralGold else TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRequestAi,
            colors = ButtonDefaults.buttonColors(containerColor = AstralGold),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = TextOnGold,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Get AI Astrological Interpretation",
                color = TextOnGold,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DetailSection(label: String, color: androidx.compose.ui.graphics.Color, content: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CosmicCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                color = TextPrimary,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
fun GuidedJournalEntryDialog(
    activeHour: PlanetaryHour?,
    todayHours: List<PlanetaryHour>,
    currentMoonPhase: String,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        timestamp: Long,
        planet: Planet,
        hourNumber: Int,
        isDayHour: Boolean,
        hourTimeRange: String,
        moonPhase: String,
        activitiesPerformed: String,
        intentionSet: String,
        reflections: String,
        synchronicities: String,
        outcomeOfIntentions: String,
        rating: Int,
        tags: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedHour by remember { mutableStateOf(activeHour ?: todayHours.firstOrNull()) }
    var selectedPlanet by remember { mutableStateOf(selectedHour?.planet ?: Planet.SUN) }

    var intentionSet by remember { mutableStateOf("") }
    var activitiesPerformed by remember { mutableStateOf("") }
    var reflections by remember { mutableStateOf("") }
    var synchronicities by remember { mutableStateOf("") }
    var outcomeOfIntentions by remember { mutableStateOf("") }
    var selectedRating by remember { mutableIntStateOf(5) }

    var showPromptInspirations by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CosmicSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = AstralGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Daily Planetary Journal",
                    color = AstralGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Section 1: Associated Planetary Hour Selection
                Text(
                    text = "Associated Planetary Hour:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(todayHours) { h ->
                        val isSelected = selectedHour == h
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) h.planet.uiColor.copy(alpha = 0.25f) else CosmicCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) h.planet.uiColor else CosmicBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedHour = h
                                    selectedPlanet = h.planet
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = h.planet.glyph, color = h.planet.uiColor, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${h.planet.displayName} #${h.hourNumberInPeriod}",
                                        color = if (isSelected) AstralGold else TextPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = h.formattedTimeRange(),
                                    color = TextMuted,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Entry Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (e.g. Venus Heart-Opening Meditation)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AstralGold,
                        unfocusedBorderColor = CosmicBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Field 1: Intention Set
                Text(
                    text = "1. Intention Set for this Hour:",
                    color = AstralGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "What was the purpose, prayer, or focused intention of your working?",
                    color = TextMuted,
                    fontSize = 10.sp
                )

                // Quick Intention Chips based on selected planet
                LazyRow(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(selectedPlanet.primaryIntentions) { intent ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CosmicSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    intentionSet = if (intentionSet.isBlank()) intent else "$intentionSet, $intent"
                                }
                        ) {
                            Text(
                                text = "+ $intent",
                                color = selectedPlanet.uiColor,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = intentionSet,
                    onValueChange = { intentionSet = it },
                    placeholder = { Text("Enter your intention...", color = TextMuted, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AstralGold,
                        unfocusedBorderColor = CosmicBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Field 2: Activities Performed
                Text(
                    text = "2. Activities Undertaken During this Time:",
                    color = StarlightPurple,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Rituals, meditations, tasks, or study completed under this hour's influence.",
                    color = TextMuted,
                    fontSize = 10.sp
                )

                // Quick Activity Suggestions based on planet
                LazyRow(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(selectedPlanet.recommendedActivities.take(3)) { act ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CosmicSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    activitiesPerformed = if (activitiesPerformed.isBlank()) act else "$activitiesPerformed; $act"
                                }
                        ) {
                            Text(
                                text = "+ ${act.take(24)}...",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = activitiesPerformed,
                    onValueChange = { activitiesPerformed = it },
                    placeholder = { Text("List practices, spells, study, or actions...", color = TextMuted, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AstralGold,
                        unfocusedBorderColor = CosmicBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Field 3: Personal Reflections & Experiences
                Text(
                    text = "3. Personal Reflections & Experiences:",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "What internal shifts, feelings, sensations, or insights arose?",
                    color = TextMuted,
                    fontSize = 10.sp
                )

                OutlinedTextField(
                    value = reflections,
                    onValueChange = { reflections = it },
                    placeholder = { Text("Describe how your energy and mind felt...", color = TextMuted, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AstralGold,
                        unfocusedBorderColor = CosmicBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Field 4: Synchronicities Observed
                Text(
                    text = "4. Synchronicities Observed ⚡:",
                    color = CelestialTeal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Signs, repeating numbers, unexpected messages, or meaningful coincidences.",
                    color = TextMuted,
                    fontSize = 10.sp
                )

                OutlinedTextField(
                    value = synchronicities,
                    onValueChange = { synchronicities = it },
                    placeholder = { Text("Note any symbolic encounters or occurrences...", color = TextMuted, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CelestialTeal,
                        unfocusedBorderColor = CosmicBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Field 5: Outcomes of Intentions
                Text(
                    text = "5. Outcomes of Your Intentions:",
                    color = AstralGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "What tangible results, emotional clarity, or manifestation progress manifested?",
                    color = TextMuted,
                    fontSize = 10.sp
                )

                OutlinedTextField(
                    value = outcomeOfIntentions,
                    onValueChange = { outcomeOfIntentions = it },
                    placeholder = { Text("Manifestation results, breakthroughs, or mood shifts...", color = TextMuted, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AstralGold,
                        unfocusedBorderColor = CosmicBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Star Rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Spiritual Resonance: ",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    for (star in 1..5) {
                        IconButton(
                            onClick = { selectedRating = star },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (star <= selectedRating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$star stars",
                                tint = if (star <= selectedRating) AstralGold else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = title.ifBlank {
                        "${selectedPlanet.displayName} Hour Working"
                    }
                    val hour = selectedHour
                    onConfirm(
                        finalTitle.trim(),
                        System.currentTimeMillis(),
                        selectedPlanet,
                        hour?.hourNumberInPeriod ?: 1,
                        hour?.isDayHour ?: true,
                        hour?.formattedTimeRange() ?: "",
                        currentMoonPhase,
                        activitiesPerformed.trim(),
                        intentionSet.trim(),
                        reflections.trim(),
                        synchronicities.trim(),
                        outcomeOfIntentions.trim(),
                        selectedRating,
                        ""
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = AstralGold)
            ) {
                Text("Save Chronicle", color = TextOnGold, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
