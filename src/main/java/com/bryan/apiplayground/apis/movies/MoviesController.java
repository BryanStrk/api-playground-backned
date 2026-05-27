package com.bryan.apiplayground.apis.movies;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movies")
@Tag(name = "Cine", description = "Películas vía TMDB (requiere TMDB_API_KEY)")
public class MoviesController {

    private final MoviesService moviesService;

    public MoviesController(MoviesService moviesService) {
        this.moviesService = moviesService;
    }

    @GetMapping
    @Operation(
            summary = "Buscar o descubrir películas",
            description = "Si llega ?query=, busca en /search/movie. Si no, hace /discover/movie con "
                    + "filtros opcionales ?genre= y ?year=, ordenado por popularidad. Cualquiera de "
                    + "los dos modos devuelve hasta 12 resultados con el shape de /movies/popular."
    )
    public MoviesResponse searchOrDiscover(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Integer genre,
            @RequestParam(required = false) Integer year,
            @RequestParam(name = "lang", defaultValue = "es-ES") String lang
    ) {
        if (query != null && !query.isBlank()) {
            return moviesService.search(query, lang);
        }
        return moviesService.discover(genre, year, lang);
    }

    @GetMapping("/popular")
    @Operation(
            summary = "Películas populares",
            description = "Devuelve la primera página de películas populares en el idioma indicado. Los pósters se sirven ya con el host de imágenes de TMDB."
    )
    public MoviesResponse getPopular(@RequestParam(name = "lang", defaultValue = "es-ES") String lang) {
        return moviesService.getPopular(lang);
    }

    @GetMapping("/genres")
    @Operation(
            summary = "Catálogo de géneros",
            description = "Lista los géneros de TMDB con su id, para poblar el desplegable que alimenta el filtro de discover."
    )
    public List<Genre> getGenres(@RequestParam(name = "lang", defaultValue = "es-ES") String lang) {
        return moviesService.getGenres(lang);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Detalle de una película por id",
            description = "Devuelve ficha ampliada: tagline, runtime, status, homepage, idioma original y la lista de géneros con su id."
    )
    public MovieDetails getDetails(
            @PathVariable int id,
            @RequestParam(name = "lang", defaultValue = "es-ES") String lang
    ) {
        return moviesService.getDetails(id, lang);
    }

    @GetMapping("/{id}/trailer")
    @Operation(
            summary = "Primer tráiler de YouTube",
            description = "Busca en /movie/{id}/videos y devuelve la primera entrada con site=YouTube y type=Trailer. Si la película no tiene tráiler, los campos vienen null (no es un error)."
    )
    public TrailerResponse getTrailer(
            @PathVariable int id,
            @RequestParam(name = "lang", defaultValue = "es-ES") String lang
    ) {
        return moviesService.getTrailer(id, lang);
    }
}
