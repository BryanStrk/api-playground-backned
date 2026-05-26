package com.bryan.apiplayground.apis.sports;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sports")
@Tag(name = "Deportes", description = "Equipos deportivos vía TheSportsDB (SPORTSDB_KEY opcional, '123' por defecto)")
public class SportsController {

    private final SportsService sportsService;

    public SportsController(SportsService sportsService) {
        this.sportsService = sportsService;
    }

    @GetMapping("/team")
    @Operation(
            summary = "Buscar equipos por nombre",
            description = "Devuelve la lista de equipos que coinciden con el nombre, con deporte, liga, país, estadio, descripción y URLs de escudo/logo."
    )
    public TeamsResponse searchTeams(@RequestParam String name) {
        return sportsService.searchTeams(name);
    }
}
