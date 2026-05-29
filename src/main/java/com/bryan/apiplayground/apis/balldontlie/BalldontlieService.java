package com.bryan.apiplayground.apis.balldontlie;

import com.bryan.apiplayground.common.exception.ApiKeyNotConfiguredException;
import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.bryan.apiplayground.common.exception.PremiumFeatureException;
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
    private static final String STANDINGS_URL = BASE_URL + "/standings?per_page=100";
    private static final String MATCHES_URL = BASE_URL + "/matches?per_page=100&season={season}";
    private static final String PLAYERS_URL = BASE_URL + "/players?per_page=100&search={search}";
    private static final String PLAN_ALL_STAR = "ALL-STAR";
    private static final String PLAN_GOAT = "GOAT";
    // Static catalog of BALLDONTLIE tier pricing and feature coverage. Hardcoded
    // because there's no public endpoint that surfaces it — the frontend reads
    // this to render padlocks on premium cards without duplicating the list.
    // Each tier inherits the previous tier's features (FREE ⊂ ALL-STAR ⊂ GOAT).
    private static final List<BdlTier> TIERS = List.of(
            new BdlTier("FREE", 0.0, 5,
                    List.of("teams", "stadiums")),
            new BdlTier(PLAN_ALL_STAR, 9.99, 60,
                    List.of("teams", "stadiums", "standings")),
            new BdlTier(PLAN_GOAT, 39.99, 600,
                    List.of("teams", "stadiums", "standings", "matches", "players", "odds", "stats"))
    );

    private final RestClient restClient;
    private final String apiKey;

    public BalldontlieService(RestClient restClient,
                              @Value("${balldontlie.api-key:}") String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    public List<BdlTier> getTiers() {
        return TIERS;
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

    public List<BdlStanding> getStandings() {
        var envelope = callPremium(STANDINGS_URL, StandingsEnvelope.class,
                PLAN_ALL_STAR, "standings");
        if (envelope.data() == null) return List.of();
        return envelope.data().stream()
                .map(s -> new BdlStanding(
                        s.group(),
                        s.team() == null ? null : s.team().name(),
                        s.position(),
                        s.played(),
                        s.won(),
                        s.drawn(),
                        s.lost(),
                        s.goalsFor(),
                        s.goalsAgainst(),
                        s.goalDiff(),
                        s.points()))
                .toList();
    }

    public List<BdlMatch> getMatches(Integer season) {
        var effectiveSeason = season == null ? 2026 : season;
        var envelope = callPremiumUri(MATCHES_URL, MatchesEnvelope.class,
                PLAN_GOAT, "matches", effectiveSeason);
        if (envelope.data() == null) return List.of();
        return envelope.data().stream()
                .map(m -> new BdlMatch(
                        m.id(),
                        m.matchNumber(),
                        m.datetime(),
                        m.status(),
                        m.stageName(),
                        m.group(),
                        m.stadium() == null ? null : m.stadium().name(),
                        m.homeTeam() == null ? null : m.homeTeam().name(),
                        m.awayTeam() == null ? null : m.awayTeam().name(),
                        m.homeScore(),
                        m.awayScore()))
                .toList();
    }

    public List<BdlPlayer> getPlayers(String search) {
        // BALLDONTLIE accepts an empty search param and returns the first page
        // of all players; no need to special-case null.
        var effectiveSearch = search == null ? "" : search.trim();
        var envelope = callPremiumUri(PLAYERS_URL, PlayersEnvelope.class,
                PLAN_GOAT, "players", effectiveSearch);
        if (envelope.data() == null) return List.of();
        return envelope.data().stream()
                .map(p -> new BdlPlayer(
                        p.id(),
                        p.name(),
                        p.shortName(),
                        p.position(),
                        p.country() == null ? null : p.country().name(),
                        p.heightCm(),
                        p.jerseyNumber()))
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

    // Variant of call() for tier-gated endpoints: upstream 401 means "key valid
    // but plan doesn't cover this feature" — translate to PremiumFeatureException
    // so the global handler emits 402 with requiredPlan + feature, separating it
    // from a real auth failure (which would be 503 ApiKeyNotConfigured).
    private <T> T callPremium(String url, Class<T> type, String requiredPlan, String feature) {
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
            if (e.getStatusCode().value() == 401) {
                throw new PremiumFeatureException(requiredPlan, feature);
            }
            throw new ExternalApiException(
                    "BALLDONTLIE returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "BALLDONTLIE unreachable: " + e.getMessage(), 0, e);
        }
    }

    // Same as callPremium but for URLs with placeholder vars (season, search).
    private <T> T callPremiumUri(String url, Class<T> type, String requiredPlan,
                                 String feature, Object... uriVars) {
        requireKey();
        try {
            var body = restClient.get()
                    .uri(url, uriVars)
                    .header("Authorization", apiKey)
                    .retrieve()
                    .body(type);
            if (body == null) {
                throw new ExternalApiException("BALLDONTLIE returned an empty payload", 502);
            }
            return body;
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new PremiumFeatureException(requiredPlan, feature);
            }
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

    private record StandingsEnvelope(List<StandingRaw> data) {
    }

    private record StandingRaw(
            String group,
            TeamRef team,
            Integer position,
            Integer played,
            Integer won,
            Integer drawn,
            Integer lost,
            @JsonAlias({"goals_for", "goalsFor"}) Integer goalsFor,
            @JsonAlias({"goals_against", "goalsAgainst"}) Integer goalsAgainst,
            @JsonAlias({"goal_diff", "goalDiff"}) Integer goalDiff,
            Integer points
    ) {
    }

    private record MatchesEnvelope(List<MatchRaw> data) {
    }

    // homeTeam / awayTeam come back as nested {id,name,…} objects when the
    // bracket is settled, and as null in pre-bracket knockouts — flattening to
    // a String at the DTO layer keeps the frontend free of TBD-handling logic.
    private record MatchRaw(
            int id,
            @JsonAlias({"match_number", "matchNumber"}) Integer matchNumber,
            String datetime,
            String status,
            @JsonAlias({"stage_name", "stageName"}) String stageName,
            String group,
            StadiumRef stadium,
            @JsonAlias({"home_team", "homeTeam"}) TeamRef homeTeam,
            @JsonAlias({"away_team", "awayTeam"}) TeamRef awayTeam,
            @JsonAlias({"home_score", "homeScore"}) Integer homeScore,
            @JsonAlias({"away_score", "awayScore"}) Integer awayScore
    ) {
    }

    private record PlayersEnvelope(List<PlayerRaw> data) {
    }

    private record PlayerRaw(
            int id,
            String name,
            @JsonAlias({"short_name", "shortName"}) String shortName,
            String position,
            CountryRef country,
            @JsonAlias({"height_cm", "heightCm"}) Integer heightCm,
            @JsonAlias({"jersey_number", "jerseyNumber"}) String jerseyNumber
    ) {
    }

    private record TeamRef(Integer id, String name) {
    }

    private record StadiumRef(Integer id, String name) {
    }

    private record CountryRef(Integer id, String name) {
    }
}
