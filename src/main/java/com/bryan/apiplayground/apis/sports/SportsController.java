package com.bryan.apiplayground.apis.sports;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sports")
@Tag(name = "Deportes", description = "Clasificaciones, partidos y equipos vía Football-Data.org (requiere FOOTBALL_DATA_KEY)")
public class SportsController {

    private final SportsService sportsService;

    public SportsController(SportsService sportsService) {
        this.sportsService = sportsService;
    }

    @GetMapping("/standings")
    @Operation(
            summary = "Clasificación de una competición",
            description = "Devuelve la tabla TOTAL del torneo indicado. competition es el código de "
                    + "Football-Data (PD = La Liga por defecto, PL = Premier League, CL = Champions, ...)."
    )
    public StandingsResponse getStandings(
            @RequestParam(defaultValue = "PD") String competition
    ) {
        return sportsService.getStandings(competition);
    }

    @GetMapping("/matches")
    @Operation(
            summary = "Partidos próximos (o últimos jugados)",
            description = "Devuelve hasta 15 partidos. Modo UPCOMING si hay SCHEDULED/TIMED ordenados por "
                    + "fecha ascendente; si no quedan próximos, modo RECENT con los FINISHED más recientes. "
                    + "homeScore/awayScore vienen null para partidos no jugados; estadio no se expone porque "
                    + "Football-Data no lo incluye en el free tier."
    )
    public MatchesResponse getMatches(
            @RequestParam(defaultValue = "PD") String competition
    ) {
        return sportsService.getMatches(competition);
    }

    @GetMapping("/teams")
    @Operation(
            summary = "Equipos de una competición",
            description = "Lista los clubes que disputan la temporada actual con name, crest, venue "
                    + "(estadio si Football-Data lo expone) y founded (año de fundación)."
    )
    public TeamsResponse getTeams(
            @RequestParam(defaultValue = "PD") String competition
    ) {
        return sportsService.getTeams(competition);
    }

    @GetMapping("/competitions")
    @Operation(
            summary = "Competiciones gratuitas",
            description = "Devuelve los 12 códigos de competición disponibles en el plan gratuito de "
                    + "Football-Data (Premier League, La Liga, Bundesliga, Champions, Mundial, etc.) "
                    + "para poblar el selector del dashboard. No requiere key."
    )
    public List<CompetitionInfo> listCompetitions() {
        return sportsService.listCompetitions();
    }
}
