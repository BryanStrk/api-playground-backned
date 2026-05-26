package com.bryan.apiplayground.apis.movies;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/movies")
@Tag(name = "Cine", description = "Películas populares vía TMDB (requiere TMDB_API_KEY)")
public class MoviesController {

    private final MoviesService moviesService;

    public MoviesController(MoviesService moviesService) {
        this.moviesService = moviesService;
    }

    @GetMapping("/popular")
    @Operation(
            summary = "Películas populares",
            description = "Devuelve la primera página de películas populares en el idioma indicado. Los pósters se sirven ya con el host de imágenes de TMDB."
    )
    public MoviesResponse getPopular(@RequestParam(name = "lang", defaultValue = "es-ES") String lang) {
        return moviesService.getPopular(lang);
    }
}
