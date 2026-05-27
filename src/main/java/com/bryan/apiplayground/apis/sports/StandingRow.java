package com.bryan.apiplayground.apis.sports;

public record StandingRow(
        int position,
        String teamName,
        String crestUrl,
        int played,
        int won,
        int draw,
        int lost,
        int points
) {
}
