package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LocationData
import com.example.model.Planet
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
import com.example.util.LocationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    currentLocation: LocationData,
    ephemeris: SolarCalculator.DayEphemeris,
    hasApiKey: Boolean,
    onSaveLocation: (LocationData) -> Unit,
    onAutoDetectLocation: ((Boolean, String) -> Unit) -> Unit,
    onSaveApiKey: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var customLat by remember { mutableStateOf(currentLocation.latitude.toString()) }
    var customLng by remember { mutableStateOf(currentLocation.longitude.toString()) }
    var customApiKey by remember { mutableStateOf("") }
    var apiKeySavedToast by remember { mutableStateOf(false) }

    var isDetectingLocation by remember { mutableStateOf(false) }
    var locationDetectionStatus by remember { mutableStateOf<String?>(null) }
    var locationDetectionSuccess by remember { mutableStateOf(false) }

    // Sync customLat and customLng when currentLocation changes
    LaunchedEffect(currentLocation) {
        customLat = currentLocation.latitude.toString()
        customLng = currentLocation.longitude.toString()
    }

    // Permission launcher for Location
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            isDetectingLocation = true
            locationDetectionStatus = "Acquiring GPS fix..."
            onAutoDetectLocation { success, message ->
                isDetectingLocation = false
                locationDetectionSuccess = success
                locationDetectionStatus = message
            }
        } else {
            isDetectingLocation = false
            locationDetectionSuccess = false
            locationDetectionStatus = "Location permission denied. You can select a city preset below."
        }
    }

    val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicVoid)
            .padding(horizontal = 16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // Location Settings Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = AstralGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Astrological Location & Coordinates",
                            color = AstralGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Planetary hours rely on exact solar sunrise and sunset calculations derived from geographical coordinates.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                    )

                    // Auto-Detect GPS Location Section
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = CosmicSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AstralGold.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auto_detect_gps_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(AstralGold.copy(alpha = 0.2f))
                                            .border(1.dp, AstralGold, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MyLocation,
                                            contentDescription = "GPS",
                                            tint = AstralGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Current: ${currentLocation.cityName}",
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${String.format(Locale.getDefault(), "%.4f", currentLocation.latitude)}°, ${String.format(Locale.getDefault(), "%.4f", currentLocation.longitude)}°",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (LocationHelper.hasLocationPermission(context)) {
                                            isDetectingLocation = true
                                            locationDetectionStatus = "Acquiring GPS fix..."
                                            onAutoDetectLocation { success, message ->
                                                isDetectingLocation = false
                                                locationDetectionSuccess = success
                                                locationDetectionStatus = message
                                            }
                                        } else {
                                            locationPermissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        }
                                    },
                                    enabled = !isDetectingLocation,
                                    colors = ButtonDefaults.buttonColors(containerColor = AstralGold),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("auto_detect_gps_button")
                                ) {
                                    if (isDetectingLocation) {
                                        CircularProgressIndicator(
                                            color = TextOnGold,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.GpsFixed,
                                            contentDescription = null,
                                            tint = TextOnGold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Auto Detect",
                                            color = TextOnGold,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Status feedback
                            AnimatedVisibility(visible = locationDetectionStatus != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (locationDetectionSuccess) Icons.Default.CheckCircle else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (locationDetectionSuccess) CelestialTeal else AstralGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = locationDetectionStatus.orEmpty(),
                                        color = if (locationDetectionSuccess) CelestialTeal else AstralGold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // City Presets Selector
                    Text(
                        text = "Or Select a World City Preset:",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(LocationData.DEFAULT_PRESETS) { preset ->
                            val isSelected = currentLocation.cityName == preset.cityName
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) CosmicCardElevated else CosmicSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) AstralGold else CosmicBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        onSaveLocation(preset)
                                        locationDetectionStatus = null
                                    }
                            ) {
                                Text(
                                    text = "${preset.cityName}, ${preset.country}",
                                    color = if (isSelected) AstralGold else TextPrimary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Manual Coordinates Input
                    Text(
                        text = "Manual Coordinate Entry:",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = customLat,
                            onValueChange = { customLat = it },
                            label = { Text("Latitude") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AstralGold,
                                unfocusedBorderColor = CosmicBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = customLng,
                            onValueChange = { customLng = it },
                            label = { Text("Longitude") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AstralGold,
                                unfocusedBorderColor = CosmicBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val lat = customLat.toDoubleOrNull() ?: currentLocation.latitude
                            val lng = customLng.toDoubleOrNull() ?: currentLocation.longitude
                            onSaveLocation(
                                LocationData(
                                    cityName = "Custom (${String.format(Locale.getDefault(), "%.2f, %.2f", lat, lng)})",
                                    country = "User Defined",
                                    latitude = lat,
                                    longitude = lng,
                                    timeZoneId = currentLocation.timeZoneId
                                )
                            )
                            locationDetectionStatus = "Applied coordinates: $lat, $lng"
                            locationDetectionSuccess = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AstralGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Apply Custom Coordinates", color = AstralGold, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Computed Ephemeris Snapshot for this location
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CosmicVoid,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Sunrise", color = TextMuted, fontSize = 10.sp)
                                Text(
                                    timeFmt.format(Date(ephemeris.sunriseEpochMs)),
                                    color = AstralGold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Sunset", color = TextMuted, fontSize = 10.sp)
                                Text(
                                    timeFmt.format(Date(ephemeris.sunsetEpochMs)),
                                    color = StarlightPurple,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Moon Phase", color = TextMuted, fontSize = 10.sp)
                                Text(
                                    ephemeris.moonPhase,
                                    color = CelestialTeal,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Gemini AI Engine & API Key
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = null,
                                tint = AstralGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini AI Engine & Grounding",
                                color = AstralGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (hasApiKey) CelestialTeal.copy(alpha = 0.2f) else CosmicSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (hasApiKey) CelestialTeal else TextMuted)
                        ) {
                            Text(
                                text = if (hasApiKey) "Active" else "Offline Engine",
                                color = if (hasApiKey) CelestialTeal else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Configured for gemini-3.8-flash with Google Search and Google Maps grounding for authentic real-time astronomical events and sacred geographic spots.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = customApiKey,
                        onValueChange = { customApiKey = it },
                        label = { Text("Override Gemini API Key (Optional)") },
                        placeholder = { Text("Enter key from AI Studio or Google AI") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AstralGold,
                            unfocusedBorderColor = CosmicBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (customApiKey.isNotBlank()) {
                                onSaveApiKey(customApiKey)
                                apiKeySavedToast = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AstralGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Custom API Key", color = TextOnGold, fontWeight = FontWeight.Bold)
                    }

                    if (apiKeySavedToast) {
                        Text(
                            text = "✓ API Key saved successfully",
                            color = CelestialTeal,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }

        // Planetary Magick & Chaldean Reference Guide
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "The Ancient Chaldean Order",
                        color = AstralGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "The planetary hours are based on the ancient Chaldean sequence of planets arranged by apparent geocentric speed: Saturn, Jupiter, Mars, Sun, Venus, Mercury, Moon.\n\n" +
                                "• 12 Daylight Hours: From sunrise to sunset, divided equally into 12 parts.\n" +
                                "• 12 Night Hours: From sunset to the next sunrise, divided equally into 12 parts.\n" +
                                "• Day Rulers: Sunday (Sun ☉), Monday (Moon ☽), Tuesday (Mars ♂), Wednesday (Mercury ☿), Thursday (Jupiter ♃), Friday (Venus ♀), Saturday (Saturn ♄).\n\n" +
                                "The first hour of daylight is always ruled by the day's regent, followed consecutively in the descending Chaldean circle.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Planet.values().forEach { p ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CosmicSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = p.glyph,
                                    color = p.uiColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(28.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${p.displayName} (${p.dayOfWeek})",
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = p.energyTheme,
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
