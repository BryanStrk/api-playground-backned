package com.bryan.apiplayground.apis.weather;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Service
public class WeatherService {

    // Open-Meteo only returns the variables listed in current=...; ask for
    // weather_code too so the frontend can render the corresponding icon.
    private static final String FORECAST_URL =
            "https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}"
                    + "&current=temperature_2m,wind_speed_10m,weather_code";
    private static final String GEOCODE_URL =
            "https://geocoding-api.open-meteo.com/v1/search?name={name}&count=1";

    private final RestClient restClient;

    public WeatherService(RestClient restClient) {
        this.restClient = restClient;
    }

    public WeatherResponse getByCity(String city) {
        var geo = geocode(city);
        if (geo == null) {
            throw new ExternalApiException(
                    "Open-Meteo geocoding found no match for '" + city + "'", 502);
        }
        var locationName = geo.country() == null || geo.country().isBlank()
                ? geo.name()
                : geo.name() + ", " + geo.country();
        return getCurrent(geo.latitude(), geo.longitude(), locationName);
    }

    public WeatherResponse getByCoordinates(double lat, double lon) {
        return getCurrent(lat, lon, null);
    }

    private WeatherResponse getCurrent(double lat, double lon, String locationName) {
        try {
            var raw = restClient.get()
                    .uri(FORECAST_URL, lat, lon)
                    .retrieve()
                    .body(OpenMeteoRaw.class);
            if (raw == null || raw.current() == null) {
                throw new ExternalApiException("Open-Meteo returned an empty payload", 502);
            }
            var units = raw.currentUnits();
            return new WeatherResponse(
                    locationName,
                    raw.latitude(),
                    raw.longitude(),
                    raw.current().time(),
                    raw.current().temperature(),
                    units == null ? null : units.temperature(),
                    raw.current().windSpeed(),
                    units == null ? null : units.windSpeed(),
                    raw.current().weatherCode()
            );
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "Open-Meteo returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "Open-Meteo unreachable: " + e.getMessage(), 0, e);
        }
    }

    private GeocodeResult geocode(String city) {
        try {
            var raw = restClient.get()
                    .uri(GEOCODE_URL, city)
                    .retrieve()
                    .body(GeocodeRaw.class);
            // The geocoding API returns {"generationtime_ms": ...} with no "results"
            // key when nothing matches, so null/empty both mean "no match".
            if (raw == null || raw.results() == null || raw.results().isEmpty()) {
                return null;
            }
            return raw.results().getFirst();
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "Open-Meteo geocoding returned " + e.getStatusCode(),
                    e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "Open-Meteo geocoding unreachable: " + e.getMessage(), 0, e);
        }
    }

    private record OpenMeteoRaw(
            double latitude,
            double longitude,
            Current current,
            @JsonProperty("current_units") Units currentUnits
    ) {
    }

    private record Current(
            String time,
            @JsonProperty("temperature_2m") double temperature,
            @JsonProperty("wind_speed_10m") double windSpeed,
            @JsonProperty("weather_code") Integer weatherCode
    ) {
    }

    private record Units(
            @JsonProperty("temperature_2m") String temperature,
            @JsonProperty("wind_speed_10m") String windSpeed
    ) {
    }

    private record GeocodeRaw(List<GeocodeResult> results) {
    }

    private record GeocodeResult(
            String name,
            String country,
            double latitude,
            double longitude
    ) {
    }
}
