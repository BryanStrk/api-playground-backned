package com.bryan.apiplayground.apis.music;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/music")
@Tag(name = "Música", description = "Búsqueda en el catálogo de iTunes (sin API key)")
public class MusicController {

    private final MusicService musicService;

    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    @GetMapping("/search")
    @Operation(
            summary = "Buscar canciones, álbumes o artistas",
            description = "term: texto libre. entity: 'song' (canciones), 'album' (discos), 'musicArtist' (intérpretes). "
                    + "Cada entity rellena un subconjunto distinto de campos del Track (p.ej. los álbumes no traen trackName ni previewUrl)."
    )
    public MusicResponse search(
            @RequestParam String term,
            @RequestParam(defaultValue = "song") String entity,
            @RequestParam(defaultValue = "15") int limit
    ) {
        return musicService.search(term, entity, limit);
    }
}
