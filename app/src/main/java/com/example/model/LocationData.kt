package com.example.model

data class LocationData(
    val cityName: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String
) {
    companion object {
        val DEFAULT_PRESETS = listOf(
            LocationData("New York", "USA", 40.7128, -74.0060, "America/New_York"),
            LocationData("London", "UK", 51.5074, -0.1278, "Europe/London"),
            LocationData("Paris", "France", 48.8566, 2.3522, "Europe/Paris"),
            LocationData("Rome", "Italy", 41.9028, 12.4964, "Europe/Rome"),
            LocationData("Athens", "Greece", 37.9838, 23.7275, "Europe/Athens"),
            LocationData("Cairo", "Egypt", 30.0444, 31.2357, "Africa/Cairo"),
            LocationData("Tokyo", "Japan", 35.6762, 139.6503, "Asia/Tokyo"),
            LocationData("Sydney", "Australia", -33.8688, 151.2093, "Australia/Sydney"),
            LocationData("San Francisco", "USA", 37.7749, -122.4194, "America/Los_Angeles"),
            LocationData("Los Angeles", "USA", 34.0522, -118.2437, "America/Los_Angeles"),
            LocationData("Rio de Janeiro", "Brazil", -22.9068, -43.1729, "America/Sao_Paulo")
        )

        val DEFAULT = DEFAULT_PRESETS[0]
    }
}
