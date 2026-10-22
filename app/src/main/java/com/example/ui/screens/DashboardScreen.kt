package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Planet
import com.example.model.PlanetaryHour
import com.example.model.SolarCalculator
import com.example.ui.theme.AstralGold
import com.example.ui.theme.AstralGoldDark
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    currentHour: PlanetaryHour?,
    ephemeris: SolarCalculator.DayEphemeris,
    currentTimeMs: Long,
    onNavigate: (AppScreen) -> Unit,
    onIntentionSelected: (Planet) -> Unit,
    modifier: Modifier = Modifier
) {
    val activePlanet = currentHour?.planet ?: ephemeris.dayRuler

    // Subtle breathing pulse for celestial orb
    val infiniteTransition = rememberInfiniteTransition(label = "celestial_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicVoid)
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
    ) {
        // Hero Card: Current Planetary Hour
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicCard),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, activePlanet.uiColor.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_planetary_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    activePlanet.uiColor.copy(alpha = 0.25f),
                                    CosmicCardElevated
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Badge: Day/Night Hour
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(CosmicSurface)
                                .border(1.dp, activePlanet.uiColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (currentHour?.isDayHour != false) Icons.Default.WbSunny else Icons.Default.NightsStay,
                                contentDescription = null,
                                tint = if (currentHour?.isDayHour != false) AstralGold else StarlightPurple,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentHour?.isDayHour != false)
                                    "Day Hour #${currentHour?.hourNumberInPeriod ?: 1} of 12"
                                else
                                    "Night Hour #${currentHour?.hourNumberInPeriod ?: 1} of 12",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Glowing Planetary Orb
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(110.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            activePlanet.uiColor.copy(alpha = 0.4f),
                                            activePlanet.uiColor.copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .border(2.dp, activePlanet.uiColor, CircleShape)
                        ) {
                            Text(
                                text = activePlanet.glyph,
                                color = activePlanet.uiColor,
                                fontSize = 54.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Planet Title & Range
                        Text(
                            text = "Hour of ${activePlanet.displayName}",
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = currentHour?.formattedTimeRange() ?: "Calculating...",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Countdown to next hour
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = CosmicVoid.copy(alpha = 0.7f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Time Remaining",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = currentHour?.remainingTimeFormatted(currentTimeMs) ?: "--:--",
                                        color = AstralGold,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val progress = currentHour?.progressFraction(currentTimeMs) ?: 0.5f
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = activePlanet.uiColor,
                                    trackColor = CosmicSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Energy Theme
                        Text(
                            text = "⚡ ${activePlanet.energyTheme}",
                            color = AstralGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Action Buttons Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onNavigate(AppScreen.AI_GUIDE) },
                    colors = ButtonDefaults.buttonColors(containerColor = AstralGold),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("consult_ai_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = TextOnGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Consult AI",
                        color = TextOnGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                OutlinedButton(
                    onClick = { onNavigate(AppScreen.JOURNAL) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StarlightPurple),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StarlightPurple),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("log_working_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = StarlightPurple,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Log Working",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Active Correspondences Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Active Correspondences",
                        color = AstralGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    CorrespondenceRow(label = "Colors", values = activePlanet.colors)
                    Spacer(modifier = Modifier.height(8.dp))
                    CorrespondenceRow(label = "Incenses", values = activePlanet.incenses)
                    Spacer(modifier = Modifier.height(8.dp))
                    CorrespondenceRow(label = "Gemstones", values = activePlanet.gemstones)
                    Spacer(modifier = Modifier.height(8.dp))
                    CorrespondenceRow(label = "Element / Metal", values = listOf(activePlanet.element, activePlanet.metal))
                }
            }
        }

        // Auspicious Magick & Activities
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Recommended in This Hour",
                            color = AstralGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AstralGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    activePlanet.recommendedActivities.forEach { activity ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•",
                                color = activePlanet.uiColor,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = activity,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // Intention Matcher (Quick Auspicious Hour Finder)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Find Best Hour by Intention",
                        color = AstralGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap any intention to jump to its upcoming ruling hour",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    val intentions = listOf(
                        Pair("Love & Beauty", Planet.VENUS),
                        Pair("Wealth & Luck", Planet.JUPITER),
                        Pair("Career & Vitality", Planet.SUN),
                        Pair("Courage & Energy", Planet.MARS),
                        Pair("Mind & Study", Planet.MERCURY),
                        Pair("Intuition & Healing", Planet.MOON),
                        Pair("Protection & Discipline", Planet.SATURN)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(intentions) { (label, planet) ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CosmicSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, planet.uiColor.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onIntentionSelected(planet) }
                                    .testTag("intention_chip_${planet.name.lowercase()}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = planet.glyph,
                                        color = planet.uiColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = label,
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Upcoming Hours Carousel
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Upcoming Today",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "View Full Schedule",
                        color = AstralGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { onNavigate(AppScreen.HOURS_CALCULATOR) }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val upcoming = ephemeris.allHours
                    .filter { it.startEpochMs >= currentTimeMs }
                    .take(5)

                if (upcoming.isEmpty()) {
                    Text(
                        text = "Night hours cycle ending soon. Sunrise brings the new day ruler.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(upcoming) { hour ->
                            UpcomingHourCard(hour = hour, onClick = { onNavigate(AppScreen.HOURS_CALCULATOR) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CorrespondenceRow(label: String, values: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            color = TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(95.dp)
        )
        Text(
            text = values.joinToString(", "),
            color = TextPrimary,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun UpcomingHourCard(hour: PlanetaryHour, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CosmicCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, hour.planet.uiColor.copy(alpha = 0.4f)),
        modifier = Modifier
            .width(135.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(hour.planet.uiColor.copy(alpha = 0.2f))
                    .border(1.dp, hour.planet.uiColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = hour.planet.glyph,
                    color = hour.planet.uiColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = hour.planet.displayName,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
            Text(
                text = timeFmt.format(Date(hour.startEpochMs)),
                color = AstralGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "${hour.durationMinutes} min",
                color = TextMuted,
                fontSize = 10.sp
            )
        }
    }
}
