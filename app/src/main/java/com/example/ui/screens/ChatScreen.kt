package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ai.ChatMessage
import com.example.ai.GroundingType
import com.example.ai.MessageSender
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

@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    searchGroundingEnabled: Boolean,
    mapsGroundingEnabled: Boolean,
    currentHour: PlanetaryHour?,
    onSendMessage: (String) -> Unit,
    onToggleSearchGrounding: () -> Unit,
    onToggleMapsGrounding: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to bottom on new message
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val starterPrompts = listOf(
        "When is the best time for a love & harmony ritual?",
        "What should I focus on in this current hour?",
        "Plan a wealth & abundance manifestation for Thursday",
        "Find serene parks or botanicas nearby for grounding ritual",
        "What are the major planetary transits and astral events this week?"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicVoid)
            .imePadding()
            .testTag("chat_screen")
    ) {
        // Grounding Controls Header
        Surface(
            color = CosmicSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(CelestialTeal)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Model: gemini-3.8-flash",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (currentHour != null) {
                        Text(
                            text = "Context: ${currentHour.planet.displayName} (${currentHour.planet.glyph})",
                            color = currentHour.planet.uiColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Grounding Tool Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = searchGroundingEnabled,
                        onClick = onToggleSearchGrounding,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (searchGroundingEnabled) AstralGold else TextMuted
                            )
                        },
                        label = {
                            Text(
                                text = "Search Grounding",
                                fontSize = 11.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CosmicCardElevated,
                            selectedLabelColor = AstralGold,
                            containerColor = CosmicSurfaceVariant,
                            labelColor = TextMuted
                        ),
                        modifier = Modifier.testTag("toggle_search_grounding")
                    )

                    FilterChip(
                        selected = mapsGroundingEnabled,
                        onClick = onToggleMapsGrounding,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (mapsGroundingEnabled) CelestialTeal else TextMuted
                            )
                        },
                        label = {
                            Text(
                                text = "Maps Grounding",
                                fontSize = 11.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CosmicCardElevated,
                            selectedLabelColor = CelestialTeal,
                            containerColor = CosmicSurfaceVariant,
                            labelColor = TextMuted
                        ),
                        modifier = Modifier.testTag("toggle_maps_grounding")
                    )
                }
            }
        }

        // Messages Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages) { msg ->
                ChatMessageItem(message = msg)
            }

            if (isLoading) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            color = AstralGold,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Consulting the celestial spheres...",
                            color = AstralGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Starter Prompts (if chat is fresh or user wants inspiration)
        if (messages.size <= 2) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(starterPrompts) { prompt ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CosmicCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSendMessage(prompt) }
                    ) {
                        Text(
                            text = prompt,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Input Field Bar
        Surface(
            color = CosmicSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Ask about timing, rituals, or planetary guidance...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AstralGold,
                        unfocusedBorderColor = CosmicBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = AstralGold
                    ),
                    shape = RoundedCornerShape(20.dp),
                    maxLines = 4,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field")
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isLoading) {
                            val textToSend = inputText
                            inputText = ""
                            onSendMessage(textToSend)
                        }
                    },
                    enabled = inputText.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank() && !isLoading) AstralGold else CosmicSurfaceVariant)
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = if (inputText.isNotBlank() && !isLoading) TextOnGold else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    val isUser = message.sender == MessageSender.USER
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(CosmicCardElevated)
                        .border(1.dp, AstralGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = AstralGold,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
            }

            Column(
                modifier = Modifier.widthIn(max = 310.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    ),
                    color = if (isUser) CosmicCardElevated else CosmicCard,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isUser) StarlightPurple.copy(alpha = 0.5f) else CosmicBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = message.text,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )

                        // Grounding Badge & Source Links if available
                        if (message.groundingType != GroundingType.NONE || message.sources.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (message.groundingType == GroundingType.MAPS) Icons.Default.Map else Icons.Default.Language,
                                    contentDescription = null,
                                    tint = CelestialTeal,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (message.groundingType) {
                                        GroundingType.SEARCH -> "Grounded with Google Search"
                                        GroundingType.MAPS -> "Grounded with Google Maps"
                                        GroundingType.BOTH -> "Grounded with Search & Maps"
                                        else -> "Grounded Insights"
                                    },
                                    color = CelestialTeal,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (message.sources.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    message.sources.take(3).forEach { source ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable {
                                                    try {
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(source.uri))
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        // ignore
                                                    }
                                                }
                                                .background(CosmicSurfaceVariant)
                                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = source.title,
                                                color = AstralGold,
                                                fontSize = 10.sp,
                                                maxLines = 1,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.OpenInNew,
                                                contentDescription = null,
                                                tint = AstralGold,
                                                modifier = Modifier.size(10.dp)
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
    }
}
