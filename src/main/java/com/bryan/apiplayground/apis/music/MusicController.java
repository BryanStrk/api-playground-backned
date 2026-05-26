package com.bryan.apiplayground.apis.music;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/music")
@Tag(name = "Música", description = "Búsqueda de canciones en el catálogo de iTunes (sin API key)")
public class MusicController {

    private final MusicService musicService;

    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    @GetMapping("/search")
    @Operation(
            summary = "Buscar canciones por término",
            description = "Devuelve canciones con artista, álbum, preview de 30s y portada (artworkUrl100)."
    )
    public MusicResponse search(
            @RequestParam String term,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return musicService.search(term, limit);
    }
}
