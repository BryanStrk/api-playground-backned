package com.bryan.apiplayground.apis.movies;

import com.bryan.apiplayground.common.exception.ApiKeyNotConfiguredException;
import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
public class MoviesService {

    private static final String POPULAR_URL =
            "https://api.themoviedb.org/3/movie/popular?api_key={apiKey}&language={lang}";
    private static final String GENRES_URL =
            "https://api.themoviedb.org/3/genre/movie/list?api_key={apiKey}&language={lang}";
    private static final String DETAILS_URL =
            "https://api.themoviedb.org/3/movie/{id}?api_key={apiKey}&language={lang}";
    private static final String VIDEOS_URL =
            "https://api.themoviedb.org/3/movie/{id}/videos?api_key={apiKey}&language={lang}";
    private static final String SEARCH_BASE = "https://api.themoviedb.org/3/search/movie";
    private static final String DISCOVER_BASE = "https://api.themoviedb.org/3/discover/movie";
    private static final String IMAGE_BASE = "https://image.tmdb.org/t/p/w500";
    // TMDB returns 20 results per page; the dashboard grid only renders 12.
    private static final int RESULT_LIMIT = 12;

    private final RestClient restClient;
    private final String apiKey;

    public MoviesService(RestClient restClient,
                         @Value("${tmdb.api-key:}") String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    public MoviesResponse getPopular(String lang) {
        requireKey();
        return callList(POPULAR_URL, apiKey, lang);
    }

    public MoviesResponse search(String query, String lang) {
        requireKey();
        var uri = UriComponentsBuilder.fromUriString(SEARCH_BASE)
                .queryParam("api_key", apiKey)
                .queryParam("language", lang)
                .queryParam("query", query)
                .build(false)
                .toUriString();
        return callList(uri);
    }

    public MoviesResponse discover(Integer genreId, Integer year, String lang) {
        requireKey();
        var builder = UriComponentsBuilder.fromUriString(DISCOVER_BASE)
                .queryParam("api_key", apiKey)
                .queryParam("language", lang)
                .queryParam("sort_by", "popularity.desc");
        if (genreId != null) builder.queryParam("with_genres", genreId);
        if (year != null) builder.queryParam("primary_release_year", year);
        return callList(builder.build(false).toUriString());
    }

    public List<Genre> getGenres(String lang) {
        requireKey();
        try {
            var raw = restClient.get()
                    .uri(GENRES_URL, apiKey, lang)
                    .retrieve()
                    .body(GenresRaw.class);
            return raw == null || raw.genres() == null ? List.of() : raw.genres();
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "TMDB returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "TMDB unreachable: " + e.getMessage(), 0, e);
        }
    }

    public MovieDetails getDetails(int id, String lang) {
        requireKey();
        try {
            var raw = restClient.get()
                    .uri(DETAILS_URL, id, apiKey, lang)
                    .retrieve()
                    .body(MovieDetailsRaw.class);
            if (raw == null) {
                throw new ExternalApiException("TMDB returned an empty payload", 502);
            }
            return new MovieDetails(
                    raw.id(),
                    raw.title(),
                    raw.originalTitle(),
                    raw.overview(),
                    raw.tagline(),
                    raw.releaseDate(),
                    posterUrl(raw.posterPath()),
                    posterUrl(raw.backdropPath()),
                    raw.voteAverage(),
                    raw.voteCount(),
                    raw.runtime(),
                    raw.status(),
                    raw.homepage(),
                    raw.originalLanguage(),
                    raw.genres() == null ? List.of() : raw.genres()
            );
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "TMDB returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "TMDB unreachable: " + e.getMessage(), 0, e);
        }
    }

    public TrailerResponse getTrailer(int id, String lang) {
        requireKey();
        try {
            var raw = restClient.get()
                    .uri(VIDEOS_URL, id, apiKey, lang)
                    .retrieve()
                    .body(VideosRaw.class);
            // No trailer is a legitimate state (not every movie has one), so
            // return an empty-but-shaped response instead of erroring out.
            if (raw == null || raw.results() == null) {
                return new TrailerResponse(null, null, null);
            }
            return raw.results().stream()
                    .filter(v -> "YouTube".equalsIgnoreCase(v.site())
                            && "Trailer".equalsIgnoreCase(v.type()))
                    .findFirst()
                    .map(v -> new TrailerResponse(
                            v.key(),
                            "https://www.youtube.com/watch?v=" + v.key(),
                            v.name()))
                    .orElse(new TrailerResponse(null, null, null));
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "TMDB returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "TMDB unreachable: " + e.getMessage(), 0, e);
        }
    }

    private MoviesResponse callList(String template, Object... uriArgs) {
        try {
            var raw = uriArgs.length == 0
                    ? restClient.get().uri(template).retrieve().body(TmdbListRaw.class)
                    : restClient.get().uri(template, uriArgs).retrieve().body(TmdbListRaw.class);
            if (raw == null) {
                return new MoviesResponse(0, List.of(), 0, 0);
            }
            var movies = (raw.results() == null ? List.<MovieRaw>of() : raw.results()).stream()
                    .limit(RESULT_LIMIT)
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

    private void requireKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ApiKeyNotConfiguredException(
                    "La API key de TMDB no está configurada. Añade TMDB_API_KEY a tu .env");
        }
    }

    private static String posterUrl(String path) {
        return (path == null || path.isBlank()) ? null : IMAGE_BASE + path;
    }

    private record TmdbListRaw(
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

    private record GenresRaw(List<Genre> genres) {
    }

    private record MovieDetailsRaw(
            int id,
            String title,
            @JsonProperty("original_title") String originalTitle,
            String overview,
            String tagline,
            @JsonProperty("release_date") String releaseDate,
            @JsonProperty("poster_path") String posterPath,
            @JsonProperty("backdrop_path") String backdropPath,
            @JsonProperty("vote_average") double voteAverage,
            @JsonProperty("vote_count") int voteCount,
            Integer runtime,
            String status,
            String homepage,
            @JsonProperty("original_language") String originalLanguage,
            List<Genre> genres
    ) {
    }

    private record VideosRaw(List<VideoRaw> results) {
    }

    private record VideoRaw(String key, String name, String site, String type) {
    }
}
