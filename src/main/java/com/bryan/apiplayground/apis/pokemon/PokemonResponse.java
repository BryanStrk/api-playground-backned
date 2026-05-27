package com.bryan.apiplayground.apis.pokemon;

import java.util.List;
import java.util.Map;

public record PokemonResponse(
        int id,
        String name,
        int height,
        int weight,
        List<String> types,
        String spriteUrl,
        Map<String, Integer> stats
) {
}
