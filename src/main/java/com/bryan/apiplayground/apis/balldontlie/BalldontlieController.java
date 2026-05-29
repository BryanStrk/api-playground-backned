package com.bryan.apiplayground.apis.balldontlie;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
