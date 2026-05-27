package com.bryan.apiplayground.apis.dota;

public record ProMatch(
        long matchId,
        String leagueName,
        long startTime,
        int duration,
        boolean radiantWin,
        String radiantName,
        String direName
) {
}
