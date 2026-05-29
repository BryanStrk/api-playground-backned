package com.bryan.apiplayground.apis.worldcup;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/worldcup")
@Tag(name = "Deportes", description = "Mundial de fútbol 2026 (openfootball, sin API key)")
public class WorldcupController {

    private final WorldcupService worldcupService;

    public WorldcupController(WorldcupService worldcupService) {
        this.worldcupService = worldcupService;
    }

    @GetMapping("/matches")
    @Operation(
            summary = "Partidos del Mundial 2026",
            description = "Lista los 104 partidos del torneo. Filtros opcionales: "
                    + "group (ej. 'Group A'), team (substring, case-insensitive, ej. 'mexico'), "
                    + "status ('upcoming' o 'played'). El campo status se deriva de la presencia "
                    + "del score: si el partido aún no se ha jugado score1/score2 y goals1/goals2 "
                    + "vienen a null. En rondas eliminatorias team1/team2 pueden ser códigos "
                    + "tipo '2A' o 'W101' — se pasan tal cual. Cacheado 5 minutos en memoria."
    )
    public List<WorldCupMatch> getMatches(
            @RequestParam(required = false) String group,
            @RequestParam(required = false) String team,
            @RequestParam(required = false) String status
    ) {
        return worldcupService.getMatches(group, team, status);
    }

    @GetMapping("/groups")
    @Operation(
            summary = "Clasificación de los grupos del Mundial 2026",
            description = "Devuelve la tabla de cada grupo (A–L) calculada a partir de los "
                    + "partidos ya jugados de fase de grupos: 3 puntos por victoria, 1 por "
                    + "empate, 0 por derrota. Orden: points, goalDiff, goalsFor, team. Antes "
                    + "del torneo todas las filas salen a 0 — comportamiento correcto. Los "
                    + "partidos eliminatorios se ignoran (no tienen 'group' en la fuente)."
    )
    public List<GroupStanding> getGroups() {
        return worldcupService.getGroups();
    }
}
