package com.bryan.apiplayground.apis.balldontlie;

public record BdlMatch(
        int id,
        Integer matchNumber,
        String datetime,
        String status,
        String stageName,
        String group,
        String stadiumName,
        String homeTeam,
        String awayTeam,
        Integer homeScore,
        Integer awayScore
) {
}
