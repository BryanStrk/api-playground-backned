package com.bryan.apiplayground.apis.sports;

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
public class SportsService {

    private static final String STANDINGS_URL =
            "https://api.football-data.org/v4/competitions/{code}/standings";
    private static final String AUTH_HEADER = "X-Auth-Token";

    private final RestClient restClient;
    private final String apiKey;

    public SportsService(RestClient restClient,
                         @Value("${football-data.api-key:}") String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    public StandingsResponse getStandings(String competitionCode) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ApiKeyNotConfiguredException(
                    "La API key de Football-Data no está configurada. Añade FOOTBALL_DATA_KEY a tu .env");
        }
        try {
            var raw = restClient.get()
                    .uri(STANDINGS_URL, competitionCode)
                    .header(AUTH_HEADER, apiKey)
                    .retrieve()
                    .body(StandingsRaw.class);
            if (raw == null) {
                throw new ExternalApiException("Football-Data returned an empty payload", 502);
            }
            // The API exposes three standings (TOTAL, HOME, AWAY); we surface TOTAL.
            var totalGroup = (raw.standings() == null ? List.<StandingGroup>of() : raw.standings()).stream()
                    .filter(g -> "TOTAL".equalsIgnoreCase(g.type()))
                    .findFirst()
                    .orElseGet(() -> raw.standings() == null || raw.standings().isEmpty()
                            ? null
                            : raw.standings().getFirst());
            var rows = (totalGroup == null || totalGroup.table() == null
                    ? List.<TableRow>of()
                    : totalGroup.table()).stream()
                    .map(r -> new StandingRow(
                            r.position(),
                            r.team() == null ? null : r.team().name(),
                            r.team() == null ? null : r.team().crest(),
                            r.playedGames(),
                            r.won(),
                            r.draw(),
                            r.lost(),
                            r.points()))
                    .toList();
            return new StandingsResponse(
                    raw.competition() == null ? null : raw.competition().name(),
                    seasonLabel(raw.season()),
                    rows
            );
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "Football-Data returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "Football-Data unreachable: " + e.getMessage(), 0, e);
        }
    }

    // Football-Data returns startDate / endDate as "YYYY-MM-DD"; render as
    // "2024/25" when the season spans two calendar years (most leagues) or
    // just the start year when it's contained in one (e.g. summer tournaments).
    private static String seasonLabel(SeasonRaw season) {
        if (season == null || season.startDate() == null || season.startDate().length() < 4) {
            return null;
        }
        var startYear = season.startDate().substring(0, 4);
        if (season.endDate() == null || season.endDate().length() < 4) {
            return startYear;
        }
        var endYear = season.endDate().substring(0, 4);
        return startYear.equals(endYear) ? startYear : startYear + "/" + endYear.substring(2);
    }

    private record StandingsRaw(
            CompetitionRaw competition,
            SeasonRaw season,
            List<StandingGroup> standings
    ) {
    }

    private record CompetitionRaw(String name, String code) {
    }

    private record SeasonRaw(
            @JsonProperty("startDate") String startDate,
            @JsonProperty("endDate") String endDate
    ) {
    }

    private record StandingGroup(String stage, String type, List<TableRow> table) {
    }

    private record TableRow(
            int position,
            TeamRaw team,
            @JsonProperty("playedGames") int playedGames,
            int won,
            int draw,
            int lost,
            int points
    ) {
    }

    private record TeamRaw(int id, String name, String crest) {
    }
}
