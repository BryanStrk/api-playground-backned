package com.bryan.apiplayground.apis.worldcup;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.fasterxml.jackson.annotation.JsonAlias;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class WorldcupService {

    private static final String FEED_URL =
            "https://raw.githubusercontent.com/openfootball/worldcup.json/master/2026/worldcup.json";
    private static final long TTL_MS = 5 * 60 * 1000L;
    private static final String STATUS_PLAYED = "played";
    private static final String STATUS_UPCOMING = "upcoming";

    private final RestClient restClient;
    // openfootball publishes a static file that changes at most a couple of times
    // per day; a single shared snapshot per JVM with a 5-minute window keeps the
    // hot path off the network without holding stale data for long.
    private volatile Snapshot snapshot;

    public WorldcupService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<WorldCupMatch> getMatches(String group, String team, String status) {
        var matches = loadMatches();
        var teamFilter = team == null ? null : team.trim().toLowerCase(Locale.ROOT);
        var statusFilter = normalizeStatus(status);
        return matches.stream()
                .filter(m -> group == null || group.isBlank() || group.equalsIgnoreCase(m.group()))
                .filter(m -> teamFilter == null || teamFilter.isBlank()
                        || (m.team1() != null && m.team1().toLowerCase(Locale.ROOT).contains(teamFilter))
                        || (m.team2() != null && m.team2().toLowerCase(Locale.ROOT).contains(teamFilter)))
                .filter(m -> statusFilter == null || statusFilter.equals(m.status()))
                .toList();
    }

    public List<GroupStanding> getGroups() {
        // Group-stage only: openfootball leaves "group" null on knockout matches,
        // so the null filter doubles as a stage filter without hardcoding round
        // names like "Round of 32" / "Final".
        var tally = new LinkedHashMap<String, Map<String, Tally>>();
        for (var m : loadMatches()) {
            if (m.group() == null || m.group().isBlank()) continue;
            if (!STATUS_PLAYED.equals(m.status())) continue;
            var groupRows = tally.computeIfAbsent(m.group(), k -> new LinkedHashMap<>());
            var t1 = groupRows.computeIfAbsent(m.team1(), k -> new Tally());
            var t2 = groupRows.computeIfAbsent(m.team2(), k -> new Tally());
            t1.record(m.score1(), m.score2());
            t2.record(m.score2(), m.score1());
        }
        // Seed every team that appears in any group-stage fixture so the table
        // renders all 4 rows per group even before any match has been played.
        for (var m : loadMatches()) {
            if (m.group() == null || m.group().isBlank()) continue;
            var groupRows = tally.computeIfAbsent(m.group(), k -> new LinkedHashMap<>());
            groupRows.computeIfAbsent(m.team1(), k -> new Tally());
            groupRows.computeIfAbsent(m.team2(), k -> new Tally());
        }
        var result = new ArrayList<GroupStanding>();
        tally.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(groupEntry -> {
                    var groupName = groupEntry.getKey();
                    groupEntry.getValue().entrySet().stream()
                            .map(e -> e.getValue().toStanding(groupName, e.getKey()))
                            .sorted(Comparator
                                    .comparingInt(GroupStanding::points).reversed()
                                    .thenComparing(Comparator.comparingInt(GroupStanding::goalDiff).reversed())
                                    .thenComparing(Comparator.comparingInt(GroupStanding::goalsFor).reversed())
                                    .thenComparing(GroupStanding::team))
                            .forEach(result::add);
                });
        return result;
    }

    List<WorldCupMatch> loadMatches() {
        var current = snapshot;
        if (current != null && System.currentTimeMillis() - current.fetchedAt < TTL_MS) {
            return current.matches;
        }
        return refresh();
    }

    private synchronized List<WorldCupMatch> refresh() {
        // Double-check: a concurrent caller may have populated the cache while we
        // were waiting on the monitor. No point burning a second HTTP call.
        var current = snapshot;
        if (current != null && System.currentTimeMillis() - current.fetchedAt < TTL_MS) {
            return current.matches;
        }
        var fetched = fetch();
        snapshot = new Snapshot(fetched, System.currentTimeMillis());
        return fetched;
    }

    private List<WorldCupMatch> fetch() {
        try {
            var raw = restClient.get()
                    .uri(FEED_URL)
                    .retrieve()
                    .body(FeedRaw.class);
            if (raw == null || raw.matches() == null) return List.of();
            return raw.matches().stream().map(WorldcupService::toMatch).toList();
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "openfootball returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "openfootball unreachable: " + e.getMessage(), 0, e);
        }
    }

    private static WorldCupMatch toMatch(MatchRaw raw) {
        Integer s1 = null;
        Integer s2 = null;
        if (raw.score() != null && raw.score().ft() != null && raw.score().ft().size() >= 2) {
            s1 = raw.score().ft().get(0);
            s2 = raw.score().ft().get(1);
        }
        var status = (s1 != null && s2 != null) ? STATUS_PLAYED : STATUS_UPCOMING;
        return new WorldCupMatch(
                raw.round(),
                raw.date(),
                raw.time(),
                raw.team1(),
                raw.team2(),
                raw.group(),
                raw.ground(),
                s1,
                s2,
                status,
                toGoals(raw.goals1()),
                toGoals(raw.goals2())
        );
    }

    private static List<Goal> toGoals(List<GoalRaw> raw) {
        if (raw == null) return null;
        return raw.stream()
                .map(g -> new Goal(g.name(), g.minute(), Boolean.TRUE.equals(g.penalty()),
                        Boolean.TRUE.equals(g.ownGoal())))
                .toList();
    }

    private static String normalizeStatus(String status) {
        if (status == null || status.isBlank()) return null;
        var s = status.trim().toLowerCase(Locale.ROOT);
        if (!s.equals(STATUS_PLAYED) && !s.equals(STATUS_UPCOMING)) {
            throw new IllegalArgumentException("status must be 'upcoming' or 'played'");
        }
        return s;
    }

    private static final class Tally {
        int played, won, drawn, lost, goalsFor, goalsAgainst;

        void record(int forGoals, int againstGoals) {
            played++;
            goalsFor += forGoals;
            goalsAgainst += againstGoals;
            if (forGoals > againstGoals) won++;
            else if (forGoals < againstGoals) lost++;
            else drawn++;
        }

        GroupStanding toStanding(String group, String team) {
            return new GroupStanding(group, team, played, won, drawn, lost,
                    goalsFor, goalsAgainst, goalsFor - goalsAgainst, won * 3 + drawn);
        }
    }

    private record Snapshot(List<WorldCupMatch> matches, long fetchedAt) {
    }

    private record FeedRaw(String name, List<MatchRaw> matches) {
    }

    private record MatchRaw(
            String round,
            String date,
            String time,
            String team1,
            String team2,
            String group,
            String ground,
            ScoreRaw score,
            List<GoalRaw> goals1,
            List<GoalRaw> goals2
    ) {
    }

    private record ScoreRaw(List<Integer> ft, List<Integer> ht) {
    }

    // owngoal is the dominant spelling in openfootball feeds; own_goal shows up
    // in older snapshots — accept both so a format flip upstream doesn't blank
    // the field silently.
    private record GoalRaw(
            String name,
            Integer minute,
            Boolean penalty,
            @JsonAlias({"owngoal", "own_goal"}) Boolean ownGoal
    ) {
    }
}
