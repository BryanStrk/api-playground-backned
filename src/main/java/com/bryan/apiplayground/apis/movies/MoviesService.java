package com.bryan.apiplayground.apis.movies;

import com.bryan.apiplayground.common.exception.ApiKeyNotConfiguredException;
import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Service
public class MoviesService {

    private static final String POPULAR_URL =
            "https://api.themoviedb.org/3/movie/popular?api_key={apiKey}&language={lang}";
    private static final String IMAGE_BASE = "https://image.tmdb.org/t/p/w500";

    private final RestClient restClient;
    private final String apiKey;

    public MoviesService(RestClient restClient,
                         @Value("${tmdb.api-key:}") String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    public MoviesResponse getPopular(String lang) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ApiKeyNotConfiguredException(
                    "La API key de TMDB no está configurada. Añade TMDB_API_KEY a tu .env");
        }
        try {
            var raw = restClient.get()
                    .uri(POPULAR_URL, apiKey, lang)
                    .retrieve()
                    .body(TmdbPopularRaw.class);
            if (raw == null) {
                return new MoviesResponse(0, List.of(), 0, 0);
            }
            var movies = (raw.results() == null ? List.<MovieRaw>of() : raw.results()).stream()
                    .map(m -> new Movie(
                            m.id(),
                            m.title(),
                            m.originalTitle(),
                            m.overview(),
                            m.releaseDate(),
                            posterUrl(m.posterPath()),
                            posterUrl(m.backdropPath()),
                            m.voteAverage(),
                            m.voteCount()))
                    .toList();
            return new MoviesResponse(raw.page(), movies, raw.totalPages(), raw.totalResults());
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "TMDB returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "TMDB unreachable: " + e.getMessage(), 0, e);
        }
    }

    private static String posterUrl(String path) {
        return (path == null || path.isBlank()) ? null : IMAGE_BASE + path;
    }

    private record TmdbPopularRaw(
            int page,
            List<MovieRaw> results,
            @JsonProperty("total_pages") int totalPages,
            @JsonProperty("total_results") int totalResults
    ) {
    }

    private record MovieRaw(
            int id,
            String title,
            @JsonProperty("original_title") String originalTitle,
            String overview,
            @JsonProperty("release_date") String releaseDate,
            @JsonProperty("poster_path") String posterPath,
            @JsonProperty("backdrop_path") String backdropPath,
            @JsonProperty("vote_average") double voteAverage,
            @JsonProperty("vote_count") int voteCount
    ) {
    }
}
