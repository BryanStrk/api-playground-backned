package com.bryan.apiplayground.apis.pokemon;

import java.util.List;

public record PokemonTypeResponse(String type, int count, List<String> pokemon) {
}
