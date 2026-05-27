package com.bryan.apiplayground.apis.meals;

import java.util.List;

public record Meal(
        String id,
        String name,
        String category,
        String area,
        String instructions,
        String thumbUrl,
        String youtubeUrl,
        List<Ingredient> ingredients
) {
}
