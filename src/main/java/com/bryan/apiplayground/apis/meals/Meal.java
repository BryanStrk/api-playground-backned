package com.bryan.apiplayground.apis.meals;

public record Meal(
        String id,
        String name,
        String category,
        String area,
        String instructions,
        String thumbnail,
        String youtubeUrl
) {
}
