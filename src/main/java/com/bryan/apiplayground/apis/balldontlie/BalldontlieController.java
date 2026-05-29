package com.bryan.apiplayground.apis.balldontlie;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/balldontlie")
@Tag(name = "Deportes", description = "Mundial live (BALLDONTLIE FIFA) — requiere BALLDONTLIE_API_KEY")
public class BalldontlieController {

    private final BalldontlieService balldontlieService;

    public BalldontlieController(BalldontlieService balldontlieService) {
        this.balldontlieService = balldontlieService;
    }

    @GetMapping("/teams")
    @Operation(
            summary = "Selecciones del Mundial [FREE]",
            description = "Devuelve las selecciones nacionales que participan en el torneo. "
                    + "Disponible en el tier FREE de BALLDONTLIE — funciona con cualquier key válida."
    )
    public List<BdlTeam> getTeams() {
        return balldontlieService.getTeams();
    }

    @GetMapping("/stadiums")
    @Operation(
            summary = "Estadios del Mundial [FREE]",
            description = "Sedes oficiales del Mundial con ciudad, país, capacidad y coordenadas. "
                    + "Disponible en el tier FREE de BALLDONTLIE."
    )
    public List<BdlStadium> getStadiums() {
        return balldontlieService.getStadiums();
    }

    @GetMapping("/standings")
    @Operation(
            summary = "Clasificación del Mundial [ALL-STAR]",
            description = "Tabla de cada grupo con posición, puntos y diferencia de goles. "
                    + "Requiere plan ALL-STAR de BALLDONTLIE. Si tu key es FREE el upstream "
                    + "responde 401 y este endpoint devuelve 402 con requiredPlan='ALL-STAR'."
    )
    public List<BdlStanding> getStandings() {
        return balldontlieService.getStandings();
    }

    @GetMapping("/matches")
    @Operation(
            summary = "Partidos live del Mundial [GOAT]",
            description = "Calendario y resultados con paginación cursor-based (devolvemos la "
                    + "primera página, per_page=100). Filtrable por season (default 2026). "
                    + "Requiere plan GOAT. En eliminatorias homeTeam/awayTeam pueden ser null "
                    + "hasta que se conocen los clasificados. Si tu key es FREE el upstream "
                    + "responde 401 y este endpoint devuelve 402 con requiredPlan='GOAT'."
    )
    public List<BdlMatch> getMatches(
            @RequestParam(required = false) Integer season
    ) {
        return balldontlieService.getMatches(season);
    }

    @GetMapping("/players")
    @Operation(
            summary = "Jugadores del Mundial [GOAT]",
            description = "Búsqueda paginada cursor-based (primera página, per_page=100). "
                    + "Filtra por nombre con el param 'search'. Requiere plan GOAT. Si tu key "
                    + "es FREE el upstream responde 401 y este endpoint devuelve 402 con "
                    + "requiredPlan='GOAT'."
    )
    public List<BdlPlayer> getPlayers(
            @RequestParam(required = false) String search
    ) {
        return balldontlieService.getPlayers(search);
    }

    @GetMapping("/tiers")
    @Operation(
            summary = "Tabla de tiers de BALLDONTLIE [FREE]",
            description = "Metadata estática: qué endpoints cubre cada plan (FREE, ALL-STAR, "
                    + "GOAT), su precio mensual en USD y el rate limit por minuto. El frontend "
                    + "la usa para pintar los candados sobre las cards de los endpoints "
                    + "premium sin hardcodear la lista. No hace llamada al upstream."
    )
    public List<BdlTier> getTiers() {
        return balldontlieService.getTiers();
    }
}
