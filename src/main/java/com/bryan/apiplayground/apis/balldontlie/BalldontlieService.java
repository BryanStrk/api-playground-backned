package com.bryan.apiplayground.apis.balldontlie;

import com.bryan.apiplayground.common.exception.ApiKeyNotConfiguredException;
import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.fasterxml.jackson.annotation.JsonAlias;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Service
public class BalldontlieService {

    private static final String BASE_URL = "https://api.balldontlie.io/fifa/worldcup/v1";
    private static final String TEAMS_URL = BASE_URL + "/teams?per_page=100";
    private static final String STADIUMS_URL = BASE_URL + "/stadiums?per_page=100";

    private final RestClient restClient;
    private final String apiKey;

    public BalldontlieService(RestClient restClient,
                              @Value("${balldontlie.api-key:}") String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    public List<BdlTeam> getTeams() {
        var envelope = call(TEAMS_URL, TeamsEnvelope.class);
        if (envelope.data() == null) return List.of();
        return envelope.data().stream()
                .map(t -> new BdlTeam(t.id(), t.name(), t.abbreviation(), t.countryCode(), t.confederation()))
                .toList();
    }

    public List<BdlStadium> getStadiums() {
        var envelope = call(STADIUMS_URL, StadiumsEnvelope.class);
        if (envelope.data() == null) return List.of();
        return envelope.data().stream()
                .map(s -> new BdlStadium(s.id(), s.name(), s.city(), s.country(),
                        s.capacity(), s.latitude(), s.longitude()))
                .toList();
    }

    // Shared call wrapper: validates the key, sets the Authorization header in
    // the raw form BALLDONTLIE expects (NO "Bearer " prefix), and translates
    // upstream failures into the project's exception hierarchy so the global
    // handler can pick the right HTTP status.
    private <T> T call(String url, Class<T> type) {
        requireKey();
        try {
            var body = restClient.get()
                    .uri(url)
                    .header("Authorization", apiKey)
                    .retrieve()
                    .body(type);
            if (body == null) {
                throw new ExternalApiException("BALLDONTLIE returned an empty payload", 502);
            }
            return body;
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "BALLDONTLIE returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "BALLDONTLIE unreachable: " + e.getMessage(), 0, e);
        }
    }

    private void requireKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ApiKeyNotConfiguredException(
                    "La API key de BALLDONTLIE no está configurada. Añade BALLDONTLIE_API_KEY a tu .env");
        }
    }

    // Envelope shape from BALLDONTLIE: { "data": [...], "meta": { "next_cursor": ... } }.
    // Cursor pagination is exposed via the controller (per_page=100) — for the
    // playground demo we surface only the first page.
    private record TeamsEnvelope(List<TeamRaw> data) {
    }

    private record StadiumsEnvelope(List<StadiumRaw> data) {
    }

    // Snake-case is the dominant style upstream; @JsonAlias keeps the mapping
    // resilient if a field flips to camelCase later (mirrors how openfootball's
    // owngoal/own_goal alias works in WorldcupService).
    private record TeamRaw(
            int id,
            String name,
            String abbreviation,
            @JsonAlias({"country_code", "countryCode"}) String countryCode,
            String confederation
    ) {
    }

    private record StadiumRaw(
            int id,
            String name,
            String city,
            String country,
            Integer capacity,
            Double latitude,
            Double longitude
    ) {
    }
}
