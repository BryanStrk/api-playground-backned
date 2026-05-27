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

    @GetMapping("/random")
    @Operation(
            summary = "Personaje aleatorio",
            description = "Devuelve un personaje aleatorio del catálogo completo (Rick and Morty no expone un /random "
                    + "propio, así que el servicio lee info.count y pide un id al azar dentro del rango)."
    )
    public CharacterResponse getRandom() {
        return charactersService.getRandom();
    }

    @GetMapping("/list")
    @Operation(
            summary = "Listado paginado con filtros",
            description = "Devuelve una página de 20 personajes (la paginación la fija el upstream). "
                    + "Filtros opcionales: status (alive/dead/unknown), species, gender (female/male/genderless/unknown). "
                    + "Sin coincidencias → página vacía (info en cero y results vacío)."
    )
    public CharacterPage list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String species,
            @RequestParam(required = false) String gender,
            @RequestParam(defaultValue = "1") int page
    ) {
        return charactersService.list(status, species, gender, page);
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
