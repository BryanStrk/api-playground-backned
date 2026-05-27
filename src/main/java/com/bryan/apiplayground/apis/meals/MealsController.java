package com.bryan.apiplayground.apis.meals;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
            description = "Devuelve recetas completas con ingredientes (pares nombre + medida, "
                    + "ignorando los slots vacíos del upstream). Vacía si no hay coincidencias."
    )
    public MealsResponse search(@RequestParam String q) {
        return mealsService.search(q);
    }

    @GetMapping("/categories")
    @Operation(
            summary = "Catálogo de categorías",
            description = "Lista las categorías de TheMealDB (Beef, Chicken, Dessert…) con id, miniatura y descripción. El name se usa como ?category= en /meals/filter."
    )
    public List<MealCategory> categories() {
        return mealsService.categories();
    }

    @GetMapping("/filter")
    @Operation(
            summary = "Recetas por categoría",
            description = "Versión 'ligera': devuelve solo id, name y thumbUrl por receta. Para cargar "
                    + "los detalles (instrucciones, ingredientes…) llama después a /meals/search?q={name}."
    )
    public List<MealSummary> filterByCategory(@RequestParam String category) {
        return mealsService.filterByCategory(category);
    }
}
