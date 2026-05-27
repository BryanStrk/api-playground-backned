package com.bryan.apiplayground.apis.cats;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cats")
@Tag(name = "Animales", description = "The Cat API (key opcional, mejora el rate limit)")
public class CatsController {

    private final CatsService catsService;

    public CatsController(CatsService catsService) {
        this.catsService = catsService;
    }

    @GetMapping("/random")
    @Operation(
            summary = "Imagen aleatoria de gato",
            description = "Devuelve id, url y dimensiones. Si llega ?breed= se filtra por esa raza (el id se obtiene de /cats/breeds). Si CAT_API_KEY está configurada, se envía en el header x-api-key."
    )
    public CatResponse getRandom(@RequestParam(required = false) String breed) {
        return catsService.getRandom(breed);
    }

    @GetMapping("/breeds")
    @Operation(
            summary = "Lista de razas",
            description = "Devuelve el catálogo completo de razas (id, nombre, descripción, temperamento, origen, longevidad máxima). El id se usa para filtrar /cats/random?breed=."
    )
    public List<CatBreed> listBreeds() {
        return catsService.listBreeds();
    }
}
