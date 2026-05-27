package com.bryan.apiplayground.apis.characters;

public record CharacterSummary(
        int id,
        String name,
        String imageUrl,
        String status,
        String species,
        String gender
) {
}
