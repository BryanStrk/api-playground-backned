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
            summary = "Buscar recetas por nombre (q opcional)",
            description = "Si llega ?q=, devuelve recetas completas que coinciden con el nombre. "
                    + "Si no llega o viene vacío, sirve una receta aleatoria desde random.php para "
                    + "que pulsar 'Ejecutar' sin teclear siempre muestre algo. Cada receta trae "
                    + "ingredientes pareados (nombre + medida), ignorando los slots vacíos del upstream."
    )
    public MealsResponse search(@RequestParam(required = false) String q) {
        if (q == null || q.isBlank()) {
            return mealsService.random();
        }
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
