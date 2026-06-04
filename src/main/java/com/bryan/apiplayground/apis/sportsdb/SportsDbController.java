package com.bryan.apiplayground.apis.sportsdb;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sportsdb")
@Tag(name = "Deportes", description = "Búsqueda de equipos vía TheSportsDB (key de test pública por defecto)")
public class SportsDbController {

    private final SportsDbService sportsDbService;

    public SportsDbController(SportsDbService sportsDbService) {
        this.sportsDbService = sportsDbService;
    }

    @GetMapping("/team")
    @Operation(
            summary = "Buscar equipo por nombre",
            description = "Busca equipos por nombre y devuelve id, nombre, liga, estadio, escudo y "
                    + "descripción. Si no hay coincidencias, devuelve una lista vacía."
    )
    public SportsDbResponse searchTeam(@RequestParam String name) {
        return sportsDbService.searchTeam(name);
    }
}
