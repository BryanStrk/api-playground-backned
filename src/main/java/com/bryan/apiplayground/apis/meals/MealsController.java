package com.bryan.apiplayground.apis.meals;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/meals")
@Tag(name = "Cocina", description = "Recetas vía TheMealDB (sin API key)")
public class MealsController {

    private final MealsService mealsService;

    public MealsController(MealsService mealsService) {
        this.mealsService = mealsService;
    }

    @GetMapping("/search")
    @Operation(
            summary = "Buscar recetas por nombre",
            description = "Devuelve la lista de recetas que coinciden con el término. Vacía si no hay coincidencias."
    )
    public MealsResponse search(@RequestParam String name) {
        return mealsService.search(name);
    }
}
