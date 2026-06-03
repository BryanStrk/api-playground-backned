package com.bryan.apiplayground.apis.sportsdb;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Service
public class SportsDbService {

    // TheSportsDB v1 carries the API key as a PATH segment (not a header/query).
    // The public test key "123" is the default, but it is still loaded from an
    // env var so it is never hardcoded and can be swapped for a paid key.
    private static final String SEARCH_TEAMS_URL =
            "https://www.thesportsdb.com/api/v1/json/{key}/searchteams.php?t={team}";

    private final RestClient restClient;
    private final String apiKey;

    public SportsDbService(RestClient restClient,
                           @Value("${thesportsdb.api-key:123}") String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    public SportsDbResponse searchTeam(String name) {
        try {
            var raw = restClient.get()
                    .uri(SEARCH_TEAMS_URL, apiKey, name)
                    .retrieve()
                    .body(SportsDbResponse.class);
            // searchteams.php returns {"teams": null} when nothing matches; the
            // field names line up 1:1 with SportsTeam, so we bind straight into it.
            if (raw == null || raw.teams() == null) {
                return new SportsDbResponse(List.of());
            }
            return raw;
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "TheSportsDB returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "TheSportsDB unreachable: " + e.getMessage(), 0, e);
        }
    }
}
