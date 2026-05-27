package com.bryan.apiplayground.apis.weather;

public record WeatherResponse(
        String locationName,
        double latitude,
        double longitude,
        String time,
        double temperature,
        String temperatureUnit,
        double windSpeed,
        String windSpeedUnit,
        Integer weatherCode
) {
}
