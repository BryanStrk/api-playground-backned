package com.bryan.apiplayground.apis.cocktails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cocktails")
@Tag(name = "Bebida", description = "Cócteles vía TheCocktailDB (sin API key)")
public class CocktailsController {

    private final CocktailsService cocktailsService;

    public CocktailsController(CocktailsService cocktailsService) {
        this.cocktailsService = cocktailsService;
    }

    @GetMapping("/search")
    @Operation(
            summary = "Buscar cócteles por nombre (name y category opcionales)",
            description = "Si llega ?name=, devuelve cócteles completos que coinciden con el nombre. "
                    + "Si además llega ?category=, filtra esos resultados por categoría. "
                    + "Si no llega ni name ni category, sirve un cóctel aleatorio desde random.php para "
                    + "que pulsar 'Ejecutar' sin teclear siempre muestre algo. Cada cóctel trae "
                    + "ingredientes pareados (nombre + medida), ignorando los slots vacíos del upstream."
    )
    public CocktailsResponse search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category) {
        if ((name == null || name.isBlank()) && (category == null || category.isBlank())) {
            return cocktailsService.random();
        }
        return cocktailsService.search(name, category);
    }

    @GetMapping("/categories")
    @Operation(
            summary = "Catálogo de categorías",
            description = "Lista las categorías de TheCocktailDB (Ordinary Drink, Cocktail, Shot…). El name se usa como ?c= en /cocktails/filter/category."
    )
    public List<CocktailCategory> categories() {
        return cocktailsService.categories();
    }

    @GetMapping("/filter/category")
    @Operation(
            summary = "Cócteles por categoría",
            description = "Versión 'ligera': devuelve solo id, name y thumb por cóctel. Para cargar "
                    + "los detalles (instrucciones, ingredientes…) llama después a /cocktails/{id}."
    )
    public CocktailsResponse filterByCategory(@RequestParam String c) {
        return cocktailsService.filterByCategory(c);
    }

    @GetMapping("/filter/alcoholic")
    @Operation(
            summary = "Cócteles por tipo alcohólico",
            description = "Versión 'ligera': filtra por Alcoholic o Non_Alcoholic y devuelve solo id, "
                    + "name y thumb por cóctel. Para los detalles llama después a /cocktails/{id}."
    )
    public CocktailsResponse filterByAlcoholic(@RequestParam String a) {
        return cocktailsService.filterByAlcoholic(a);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Detalle de un cóctel por id",
            description = "Devuelve el cóctel completo desde lookup.php con sus ingredientes pareados "
                    + "(nombre + medida), ignorando los slots vacíos del upstream."
    )
    public CocktailsResponse byId(@PathVariable String id) {
        return cocktailsService.byId(id);
    }
}
