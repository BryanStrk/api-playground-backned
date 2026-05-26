package com.bryan.apiplayground.apis.music;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class MusicService {

    private static final String SEARCH_URL =
            "https://itunes.apple.com/search?term={term}&entity=song&limit={limit}";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public MusicService(RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public MusicResponse search(String term, int limit) {
        try {
            // iTunes responds with Content-Type: text/javascript even though the body is JSON.
            // Spring's JSON converter is bound to application/json, so we fetch as String and
            // deserialize manually.
            var json = restClient.get()
                    .uri(SEARCH_URL, term, limit)
                    .retrieve()
                    .body(String.class);
            if (json == null || json.isBlank()) {
                return new MusicResponse(0, List.of());
            }
            var raw = objectMapper.readValue(json, ItunesRaw.class);
            if (raw == null) {
                return new MusicResponse(0, List.of());
            }
            var tracks = (raw.results() == null ? List.<TrackRaw>of() : raw.results()).stream()
                    .map(t -> new Track(
                            t.trackId(),
                            t.trackName(),
                            t.artistName(),
                            t.collectionName(),
                            t.previewUrl(),
                            t.artworkUrl100(),
                            t.trackTimeMillis() == null ? 0 : t.trackTimeMillis(),
                            t.primaryGenreName()))
                    .toList();
            return new MusicResponse(raw.resultCount(), tracks);
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "iTunes Search returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "iTunes Search unreachable: " + e.getMessage(), 0, e);
        } catch (JacksonException e) {
            throw new ExternalApiException(
                    "iTunes Search returned malformed JSON: " + e.getMessage(), 502, e);
        }
    }

    private record ItunesRaw(int resultCount, List<TrackRaw> results) {
    }

    private record TrackRaw(
            @JsonProperty("trackId") long trackId,
            @JsonProperty("trackName") String trackName,
            @JsonProperty("artistName") String artistName,
            @JsonProperty("collectionName") String collectionName,
            @JsonProperty("previewUrl") String previewUrl,
            @JsonProperty("artworkUrl100") String artworkUrl100,
            @JsonProperty("trackTimeMillis") Integer trackTimeMillis,
            @JsonProperty("primaryGenreName") String primaryGenreName
    ) {
    }
}
