package com.bryan.apiplayground.apis.characters;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/characters")
@Tag(name = "Entretenimiento", description = "Personajes de Rick and Morty (sin API key)")
public class CharactersController {

    private final CharactersService charactersService;

    public CharactersController(CharactersService charactersService) {
        this.charactersService = charactersService;
    }

    @GetMapping("/search")
    @Operation(
            summary = "Buscar personajes por nombre",
            description = "Devuelve la lista de personajes cuyo nombre contiene el término (matching parcial del upstream). "
                    + "Si no hay coincidencias, devuelve lista vacía (la API real responde 404 en ese caso)."
    )
    public List<CharacterResponse> search(@RequestParam String name) {
        return charactersService.search(name);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Personaje por id",
            description = "Devuelve nombre, estado, especie, género, origen, última ubicación, imagen y número de episodios en los que aparece."
    )
    public CharacterResponse getById(@PathVariable int id) {
        return charactersService.getById(id);
    }
}
