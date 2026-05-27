package com.bryan.apiplayground.apis.cats;

public record CatBreed(
        String id,
        String name,
        String description,
        String temperament,
        String origin,
        Integer lifeSpan
) {
}
