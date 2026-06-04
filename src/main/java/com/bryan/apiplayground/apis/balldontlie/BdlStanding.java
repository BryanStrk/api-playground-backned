package com.bryan.apiplayground.apis.balldontlie;

public record BdlStanding(
        String group,
        String team,
        Integer position,
        Integer played,
        Integer won,
        Integer drawn,
        Integer lost,
        Integer goalsFor,
        Integer goalsAgainst,
        Integer goalDiff,
        Integer points
) {
}
