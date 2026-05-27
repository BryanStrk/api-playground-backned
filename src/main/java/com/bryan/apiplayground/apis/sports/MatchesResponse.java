package com.bryan.apiplayground.apis.sports;

import java.util.List;

public record MatchesResponse(
        String competition,
        String mode,
        List<MatchRow> matches
) {
}
