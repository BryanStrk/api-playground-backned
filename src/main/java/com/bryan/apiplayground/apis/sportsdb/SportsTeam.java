package com.bryan.apiplayground.apis.sportsdb;

public record SportsTeam(
        String idTeam,
        String strTeam,
        String strLeague,
        String strStadium,
        String strBadge,
        String strDescriptionEN
) {
}
