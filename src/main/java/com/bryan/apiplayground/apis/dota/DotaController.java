package com.bryan.apiplayground.apis.dota;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dota")
@Tag(name = "Videojuegos", description = "Datos públicos de Dota 2 vía OpenDota (sin API key)")
public class DotaController {

    private final DotaService dotaService;

    public DotaController(DotaService dotaService) {
        this.dotaService = dotaService;
    }

    @GetMapping("/heroes")
    @Operation(
            summary = "Catálogo de héroes con sus stats",
            description = "Devuelve todos los héroes con id, nombre interno, nombre localizado, atributo "
                    + "principal, tipo de ataque, roles y URLs absolutas de imagen e icono "
                    + "(prefijadas con el CDN de Steam para que el front las embeba directo)."
    )
    public List<DotaHero> listHeroes() {
        return dotaService.listHeroes();
    }

    @GetMapping("/pro-matches")
    @Operation(
            summary = "Partidas profesionales recientes",
            description = "Lista las últimas partidas pro disponibles en OpenDota. limit: 1–100 (default 20)."
    )
    public List<ProMatch> listProMatches(@RequestParam(defaultValue = "20") int limit) {
        return dotaService.listProMatches(limit);
    }
}
