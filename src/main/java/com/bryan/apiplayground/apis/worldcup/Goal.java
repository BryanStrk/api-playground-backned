package com.bryan.apiplayground.apis.worldcup;

public record Goal(
        String name,
        Integer minute,
        boolean penalty,
        boolean ownGoal
) {
}
