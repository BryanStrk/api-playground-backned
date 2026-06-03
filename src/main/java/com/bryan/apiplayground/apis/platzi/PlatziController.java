package com.bryan.apiplayground.apis.platzi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/platzi")
@Tag(name = "Compras", description = "Catálogo de productos vía Platzi Fake Store API (sin API key)")
public class PlatziController {

    private final PlatziService platziService;

    public PlatziController(PlatziService platziService) {
        this.platziService = platziService;
    }

    @GetMapping("/products")
    @Operation(
            summary = "Listado de productos",
            description = "Devuelve el catálogo de Platzi con categoría anidada e imágenes. "
                    + "Las URLs de imagen del upstream llegan a menudo rotas (envueltas en "
                    + "[\"...\"] o con comillas escapadas); el backend las sanea y descarta las inválidas."
    )
    public List<PlatziProduct> getProducts() {
        return platziService.getProducts();
    }

    @GetMapping("/products/{id}")
    @Operation(
            summary = "Detalle de un producto por id",
            description = "Devuelve un único producto de Platzi con sus imágenes ya saneadas."
    )
    public PlatziProduct getProduct(@PathVariable long id) {
        return platziService.getProduct(id);
    }

    @GetMapping("/categories")
    @Operation(
            summary = "Catálogo de categorías",
            description = "Lista las categorías de Platzi (id, name e imagen saneada)."
    )
    public List<Category> getCategories() {
        return platziService.getCategories();
    }
}
