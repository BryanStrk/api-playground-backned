package com.bryan.apiplayground.apis.sportsdb;

import java.util.List;

public record SportsDbResponse(
        List<SportsTeam> teams
) {
}
