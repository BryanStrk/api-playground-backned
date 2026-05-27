package com.bryan.apiplayground.apis.pokemon;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pokemon")
@Tag(name = "Juegos", description = "Datos de Pokémon vía PokéAPI (sin API key)")
public class PokemonController {

    private final PokemonService pokemonService;

    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    @GetMapping("/{name}")
    @Operation(
            summary = "Pokémon por nombre",
            description = "Devuelve id, nombre, altura, peso, tipos y sprite oficial."
    )
    public PokemonResponse getByName(@PathVariable String name) {
        return pokemonService.getByName(name);
    }

    @GetMapping("/type/{type}")
    @Operation(
            summary = "Pokémon por tipo",
            description = "Devuelve la lista de nombres de Pokémon que pertenecen al tipo indicado (fire, water, electric…). El front lo usa para poblar un desplegable o filtro."
    )
    public PokemonTypeResponse listByType(@PathVariable String type) {
        return pokemonService.listByType(type);
    }
}
