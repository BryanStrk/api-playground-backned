package com.bryan.apiplayground.apis.characters;

public record CharacterResponse(
        int id,
        String name,
        String status,
        String species,
        String gender,
        String origin,
        String location,
        String imageUrl,
        int episodeCount
) {
}
