package com.bryan.apiplayground.apis.balldontlie;

public record BdlPlayer(
        int id,
        String name,
        String shortName,
        String position,
        String countryName,
        Integer heightCm,
        String jerseyNumber
) {
}
