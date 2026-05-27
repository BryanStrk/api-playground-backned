package com.bryan.apiplayground.apis.sports;

import com.bryan.apiplayground.common.exception.ApiKeyNotConfiguredException;
import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class SportsService {

    private static final String STANDINGS_URL =
            "https://api.football-data.org/v4/competitions/{code}/standings";
    private static final String MATCHES_URL =
            "https://api.football-data.org/v4/competitions/{code}/matches";
    private static final String TEAMS_URL =
            "https://api.football-data.org/v4/competitions/{code}/teams";
    private static final String AUTH_HEADER = "X-Auth-Token";
    private static final Set<String> UPCOMING_STATUSES = Set.of("SCHEDULED", "TIMED");
    private static final int MATCHES_LIMIT = 15;
    // The 12 competitions available on Football-Data's free tier. Hardcoded
    // because the /v4/competitions endpoint requires a key just to list them
    // and the free-tier subset never changes — a static list keeps the
    // dashboard's competition selector working even before the user signs up.
    private static final List<CompetitionInfo> FREE_TIER_COMPETITIONS = List.of(
            new CompetitionInfo("PL", "Premier League"),
            new CompetitionInfo("PD", "La Liga"),
            new CompetitionInfo("BL1", "Bundesliga"),
            new CompetitionInfo("SA", "Serie A"),
            new CompetitionInfo("FL1", "Ligue 1"),
            new CompetitionInfo("CL", "UEFA Champions League"),
            new CompetitionInfo("DED", "Eredivisie"),
            new CompetitionInfo("PPL", "Primeira Liga"),
            new CompetitionInfo("ELC", "Championship"),
            new CompetitionInfo("BSA", "Brasileirão Série A"),
            new CompetitionInfo("WC", "FIFA World Cup"),
            new CompetitionInfo("EC", "European Championship")
    );

    private final RestClient restClient;
    private final String apiKey;

    public SportsService(RestClient restClient,
                         @Value("${football-data.api-key:}") String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    public List<CompetitionInfo> listCompetitions() {
        return FREE_TIER_COMPETITIONS;
    }

    public StandingsResponse getStandings(String competitionCode) {
        requireKey();
        try {
            var raw = restClient.get()
                    .uri(STANDINGS_URL, competitionCode)
                    .header(AUTH_HEADER, apiKey)
                    .retrieve()
                    .body(StandingsRaw.class);
            if (raw == null) {
                throw new ExternalApiException("Football-Data returned an empty payload", 502);
            }
            // The API exposes three standings for leagues (TOTAL, HOME, AWAY) and
            // one TOTAL block per group for cups (GROUP_A..). Keep only the TOTAL
            // blocks; the group field disambiguates league vs cup.
            var totals = (raw.standings() == null ? List.<StandingGroup>of() : raw.standings()).stream()
                    .filter(g -> "TOTAL".equalsIgnoreCase(g.type()))
                    .toList();
            var competitionName = raw.competition() == null ? null : raw.competition().name();
            var seasonLabel = seasonLabel(raw.season());
            // Cup: each TOTAL belongs to a different group (GROUP_A, GROUP_B…).
            // Surface every group so the dashboard can render all of them.
            var hasGroups = totals.stream().anyMatch(g -> g.group() != null && !g.group().isBlank());
            if (hasGroups) {
                var groups = totals.stream()
                        .map(g -> new GroupTable(g.group(), mapRows(g.table())))
                        .toList();
                return new StandingsResponse(competitionName, seasonLabel, List.of(), groups);
            }
            // League: exactly one TOTAL block, no group label — return as a flat table.
            var leagueRows = totals.isEmpty() ? List.<StandingRow>of() : mapRows(totals.getFirst().table());
            return new StandingsResponse(competitionName, seasonLabel, leagueRows, List.of());
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "Football-Data returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "Football-Data unreachable: " + e.getMessage(), 0, e);
        }
    }

    private static List<StandingRow> mapRows(List<TableRow> rows) {
        if (rows == null) return List.of();
        return rows.stream()
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
    }

    public MatchesResponse getMatches(String competitionCode, String type) {
        requireKey();
        try {
            var raw = restClient.get()
                    .uri(MATCHES_URL, competitionCode)
                    .header(AUTH_HEADER, apiKey)
                    .retrieve()
                    .body(MatchesRaw.class);
            if (raw == null) {
                throw new ExternalApiException("Football-Data returned an empty payload", 502);
            }
            var all = raw.matches() == null ? List.<MatchRaw>of() : raw.matches();
            // type controls which slice the caller wants. No auto-fallback: an
            // "upcoming" request after the season ends correctly returns []
            // rather than silently swapping to past results.
            List<MatchRaw> chosen;
            String mode;
            if ("recent".equalsIgnoreCase(type)) {
                chosen = all.stream()
                        .filter(m -> "FINISHED".equalsIgnoreCase(m.status()))
                        .sorted(Comparator.comparing(MatchRaw::utcDate,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                        .limit(MATCHES_LIMIT)
                        .toList();
                mode = "RECENT";
            } else {
                chosen = all.stream()
                        .filter(m -> m.status() != null
                                && UPCOMING_STATUSES.contains(m.status().toUpperCase()))
                        .sorted(Comparator.comparing(MatchRaw::utcDate,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .limit(MATCHES_LIMIT)
                        .toList();
                mode = "UPCOMING";
            }
            var rows = chosen.stream().map(SportsService::toMatchRow).toList();
            return new MatchesResponse(
                    raw.competition() == null ? null : raw.competition().name(),
                    mode,
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

    public TeamsResponse getTeams(String competitionCode) {
        requireKey();
        try {
            var raw = restClient.get()
                    .uri(TEAMS_URL, competitionCode)
                    .header(AUTH_HEADER, apiKey)
                    .retrieve()
                    .body(TeamsRaw.class);
            if (raw == null) {
                throw new ExternalApiException("Football-Data returned an empty payload", 502);
            }
            var rows = (raw.teams() == null ? List.<TeamRaw>of() : raw.teams()).stream()
                    .map(t -> new TeamRow(t.name(), t.crest(), t.venue(), t.founded()))
                    .toList();
            return new TeamsResponse(
                    raw.competition() == null ? null : raw.competition().name(),
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

    private static MatchRow toMatchRow(MatchRaw m) {
        Integer homeScore = null;
        Integer awayScore = null;
        if (m.score() != null && m.score().fullTime() != null) {
            homeScore = m.score().fullTime().home();
            awayScore = m.score().fullTime().away();
        }
        return new MatchRow(
                m.utcDate(),
                m.status(),
                m.matchday(),
                m.homeTeam() == null ? null : m.homeTeam().name(),
                m.homeTeam() == null ? null : m.homeTeam().crest(),
                m.awayTeam() == null ? null : m.awayTeam().name(),
                m.awayTeam() == null ? null : m.awayTeam().crest(),
                homeScore,
                awayScore
        );
    }

    private void requireKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ApiKeyNotConfiguredException(
                    "La API key de Football-Data no está configurada. Añade FOOTBALL_DATA_KEY a tu .env");
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

    private record StandingGroup(String stage, String type, String group, List<TableRow> table) {
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

    // Shared shape: standings uses id/name/crest, /teams adds venue + founded.
    // id is Integer (not int) because cup matches carry TBD entries with
    // {"id": null, "name": "Winner Match 49"} until the bracket fills in,
    // and a primitive int would blow up Jackson at parse time.
    private record TeamRaw(Integer id, String name, String crest, String venue, Integer founded) {
    }

    private record MatchesRaw(CompetitionRaw competition, List<MatchRaw> matches) {
    }

    private record MatchRaw(
            String utcDate,
            String status,
            Integer matchday,
            TeamRaw homeTeam,
            TeamRaw awayTeam,
            ScoreRaw score
    ) {
    }

    private record ScoreRaw(FullTimeRaw fullTime) {
    }

    private record FullTimeRaw(Integer home, Integer away) {
    }

    private record TeamsRaw(CompetitionRaw competition, List<TeamRaw> teams) {
    }
}
