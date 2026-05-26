package com.bryan.apiplayground.apis.pokemon;

import java.util.List;

public record PokemonResponse(
        int id,
        String name,
        int height,
        int weight,
        List<String> types,
        String imageUrl
) {
}
