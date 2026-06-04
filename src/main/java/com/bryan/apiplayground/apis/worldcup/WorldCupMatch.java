package com.bryan.apiplayground.apis.worldcup;

import java.util.List;

public record WorldCupMatch(
        String round,
        String date,
        String time,
        String team1,
        String team2,
        String group,
        String ground,
        Integer score1,
        Integer score2,
        String status,
        List<Goal> goals1,
        List<Goal> goals2
) {
}
