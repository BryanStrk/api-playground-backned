package com.bryan.apiplayground.catalog;

public record ApiInfo(
        String id,
        String name,
        String category,
        Difficulty difficulty,
        KeyRequirement requiresKey,
        String localEndpoint,
        String externalUrl,
        String description
) {
}
