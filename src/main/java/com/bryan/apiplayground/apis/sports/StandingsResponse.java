package com.bryan.apiplayground.apis.sports;

import java.util.List;

public record StandingsResponse(
        String competition,
        String season,
        List<StandingRow> table
) {
}
