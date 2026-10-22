package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LocationData
import com.example.model.Planet
import com.example.ui.theme.AstralGold
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicCard
import com.example.ui.theme.CosmicCardElevated
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicSurfaceVariant
import com.example.ui.theme.CosmicVoid
import com.example.ui.theme.StarlightPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AppScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanetaryTopAppBar(
    currentScreen: AppScreen,
    currentPlanet: Planet?,
    location: LocationData,
    onMenuClick: () -> Unit,
    onQuickLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        modifier = modifier.testTag("planetary_top_app_bar"),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currentScreen.title,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = AstralGold,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = location.cityName,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.testTag("nav_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Navigation Menu",
                    tint = AstralGold
                )
            }
        },
        actions = {
            if (currentPlanet != null) {
                Surface(
                    onClick = onQuickLocationClick,
                    shape = RoundedCornerShape(16.dp),
                    color = CosmicCardElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, currentPlanet.uiColor.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .testTag("active_planet_badge")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = currentPlanet.glyph,
                            color = currentPlanet.uiColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentPlanet.displayName,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = CosmicVoid
        )
    )
}

@Composable
fun PlanetaryNavigationDrawerSheet(
    currentScreen: AppScreen,
    currentPlanet: Planet?,
    moonPhase: String,
    onSelectScreen: (AppScreen) -> Unit,
    drawerState: DrawerState,
    scope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier
            .width(310.dp)
            .testTag("planetary_drawer_sheet"),
        drawerContainerColor = CosmicSurface,
        drawerContentColor = TextPrimary
    ) {
        // Mystical Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(CosmicCardElevated, CosmicSurface)
                    )
                )
                .padding(top = 28.dp, bottom = 20.dp, start = 20.dp, end = 20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(AstralGold.copy(alpha = 0.3f), CosmicCard)
                                )
                            )
                            .border(1.5.dp, AstralGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentPlanet?.glyph ?: "☉",
                            color = currentPlanet?.uiColor ?: AstralGold,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "AI Planetary Magick",
                            color = AstralGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Chaldean Astrological Timing",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Active Cosmos Snapshot
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CosmicVoid.copy(alpha = 0.7f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Active Hour",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "${currentPlanet?.displayName ?: "Solar"} Hour",
                                color = currentPlanet?.uiColor ?: AstralGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Moon Phase",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Text(
                                text = moonPhase,
                                color = StarlightPurple,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Navigation Items
        val items = listOf(
            Triple(AppScreen.DASHBOARD, Icons.Default.AutoAwesome, "Current Hour & Magick"),
            Triple(AppScreen.HOURS_CALCULATOR, Icons.Default.Schedule, "24-Hour Planetary Table"),
            Triple(AppScreen.ACTIVITY_PLANNER, Icons.Default.EventNote, "Ritual & Task Scheduler"),
            Triple(AppScreen.JOURNAL, Icons.Default.MenuBook, "Magickal Records & Results"),
            Triple(AppScreen.AI_GUIDE, Icons.Default.Psychology, "AI Planetary Engine & Chat"),
            Triple(AppScreen.SETTINGS, Icons.Default.Settings, "Locations & Ephemeris")
        )

        items.forEach { (screen, icon, subtitle) ->
            val selected = currentScreen == screen
            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = screen.title,
                        tint = if (selected) AstralGold else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Column {
                        Text(
                            text = screen.title,
                            color = if (selected) AstralGold else TextPrimary,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Text(
                            text = subtitle,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                },
                selected = selected,
                onClick = {
                    onSelectScreen(screen)
                    scope.launch { drawerState.close() }
                },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = CosmicSurfaceVariant,
                    unselectedContainerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 3.dp)
                    .testTag("drawer_item_${screen.name.lowercase()}")
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Saturn • Jupiter • Mars • Sun • Venus • Mercury • Moon",
                color = TextMuted,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
