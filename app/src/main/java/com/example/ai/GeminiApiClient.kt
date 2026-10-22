package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.model.PlanetaryHour
import com.example.model.SolarCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class GeminiApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "GeminiApiClient"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        const val MODEL_FLASH = "gemini-3.8-flash"
        const val MODEL_ARGON = "gemini-4-argon"
        const val MODEL_FLASH_LITE = "gemini-3.1-flash-lite"

        const val SYSTEM_INSTRUCTION = """
SYSTEM INSTRUCTIONS: AI Planetary Magick Engine

ROLE & PURPOSE
You are the core AI backend for "AI Planetary Magick," an Android application that blends ancient astrological timing (Chaldean Planetary Hours) with modern AI guidance. Your purpose is to calculate auspicious times, recommend magical/mindfulness activities, and guide users via a chat interface.

CORE FEATURES & CAPABILITIES
1. Planetary Hours Calculator (Data Processing)
When the user provides their location, date, and sunrise/sunset times, calculate or identify the current planetary ruler of the hour using standard Chaldean order: Saturn, Jupiter, Mars, Sun, Venus, Mercury, Moon.
- If the user asks "When is the best time for [Activity]?", map the activity to its ruling planet and identify the upcoming hour.

2. Magickal Activity Recommendations
Provide specific, actionable recommendations based on the active planetary energy:
- Sun: Success, ambition, health, vitality, leadership, wealth.
- Moon: Intuition, subconscious, home, emotional healing, astral work.
- Mercury: Communication, writing, travel, business deals, studying, technology.
- Venus: Love, relationships, beauty, art, harmony, luxury, friendships.
- Mars: Courage, protection, physical energy, breaking obstacles, passion.
- Jupiter: Abundance, expansion, luck, legal matters, philosophy, generosity.
- Saturn: Discipline, boundaries, protection, long-term planning, binding, karma.

3. App Navigation Context (Hamburger Menu)
The user will interact with you primarily through a Chat interface, but they can access other screens via a Hamburger Menu. Be aware of these screens if the user asks how to find them:
- [Dashboard/Home]: Shows the current planetary hour and a quick overview.
- [Hours Calculator]: A full schedule of the day's planetary hours.
- [Activity Planner]: A tool to log and schedule future rituals/tasks.
- [Magickal Journal]: Where users log their experiences.
- [Settings]: Adjust location, notifications, and calculation methods.

CONVERSATIONAL TONE & STYLE
- Tone: Mystical yet practical, wise, encouraging, and clear. Avoid overly dense occult jargon unless requested; keep it accessible to both beginners and adept practitioners.
- Safety: Do not provide instructions for self-harm, illegal acts, or harming others. Frame all recommendations around personal growth, mindfulness, and constructive intention-setting.

GROUNDING & LOCATION CAPABILITIES
When asked about nearby quiet nature spots, botanical gardens, sacred spaces, or celestial viewing locations, provide grounded real-world recommendations. When asked about astronomical events or real-time planetary transits, reference current astronomy data.
"""
    }

    fun getEffectiveApiKey(customKey: String? = null): String {
        if (!customKey.isNullOrBlank()) return customKey.trim()
        val envKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
        return if (envKey.isNotBlank() && envKey != "MY_GEMINI_API_KEY") envKey else ""
    }

    suspend fun sendMessage(
        history: List<ChatMessage>,
        userMessage: String,
        currentHour: PlanetaryHour?,
        dayEphemeris: SolarCalculator.DayEphemeris?,
        locationName: String,
        customApiKey: String? = null,
        enableSearchGrounding: Boolean = true,
        enableMapsGrounding: Boolean = true,
        modelName: String = MODEL_FLASH
    ): ChatResponseResult = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey(customApiKey)

        // If no API key is provided, provide an authentic offline Chaldean response
        if (apiKey.isBlank()) {
            return@withContext generateOfflineChaldeanGuidance(
                query = userMessage,
                currentHour = currentHour,
                dayEphemeris = dayEphemeris,
                locationName = locationName
            )
        }

        try {
            // Build Contextual Prompt Header
            val timeFmt = SimpleDateFormat("HH:mm, EEEE, MMMM d, yyyy", Locale.getDefault())
            val contextNote = buildString {
                appendLine("[ASTRONOMICAL & CELESTIAL CONTEXT]")
                appendLine("Current Local Time: ${timeFmt.format(Date())}")
                appendLine("Location: $locationName")
                if (dayEphemeris != null) {
                    appendLine("Day Ruler: ${dayEphemeris.dayRuler.displayName} (${dayEphemeris.dayRuler.glyph})")
                    appendLine("Moon Phase: ${dayEphemeris.moonPhase} (${(dayEphemeris.moonIlluminationFraction * 100).toInt()}% illuminated)")
                    val sunriseFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
                    appendLine("Sunrise: ${sunriseFmt.format(Date(dayEphemeris.sunriseEpochMs))}, Sunset: ${sunriseFmt.format(Date(dayEphemeris.sunsetEpochMs))}")
                }
                if (currentHour != null) {
                    appendLine("CURRENT ACTIVE PLANETARY HOUR: ${currentHour.planet.displayName} (${currentHour.planet.glyph})")
                    appendLine("Hour: ${if (currentHour.isDayHour) "Day Hour" else "Night Hour"} #${currentHour.hourNumberInPeriod} (${currentHour.formattedTimeRange()})")
                    appendLine("Active Energy: ${currentHour.planet.energyTheme}")
                    appendLine("Colors: ${currentHour.planet.colors.joinToString()}")
                    appendLine("Incense: ${currentHour.planet.incenses.joinToString()}")
                    appendLine("Stones: ${currentHour.planet.gemstones.joinToString()}")
                }
            }

            // Execute API request with tools
            executeGeminiRequest(
                apiKey = apiKey,
                modelName = modelName,
                history = history,
                contextNote = contextNote,
                userMessage = userMessage,
                withTools = enableSearchGrounding || enableMapsGrounding,
                enableSearch = enableSearchGrounding,
                enableMaps = enableMapsGrounding
            )
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API request failed", e)
            ChatResponseResult(
                text = "✨ *A momentary celestial fog surrounds the ether.* (${e.localizedMessage ?: "Network connection error"})\n\n" +
                        "Here is traditional Chaldean wisdom for your query:\n" +
                        generateOfflineFallbackText(userMessage, currentHour),
                groundingType = GroundingType.NONE
            )
        }
    }

    private fun executeGeminiRequest(
        apiKey: String,
        modelName: String,
        history: List<ChatMessage>,
        contextNote: String,
        userMessage: String,
        withTools: Boolean,
        enableSearch: Boolean,
        enableMaps: Boolean
    ): ChatResponseResult {
        val root = JSONObject()

        // System Instruction
        val systemInstructionObj = JSONObject()
        val partsArr = JSONArray()
        partsArr.put(JSONObject().put("text", SYSTEM_INSTRUCTION))
        systemInstructionObj.put("parts", partsArr)
        root.put("system_instruction", systemInstructionObj)

        // Contents (Multi-turn conversation history)
        val contentsArr = JSONArray()

        // Include recent history (up to last 10 turns)
        val recentHistory = history.takeLast(10)
        for (msg in recentHistory) {
            val role = if (msg.sender == MessageSender.USER) "user" else "model"
            val msgObj = JSONObject()
            msgObj.put("role", role)
            val pArr = JSONArray()
            pArr.put(JSONObject().put("text", msg.text))
            msgObj.put("parts", pArr)
            contentsArr.put(msgObj)
        }

        // Current User Message augmented with ephemeris context
        val currentMsgObj = JSONObject()
        currentMsgObj.put("role", "user")
        val currentPartsArr = JSONArray()
        currentPartsArr.put(JSONObject().put("text", "$contextNote\nUser Query: $userMessage"))
        currentMsgObj.put("parts", currentPartsArr)
        contentsArr.put(currentMsgObj)

        root.put("contents", contentsArr)

        // Grounding Tools
        if (withTools) {
            val toolsArr = JSONArray()
            val toolObj = JSONObject()
            if (enableSearch) {
                toolObj.put("googleSearch", JSONObject())
            }
            if (enableMaps) {
                toolObj.put("googleMaps", JSONObject())
            }
            toolsArr.put(toolObj)
            root.put("tools", toolsArr)
        }

        val requestBody = root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val url = "$BASE_URL/$modelName:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                Log.w(TAG, "Request with tools failed code=${response.code}: $responseBody")
                // If failed due to tool incompatibility or model, retry once without tools
                if (withTools && response.code in 400..499) {
                    return executeGeminiRequest(
                        apiKey = apiKey,
                        modelName = modelName,
                        history = history,
                        contextNote = contextNote,
                        userMessage = userMessage,
                        withTools = false,
                        enableSearch = false,
                        enableMaps = false
                    )
                }
                throw RuntimeException("Gemini API error ${response.code}: $responseBody")
            }

            return parseGeminiResponse(responseBody, enableSearch, enableMaps)
        }
    }

    private fun parseGeminiResponse(
        jsonString: String,
        searchEnabled: Boolean,
        mapsEnabled: Boolean
    ): ChatResponseResult {
        val root = JSONObject(jsonString)
        val candidates = root.optJSONArray("candidates") ?: return ChatResponseResult("The stars are silent.", GroundingType.NONE)
        if (candidates.length() == 0) return ChatResponseResult("The stars are silent.", GroundingType.NONE)

        val candidate = candidates.getJSONObject(0)
        val content = candidate.optJSONObject("content")
        val parts = content?.optJSONArray("parts")

        val textBuilder = StringBuilder()
        if (parts != null) {
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                val t = part.optString("text")
                if (t.isNotBlank()) {
                    textBuilder.append(t)
                }
            }
        }

        val fullText = textBuilder.toString().ifBlank { "Celestial guidance received in silence." }

        // Grounding Metadata
        val groundingMetadata = candidate.optJSONObject("groundingMetadata")
        val searchQueries = mutableListOf<String>()
        val sources = mutableListOf<GroundingSource>()
        var groundingType = GroundingType.NONE

        if (groundingMetadata != null) {
            val webQueries = groundingMetadata.optJSONArray("webSearchQueries")
            if (webQueries != null) {
                for (i in 0 until webQueries.length()) {
                    searchQueries.add(webQueries.getString(i))
                }
            }

            val chunks = groundingMetadata.optJSONArray("groundingChunks")
            if (chunks != null) {
                for (i in 0 until chunks.length()) {
                    val chunk = chunks.getJSONObject(i)
                    val web = chunk.optJSONObject("web")
                    if (web != null) {
                        val title = web.optString("title", "Source")
                        val uri = web.optString("uri")
                        if (uri.isNotBlank()) {
                            sources.add(GroundingSource(title, uri))
                        }
                    }
                }
            }

            groundingType = when {
                searchEnabled && mapsEnabled -> GroundingType.BOTH
                searchEnabled -> GroundingType.SEARCH
                mapsEnabled -> GroundingType.MAPS
                else -> GroundingType.NONE
            }
        }

        return ChatResponseResult(
            text = fullText,
            groundingType = groundingType,
            searchQueries = searchQueries,
            sources = sources
        )
    }

    private fun generateOfflineChaldeanGuidance(
        query: String,
        currentHour: PlanetaryHour?,
        dayEphemeris: SolarCalculator.DayEphemeris?,
        locationName: String
    ): ChatResponseResult {
        val matchedPlanet = com.example.model.Planet.findBestPlanetForIntention(query)
        val activePlanet = currentHour?.planet ?: com.example.model.Planet.SUN

        val response = buildString {
            appendLine("✨ **AI Planetary Magick Engine (Offline Mode)**")
            appendLine("*(To enable Gemini 3.8 Flash live conversational intelligence & Grounding, enter your API key in Settings or AI Studio Secrets panel)*\n")

            if (query.lowercase().contains("when") || query.lowercase().contains("best time")) {
                appendLine("🌟 **Auspicious Timing Analysis for Your Working:**")
                appendLine("Your activity resonates with the sphere of **${matchedPlanet.displayName} (${matchedPlanet.glyph})**.")
                appendLine("• **Energy Theme:** ${matchedPlanet.energyTheme}")
                appendLine("• **Auspicious Intentions:** ${matchedPlanet.primaryIntentions.joinToString(", ")}")
                appendLine("• **Ideal Offerings & Incense:** ${matchedPlanet.incenses.joinToString(", ")}")
                appendLine("• **Corresponding Gemstones:** ${matchedPlanet.gemstones.joinToString(", ")}")
                appendLine("• **Altar Color:** ${matchedPlanet.colors.joinToString(", ")}")

                // Check if current hour matches
                if (activePlanet == matchedPlanet) {
                    appendLine("\n⚡ **Immediate Alignment:** The CURRENT planetary hour is ruled by ${matchedPlanet.displayName}! This is the peak auspicious window right now.")
                } else if (dayEphemeris != null) {
                    val nextMatchingHour = dayEphemeris.allHours.firstOrNull { it.planet == matchedPlanet && it.startEpochMs > System.currentTimeMillis() }
                    if (nextMatchingHour != null) {
                        appendLine("\n⏳ **Next Auspicious Hour Today:**")
                        appendLine("• ${if (nextMatchingHour.isDayHour) "Day Hour" else "Night Hour"} #${nextMatchingHour.hourNumberInPeriod} from **${nextMatchingHour.formattedTimeRange()}**")
                    } else {
                        appendLine("\n📅 **Day Alignment:** The primary day ruled by ${matchedPlanet.displayName} is **${matchedPlanet.dayOfWeek}**.")
                    }
                }
            } else {
                appendLine("🏛️ **Current Sphere of Influence:**")
                appendLine("The heavens over **$locationName** are currently ruled by **${activePlanet.displayName} (${activePlanet.glyph})**.")
                appendLine("• **Vibe:** ${activePlanet.energyTheme}")
                appendLine("• **Element:** ${activePlanet.element} | Metal: ${activePlanet.metal}")
                appendLine("\n🌿 **Recommended Activities for this Hour:**")
                activePlanet.recommendedActivities.forEach { act ->
                    appendLine("• $act")
                }
            }
        }

        return ChatResponseResult(
            text = response,
            groundingType = GroundingType.NONE
        )
    }

    private fun generateOfflineFallbackText(query: String, currentHour: PlanetaryHour?): String {
        val target = com.example.model.Planet.findBestPlanetForIntention(query)
        val active = currentHour?.planet ?: com.example.model.Planet.SUN
        return "• Currently active ruler: **${active.displayName} (${active.glyph})**\n" +
                "• Target ruler for '${query.take(30)}': **${target.displayName} (${target.glyph})**\n" +
                "• Recommended incenses: ${target.incenses.joinToString()}\n" +
                "• Sacred color: ${target.colors.firstOrNull() ?: "Gold"}"
    }
}

data class ChatResponseResult(
    val text: String,
    val groundingType: GroundingType,
    val searchQueries: List<String> = emptyList(),
    val sources: List<GroundingSource> = emptyList()
)
