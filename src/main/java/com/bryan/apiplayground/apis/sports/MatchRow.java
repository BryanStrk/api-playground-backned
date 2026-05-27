package com.bryan.apiplayground.apis.sports;

public record MatchRow(
        String utcDate,
        String status,
        Integer matchday,
        String homeTeam,
        String homeCrest,
        String awayTeam,
        String awayCrest,
        Integer homeScore,
        Integer awayScore
) {
}
