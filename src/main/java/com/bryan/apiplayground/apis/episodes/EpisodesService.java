package com.bryan.apiplayground.apis.episodes;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class EpisodesService {

    private static final String SEARCH_URL = "https://rickandmortyapi.com/api/episode/?name={name}";
    private static final String BY_ID_URL = "https://rickandmortyapi.com/api/episode/{id}";
    // R&M character URLs always end in /character/{id} — capture the trailing
    // integer so the dashboard can drill into /characters/{id} directly.
    private static final Pattern CHARACTER_ID_PATTERN = Pattern.compile(".*/character/(\\d+)$");

    private final RestClient restClient;

    public EpisodesService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<Episode> search(String query) {
        try {
            var raw = restClient.get()
                    .uri(SEARCH_URL, query)
                    .retrieve()
                    .body(SearchRaw.class);
            if (raw == null || raw.results() == null) return List.of();
            return raw.results().stream().map(EpisodesService::toEpisode).toList();
        } catch (RestClientResponseException e) {
            // R&M answers 404 with {"error": "There is nothing here"} when the
            // query matches no episodes — return an empty list, not a 502.
            if (e.getStatusCode().value() == 404) return List.of();
            throw new ExternalApiException(
                    "Rick and Morty API returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "Rick and Morty API unreachable: " + e.getMessage(), 0, e);
        }
    }

    public Episode getById(int id) {
        try {
            var raw = restClient.get()
                    .uri(BY_ID_URL, id)
                    .retrieve()
                    .body(EpisodeRaw.class);
            if (raw == null) {
                throw new ExternalApiException("Rick and Morty API returned an empty payload", 502);
            }
            return toEpisode(raw);
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "Rick and Morty API returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "Rick and Morty API unreachable: " + e.getMessage(), 0, e);
        }
    }

    private static Episode toEpisode(EpisodeRaw raw) {
        var ids = raw.characters() == null
                ? List.<Integer>of()
                : raw.characters().stream()
                        .map(EpisodesService::extractCharacterId)
                        .filter(i -> i != null)
                        .toList();
        return new Episode(raw.id(), raw.name(), raw.episode(), raw.airDate(), ids);
    }

    private static Integer extractCharacterId(String url) {
        if (url == null) return null;
        var m = CHARACTER_ID_PATTERN.matcher(url);
        return m.matches() ? Integer.parseInt(m.group(1)) : null;
    }

    private record SearchRaw(List<EpisodeRaw> results) {
    }

    private record EpisodeRaw(
            int id,
            String name,
            @JsonProperty("air_date") String airDate,
            String episode,
            List<String> characters
    ) {
    }
}
