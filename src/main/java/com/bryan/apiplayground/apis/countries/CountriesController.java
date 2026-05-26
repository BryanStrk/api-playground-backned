package com.bryan.apiplayground.apis.countries;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/countries")
@Tag(name = "Geografía", description = "Información de países vía REST Countries (sin API key)")
public class CountriesController {

    private final CountriesService countriesService;

    public CountriesController(CountriesService countriesService) {
        this.countriesService = countriesService;
    }

    @GetMapping("/{name}")
    @Operation(
            summary = "Datos de un país por nombre",
            description = "Devuelve nombre común y oficial, capital, región, población, área, idiomas, bandera y enlace a Google Maps."
    )
    public CountryResponse getByName(@PathVariable String name) {
        return countriesService.getByName(name);
    }
}
