package com.bryan.apiplayground.apis.sports;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Service
public class SportsService {

    private static final String SEARCH_URL =
            "https://www.thesportsdb.com/api/v1/json/{key}/searchteams.php?t={name}";

    private final RestClient restClient;
    private final String apiKey;

    public SportsService(RestClient restClient,
                         @Value("${sportsdb.api-key:123}") String apiKey) {
        this.restClient = restClient;
        // SPORTSDB_KEY is OPTIONAL: empty falls back to the public test key "123".
        this.apiKey = (apiKey == null || apiKey.isBlank()) ? "123" : apiKey;
    }

    public TeamsResponse searchTeams(String name) {
        try {
            var raw = restClient.get()
                    .uri(SEARCH_URL, apiKey, name)
                    .retrieve()
                    .body(TeamsRaw.class);
            // TheSportsDB returns {"teams": null} when there is no match.
            if (raw == null || raw.teams() == null) {
                return new TeamsResponse(List.of());
            }
            var teams = raw.teams().stream()
                    .map(t -> new Team(
                            t.idTeam(),
                            t.strTeam(),
                            t.strSport(),
                            t.strLeague(),
                            t.strCountry(),
                            t.strStadium(),
                            t.strDescriptionEN(),
                            t.strBadge(),
                            t.strLogo()))
                    .toList();
            return new TeamsResponse(teams);
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "TheSportsDB returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "TheSportsDB unreachable: " + e.getMessage(), 0, e);
        }
    }

    private record TeamsRaw(List<TeamRaw> teams) {
    }

    private record TeamRaw(
            @JsonProperty("idTeam") String idTeam,
            @JsonProperty("strTeam") String strTeam,
            @JsonProperty("strSport") String strSport,
            @JsonProperty("strLeague") String strLeague,
            @JsonProperty("strCountry") String strCountry,
            @JsonProperty("strStadium") String strStadium,
            @JsonProperty("strDescriptionEN") String strDescriptionEN,
            @JsonProperty("strBadge") String strBadge,
            @JsonProperty("strLogo") String strLogo
    ) {
    }
}
