package com.bryan.apiplayground.apis.weather;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/weather")
@Tag(name = "Clima", description = "Pronóstico actual vía Open-Meteo (sin API key)")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    @Operation(
            summary = "Tiempo actual por ciudad o por coordenadas",
            description = "Acepta o bien ?city= (geocodificado vía Open-Meteo) o bien ?lat=&lon=. "
                    + "Si llegan ambos, gana city. Cuando se busca por ciudad, locationName trae "
                    + "el nombre completo (ej. 'Tokyo, Japan')."
    )
    public WeatherResponse getCurrent(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DecimalMin("-90.0") @DecimalMax("90.0") Double lat,
            @RequestParam(required = false) @DecimalMin("-180.0") @DecimalMax("180.0") Double lon
    ) {
        if (city != null && !city.isBlank()) {
            return weatherService.getByCity(city);
        }
        if (lat == null || lon == null) {
            throw new IllegalArgumentException(
                    "Provide either ?city= or both ?lat= and ?lon=");
        }
        return weatherService.getByCoordinates(lat, lon);
    }
}
