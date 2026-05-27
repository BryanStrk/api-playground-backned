package com.bryan.apiplayground.apis.holidays;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Year;
import java.util.List;

@RestController
@RequestMapping("/api/v1/holidays")
@Tag(name = "Cultura", description = "Festivos públicos vía Nager.Date (sin API key)")
public class HolidaysController {

    private final HolidaysService holidaysService;

    public HolidaysController(HolidaysService holidaysService) {
        this.holidaysService = holidaysService;
    }

    @GetMapping
    @Operation(
            summary = "Festivos por país y año",
            description = "country en ISO 3166-1 alfa-2 (ES, US, FR…). year en YYYY (por defecto el actual)."
    )
    public List<Holiday> list(
            @RequestParam(defaultValue = "ES") String country,
            @RequestParam(required = false) Integer year
    ) {
        var effectiveYear = year == null ? Year.now().getValue() : year;
        return holidaysService.list(effectiveYear, country);
    }

    @GetMapping("/countries")
    @Operation(
            summary = "Países disponibles",
            description = "Catálogo de países que Nager.Date soporta (~110), con su código ISO y nombre."
    )
    public List<Country> listCountries() {
        return holidaysService.listCountries();
    }
}
