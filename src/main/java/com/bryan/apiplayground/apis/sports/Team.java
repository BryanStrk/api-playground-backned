package com.bryan.apiplayground.apis.sports;

public record Team(
        String id,
        String name,
        String sport,
        String league,
        String country,
        String stadium,
        String description,
        String badgeUrl,
        String logoUrl
) {
}
