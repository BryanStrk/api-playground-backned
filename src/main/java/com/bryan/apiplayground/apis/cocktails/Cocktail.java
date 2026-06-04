package com.bryan.apiplayground.apis.cocktails;

import java.util.List;

public record Cocktail(
        String id,
        String name,
        String thumb,
        String category,
        String alcoholic,
        String glass,
        String instructions,
        List<Ingredient> ingredients
) {
}
