package com.bryan.apiplayground.apis.worldcup;

public record GroupStanding(
        String group,
        String team,
        int played,
        int won,
        int drawn,
        int lost,
        int goalsFor,
        int goalsAgainst,
        int goalDiff,
        int points
) {
}
