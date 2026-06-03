package com.bryan.apiplayground.apis.food;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/food")
@Tag(name = "Comida", description = "Información nutricional de productos vía Open Food Facts (sin API key)")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping("/product")
    @Operation(
            summary = "Producto por código de barras",
            description = "Devuelve nombre, marca, imagen, Nutri-Score, grupo NOVA y un subconjunto "
                    + "de nutrientes por 100 g. Si el código de barras no existe, devuelve cuerpo vacío."
    )
    public FoodProduct getProduct(@RequestParam String barcode) {
        return foodService.getProduct(barcode);
    }

    @GetMapping("/search")
    @Operation(
            summary = "Búsqueda de productos por texto",
            description = "Busca productos por término (marca, nombre…) y devuelve hasta 10 resultados "
                    + "con sus datos nutricionales."
    )
    public List<FoodProduct> search(@RequestParam String q) {
        return foodService.search(q);
    }
}
