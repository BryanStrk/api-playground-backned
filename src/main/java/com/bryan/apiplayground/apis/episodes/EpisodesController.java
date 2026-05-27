package com.bryan.apiplayground.apis.episodes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/episodes")
@Tag(name = "Entretenimiento", description = "Episodios de Rick and Morty (sin API key)")
public class EpisodesController {

    private final EpisodesService episodesService;

    public EpisodesController(EpisodesService episodesService) {
        this.episodesService = episodesService;
    }

    @GetMapping("/search")
    @Operation(
            summary = "Buscar episodios por nombre",
            description = "Devuelve la lista de episodios cuyo título contiene el término. characterIds extrae el id "
                    + "numérico de cada URL del array characters del upstream, listo para encadenar con /characters/{id}."
    )
    public List<Episode> search(@RequestParam String q) {
        return episodesService.search(q);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Episodio por id",
            description = "Devuelve el episodio con su código (S01E01), fecha de emisión y la lista de ids de personajes que aparecen."
    )
    public Episode getById(@PathVariable int id) {
        return episodesService.getById(id);
    }
}
