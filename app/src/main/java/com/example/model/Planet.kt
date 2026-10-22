package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.PlanetJupiterColor
import com.example.ui.theme.PlanetMarsColor
import com.example.ui.theme.PlanetMercuryColor
import com.example.ui.theme.PlanetMoonColor
import com.example.ui.theme.PlanetSaturnColor
import com.example.ui.theme.PlanetSunColor
import com.example.ui.theme.PlanetVenusColor

/**
 * The 7 Classical Planets in Chaldean Order:
 * Saturn, Jupiter, Mars, Sun, Venus, Mercury, Moon
 */
enum class Planet(
    val displayName: String,
    val glyph: String,
    val dayOfWeek: String, // Which day begins with this planet at sunrise
    val energyTheme: String,
    val element: String,
    val metal: String,
    val colors: List<String>,
    val incenses: List<String>,
    val gemstones: List<String>,
    val primaryIntentions: List<String>,
    val recommendedActivities: List<String>,
    val uiColor: Color
) {
    SATURN(
        displayName = "Saturn",
        glyph = "♄",
        dayOfWeek = "Saturday",
        energyTheme = "Discipline, Boundaries, Protection & Long-Term Planning",
        element = "Earth / Air",
        metal = "Lead",
        colors = listOf("Black", "Deep Indigo", "Dark Brown"),
        incenses = listOf("Myrrh", "Cypress", "Patchouli", "Cedar"),
        gemstones = listOf("Onyx", "Black Tourmaline", "Obsidian", "Hematite"),
        primaryIntentions = listOf("Protection", "Boundaries", "Discipline", "Banishing", "Karma", "Long-term Planning"),
        recommendedActivities = listOf(
            "Establish firm boundaries and say no to distractions",
            "Banishing, binding, and energetic cord-cutting rituals",
            "Review long-term investments, wills, and foundational plans",
            "Deep shadow work and karmic clearing",
            "Decluttering and structured organizing"
        ),
        uiColor = PlanetSaturnColor
    ),
    JUPITER(
        displayName = "Jupiter",
        glyph = "♃",
        dayOfWeek = "Thursday",
        energyTheme = "Abundance, Expansion, Luck, Wisdom & Generosity",
        element = "Air / Fire",
        metal = "Tin",
        colors = listOf("Royal Purple", "Sapphire Blue", "Gold"),
        incenses = listOf("Cedar", "Nutmeg", "Frankincense", "Cinnamon"),
        gemstones = listOf("Amethyst", "Lapis Lazuli", "Turquoise", "Yellow Sapphire"),
        primaryIntentions = listOf("Abundance", "Wealth", "Luck", "Expansion", "Legal Matters", "Philosophy"),
        recommendedActivities = listOf(
            "Prosperity spells and abundance manifestation rituals",
            "Signing favorable contracts and resolving legal issues",
            "Engaging with mentors, spiritual study, and philosophical reading",
            "Acts of charity, gratitude offerings, and blessing ceremonies",
            "Planning travel, foreign ventures, and big vision goals"
        ),
        uiColor = PlanetJupiterColor
    ),
    MARS(
        displayName = "Mars",
        glyph = "♂",
        dayOfWeek = "Tuesday",
        energyTheme = "Courage, Strength, Vitality, Breaking Obstacles & Passion",
        element = "Fire",
        metal = "Iron",
        colors = listOf("Crimson Red", "Scarlet", "Black"),
        incenses = listOf("Dragon's Blood", "Ginger", "Black Pepper", "Tobacco"),
        gemstones = listOf("Ruby", "Carnelian", "Red Jasper", "Bloodstone"),
        primaryIntentions = listOf("Courage", "Protection", "Overcoming Obstacles", "Physical Energy", "Passion", "Victory"),
        recommendedActivities = listOf(
            "High-energy workouts and martial physical conditioning",
            "Rituals for banishing fears and cutting through stagnation",
            "Assertive negotiations and pitching bold initiatives",
            "Protection wards and warrior energy invocations",
            "Confronting difficult tasks requiring decisive action"
        ),
        uiColor = PlanetMarsColor
    ),
    SUN(
        displayName = "Sun",
        glyph = "☉",
        dayOfWeek = "Sunday",
        energyTheme = "Success, Vitality, Ambition, Leadership & Illumination",
        element = "Fire",
        metal = "Gold",
        colors = listOf("Solar Gold", "Vibrant Yellow", "Orange"),
        incenses = listOf("Frankincense", "Bay Laurel", "Cinnamon", "Copal"),
        gemstones = listOf("Sunstone", "Citrine", "Amber", "Golden Topaz"),
        primaryIntentions = listOf("Success", "Vitality", "Ambition", "Leadership", "Confidence", "Wealth"),
        recommendedActivities = listOf(
            "Solar charging, sun gazing, and vitality revitalization",
            "Career breakthroughs, public speaking, and leadership actions",
            "Success, fame, and sovereign confidence rituals",
            "Illuminating truth and dispelling self-doubt",
            "Honoring divine light and solar deities"
        ),
        uiColor = PlanetSunColor
    ),
    VENUS(
        displayName = "Venus",
        glyph = "♀",
        dayOfWeek = "Friday",
        energyTheme = "Love, Beauty, Harmony, Romance, Art & Luxury",
        element = "Water / Earth",
        metal = "Copper",
        colors = listOf("Emerald Green", "Rose Pink", "Aqua"),
        incenses = listOf("Rose", "Sandalwood", "Jasmine", "Vanilla"),
        gemstones = listOf("Rose Quartz", "Emerald", "Rhodonite", "Jade"),
        primaryIntentions = listOf("Love", "Relationships", "Beauty", "Art", "Harmony", "Luxury", "Friendship"),
        recommendedActivities = listOf(
            "Love rituals, self-love affirmations, and heart chakra alignment",
            "Artistic creation, music composition, and aesthetic design",
            "Luxury self-care, herbal baths, and sensual dining",
            "Diplomacy, reconciliation, and deepening friendships",
            "Beauty spells and crafting attraction charms"
        ),
        uiColor = PlanetVenusColor
    ),
    MERCURY(
        displayName = "Mercury",
        glyph = "☿",
        dayOfWeek = "Wednesday",
        energyTheme = "Communication, Intellect, Writing, Travel & Trade",
        element = "Air",
        metal = "Quicksilver",
        colors = listOf("Amber Orange", "Bright Yellow", "Silver"),
        incenses = listOf("Lavender", "Peppermint", "Lemongrass", "Mastic"),
        gemstones = listOf("Agate", "Fluorite", "Opal", "Blue Lace Agate"),
        primaryIntentions = listOf("Communication", "Writing", "Travel", "Business Deals", "Studying", "Technology"),
        recommendedActivities = listOf(
            "Drafting important emails, book chapters, and presentations",
            "Learning complex subjects, coding, and technical research",
            "Speedy errands, organizing short trips, and trade negotiations",
            "Divination via Tarot, runes, and astrology charts",
            "Mental clarity spells and quick-wit enchantments"
        ),
        uiColor = PlanetMercuryColor
    ),
    MOON(
        displayName = "Moon",
        glyph = "☽",
        dayOfWeek = "Monday",
        energyTheme = "Intuition, Subconscious, Home, Healing & Astral Work",
        element = "Water",
        metal = "Silver",
        colors = listOf("Lunar Silver", "Pearl White", "Seafoam Blue"),
        incenses = listOf("Jasmine", "Mugwort", "White Sandalwood", "Eucalyptus"),
        gemstones = listOf("Moonstone", "Selenite", "Pearl", "Labradorite"),
        primaryIntentions = listOf("Intuition", "Subconscious", "Home", "Emotional Healing", "Astral Work", "Psychic Sight"),
        recommendedActivities = listOf(
            "Deep meditation, psychic scrying, and dream journaling",
            "Water scrying, lunar baths, and intuitive readings",
            "Home blessing, sacred space cleansing, and family nurturing",
            "Emotional healing rituals and releasing trapped feelings",
            "Astral projection and receptive shadow integration"
        ),
        uiColor = PlanetMoonColor
    );

    companion object {
        /**
         * Chaldean Order sequence: Saturn, Jupiter, Mars, Sun, Venus, Mercury, Moon
         */
        val chaldeanOrder = listOf(SATURN, JUPITER, MARS, SUN, VENUS, MERCURY, MOON)

        fun fromDayOfWeek(dayOfWeekName: String): Planet {
            return when (dayOfWeekName.uppercase()) {
                "SUNDAY" -> SUN
                "MONDAY" -> MOON
                "TUESDAY" -> MARS
                "WEDNESDAY" -> MERCURY
                "THURSDAY" -> JUPITER
                "FRIDAY" -> VENUS
                "SATURDAY" -> SATURN
                else -> SUN
            }
        }

        fun nextInChaldeanOrder(current: Planet): Planet {
            val idx = chaldeanOrder.indexOf(current)
            return chaldeanOrder[(idx + 1) % chaldeanOrder.size]
        }

        fun findBestPlanetForIntention(query: String): Planet {
            val lower = query.lowercase()
            return when {
                lower.contains("discipline") || lower.contains("saturn") || lower.contains("boundar") ||
                        lower.contains("banish") || lower.contains("bind") || lower.contains("karma") ||
                        lower.contains("long-term") || lower.contains("structure") -> SATURN
                lower.contains("love") || lower.contains("romance") || lower.contains("beauty") ||
                        lower.contains("friend") || lower.contains("art") || lower.contains("venus") ||
                        lower.contains("harmony") -> VENUS
                lower.contains("money") || lower.contains("wealth") || lower.contains("luck") ||
                        lower.contains("jupiter") || lower.contains("abundance") || lower.contains("expand") ||
                        lower.contains("legal") -> JUPITER
                lower.contains("career") || lower.contains("success") || lower.contains("vitality") ||
                        lower.contains("health") || lower.contains("sun") || lower.contains("ambition") ||
                        lower.contains("fame") || lower.contains("leader") -> SUN
                lower.contains("courage") || lower.contains("strength") || lower.contains("protect") ||
                        lower.contains("mars") || lower.contains("obstacle") || lower.contains("warrior") ||
                        lower.contains("passion") || lower.contains("fitness") -> MARS
                lower.contains("write") || lower.contains("study") || lower.contains("mercury") ||
                        lower.contains("communicate") || lower.contains("exam") || lower.contains("tech") ||
                        lower.contains("deal") || lower.contains("business") || lower.contains("travel") -> MERCURY
                lower.contains("intuit") || lower.contains("dream") || lower.contains("moon") ||
                        lower.contains("subconscious") || lower.contains("astral") || lower.contains("heal") ||
                        lower.contains("emotion") || lower.contains("psychic") || lower.contains("home") -> MOON
                else -> SUN
            }
        }
    }
}
