package com.bryan.apiplayground.apis.weather;

public record WeatherResponse(
        double latitude,
        double longitude,
        String time,
        double temperature,
        String temperatureUnit,
        double windSpeed,
        String windSpeedUnit
) {
}
