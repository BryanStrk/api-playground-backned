package com.bryan.apiplayground.apis.balldontlie;

public record BdlTeam(
        int id,
        String name,
        String abbreviation,
        String countryCode,
        String confederation
) {
}
