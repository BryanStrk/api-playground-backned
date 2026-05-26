package com.bryan.apiplayground.apis.weather;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class WeatherService {

    private static final String FORECAST_URL =
            "https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}"
                    + "&current=temperature_2m,wind_speed_10m";

    private final RestClient restClient;

    public WeatherService(RestClient restClient) {
        this.restClient = restClient;
    }

    public WeatherResponse getCurrent(double lat, double lon) {
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
                    raw.latitude(),
                    raw.longitude(),
                    raw.current().time(),
                    raw.current().temperature(),
                    units == null ? null : units.temperature(),
                    raw.current().windSpeed(),
                    units == null ? null : units.windSpeed()
            );
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "Open-Meteo returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "Open-Meteo unreachable: " + e.getMessage(), 0, e);
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
            @JsonProperty("wind_speed_10m") double windSpeed
    ) {
    }

    private record Units(
            @JsonProperty("temperature_2m") String temperature,
            @JsonProperty("wind_speed_10m") String windSpeed
    ) {
    }
}
