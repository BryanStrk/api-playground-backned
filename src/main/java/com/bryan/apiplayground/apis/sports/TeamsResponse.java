package com.bryan.apiplayground.apis.sports;

import java.util.List;

public record TeamsResponse(
        String competition,
        List<TeamRow> teams
) {
}
