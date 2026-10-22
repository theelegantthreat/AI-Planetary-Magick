package com.example.model

import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

object SolarCalculator {

    data class DayEphemeris(
        val date: Date,
        val sunriseEpochMs: Long,
        val sunsetEpochMs: Long,
        val nextSunriseEpochMs: Long,
        val dayHours: List<PlanetaryHour>,
        val nightHours: List<PlanetaryHour>,
        val allHours: List<PlanetaryHour>,
        val dayRuler: Planet,
        val moonPhase: String,
        val moonIlluminationFraction: Float
    )

    /**
     * Calculates the full 24 planetary hours for a given calendar day and location.
     */
    fun calculateHoursForDate(
        targetDate: Date,
        location: LocationData
    ): DayEphemeris {
        val tz = TimeZone.getTimeZone(location.timeZoneId)
        val cal = Calendar.getInstance(tz).apply {
            time = targetDate
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)

        // Sunrise and Sunset for today
        val (sunriseTodayMs, sunsetTodayMs) = computeSunriseSunset(year, month, day, location.latitude, location.longitude, tz)

        // Sunrise for tomorrow
        val nextDayCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
        val nextYear = nextDayCal.get(Calendar.YEAR)
        val nextMonth = nextDayCal.get(Calendar.MONTH) + 1
        val nextDay = nextDayCal.get(Calendar.DAY_OF_MONTH)
        val (nextSunriseMs, _) = computeSunriseSunset(nextYear, nextMonth, nextDay, location.latitude, location.longitude, tz)

        // In ancient planetary hours, the day starts at sunrise.
        // The day of week determines the ruler of the first daylight hour.
        val dayOfWeekIndex = cal.get(Calendar.DAY_OF_WEEK)
        val dayRuler = when (dayOfWeekIndex) {
            Calendar.SUNDAY -> Planet.SUN
            Calendar.MONDAY -> Planet.MOON
            Calendar.TUESDAY -> Planet.MARS
            Calendar.WEDNESDAY -> Planet.MERCURY
            Calendar.THURSDAY -> Planet.JUPITER
            Calendar.FRIDAY -> Planet.VENUS
            Calendar.SATURDAY -> Planet.SATURN
            else -> Planet.SUN
        }

        // Calculate 12 Day Hours
        val dayDuration = (sunsetTodayMs - sunriseTodayMs).coerceAtLeast(1000L * 60 * 60)
        val dayHourLength = dayDuration / 12

        val dayHours = mutableListOf<PlanetaryHour>()
        var currentRuler = dayRuler

        for (i in 0 until 12) {
            val startMs = sunriseTodayMs + (i * dayHourLength)
            val endMs = if (i == 11) sunsetTodayMs else startMs + dayHourLength
            dayHours.add(
                PlanetaryHour(
                    hourNumberInCycle = i + 1,
                    hourNumberInPeriod = i + 1,
                    isDayHour = true,
                    planet = currentRuler,
                    startEpochMs = startMs,
                    endEpochMs = endMs
                )
            )
            currentRuler = Planet.nextInChaldeanOrder(currentRuler)
        }

        // Calculate 12 Night Hours
        val nightDuration = (nextSunriseMs - sunsetTodayMs).coerceAtLeast(1000L * 60 * 60)
        val nightHourLength = nightDuration / 12

        val nightHours = mutableListOf<PlanetaryHour>()
        for (i in 0 until 12) {
            val startMs = sunsetTodayMs + (i * nightHourLength)
            val endMs = if (i == 11) nextSunriseMs else startMs + nightHourLength
            nightHours.add(
                PlanetaryHour(
                    hourNumberInCycle = i + 13,
                    hourNumberInPeriod = i + 1,
                    isDayHour = false,
                    planet = currentRuler,
                    startEpochMs = startMs,
                    endEpochMs = endMs
                )
            )
            currentRuler = Planet.nextInChaldeanOrder(currentRuler)
        }

        val allHours = dayHours + nightHours
        val (phaseName, illumination) = computeMoonPhase(targetDate)

        return DayEphemeris(
            date = targetDate,
            sunriseEpochMs = sunriseTodayMs,
            sunsetEpochMs = sunsetTodayMs,
            nextSunriseEpochMs = nextSunriseMs,
            dayHours = dayHours,
            nightHours = nightHours,
            allHours = allHours,
            dayRuler = dayRuler,
            moonPhase = phaseName,
            moonIlluminationFraction = illumination
        )
    }

    /**
     * Standard NOAA Solar Calculation for Sunrise and Sunset (zenith = 90.833° to account for atmospheric refraction)
     */
    private fun computeSunriseSunset(
        year: Int,
        month: Int,
        day: Int,
        latitude: Double,
        longitude: Double,
        timeZone: TimeZone
    ): Pair<Long, Long> {
        val cal = Calendar.getInstance(timeZone).apply {
            set(year, month - 1, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val zenith = 90.833 // Official sunrise/sunset zenith

        val gamma = 2 * Math.PI / 365.0 * (dayOfYear - 1 + (12.0 - 12.0) / 24.0)
        val eqTime = 229.18 * (0.000075 + 0.001868 * cos(gamma) - 0.032077 * sin(gamma) -
                0.014615 * cos(2 * gamma) - 0.040849 * sin(2 * gamma))
        val decl = 0.006918 - 0.399912 * cos(gamma) + 0.070257 * sin(gamma) -
                0.006758 * cos(2 * gamma) + 0.000907 * sin(2 * gamma) -
                0.002697 * cos(3 * gamma) + 0.00148 * sin(3 * gamma)

        val latRad = Math.toRadians(latitude)
        val zenithRad = Math.toRadians(zenith)

        val cosHA = (cos(zenithRad) / (cos(latRad) * cos(decl))) - (tan(latRad) * tan(decl))

        // Check for polar extremes
        val haRad = when {
            cosHA >= 1.0 -> 0.0 // Polar night
            cosHA <= -1.0 -> Math.PI // Midnight sun
            else -> acos(cosHA)
        }
        val haDeg = Math.toDegrees(haRad)

        // Sunrise & Sunset in UTC minutes from midnight
        val sunriseUtcMins = 720.0 - 4.0 * (longitude + haDeg) - eqTime
        val sunsetUtcMins = 720.0 - 4.0 * (longitude - haDeg) - eqTime

        // Offset to local millis
        val midnightEpochMs = cal.timeInMillis

        // Raw UTC time offset for the day
        val tzOffsetMs = timeZone.getOffset(midnightEpochMs)
        val sunriseLocalMs = midnightEpochMs + (sunriseUtcMins * 60 * 1000).toLong() + tzOffsetMs
        val sunsetLocalMs = midnightEpochMs + (sunsetUtcMins * 60 * 1000).toLong() + tzOffsetMs

        return Pair(sunriseLocalMs, sunsetLocalMs)
    }

    /**
     * Approximate astronomical Moon Phase calculation using Conway / synodic cycle (29.53058867 days)
     */
    fun computeMoonPhase(date: Date): Pair<String, Float> {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { time = date }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)

        // Julian Day calculation
        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        val jd = day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045

        // Synodic reference (known new moon at JD 2451549.5 on Jan 6, 2000)
        val daysSinceNew = (jd - 2451549.5) % 29.53058867
        val phaseDays = if (daysSinceNew < 0) daysSinceNew + 29.53058867 else daysSinceNew

        val fraction = (phaseDays / 29.53058867).toFloat()
        // Illumination fraction ranges from 0.0 (New) to 1.0 (Full) and back to 0.0
        val illumination = (0.5 * (1 - cos(fraction * 2 * Math.PI))).toFloat()

        val phaseName = when {
            phaseDays < 1.84566 -> "New Moon"
            phaseDays < 5.53699 -> "Waxing Crescent"
            phaseDays < 9.22831 -> "First Quarter"
            phaseDays < 12.91963 -> "Waxing Gibbous"
            phaseDays < 16.61096 -> "Full Moon"
            phaseDays < 20.30228 -> "Waning Gibbous"
            phaseDays < 23.99361 -> "Last Quarter"
            phaseDays < 27.68493 -> "Waning Crescent"
            else -> "New Moon"
        }

        return Pair(phaseName, illumination)
    }

    fun findActiveHour(ephemeris: DayEphemeris, atEpochMs: Long = System.currentTimeMillis()): PlanetaryHour? {
        return ephemeris.allHours.firstOrNull { it.isCurrent(atEpochMs) }
            ?: ephemeris.allHours.firstOrNull()
    }
}
