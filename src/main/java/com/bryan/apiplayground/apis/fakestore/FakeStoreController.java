package com.bryan.apiplayground.apis.fakestore;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/store")
@Tag(name = "Compras", description = "Catálogo de productos de demo vía FakeStoreAPI (sin API key)")
public class FakeStoreController {

    private final FakeStoreService fakeStoreService;

    public FakeStoreController(FakeStoreService fakeStoreService) {
        this.fakeStoreService = fakeStoreService;
    }

    @GetMapping("/products")
    @Operation(
            summary = "Listado de productos",
            description = "Devuelve el catálogo completo de FakeStoreAPI con precio, categoría, "
                    + "imagen y rating (nota + número de votos)."
    )
    public List<Product> getProducts() {
        return fakeStoreService.getProducts();
    }

    @GetMapping("/products/{id}")
    @Operation(
            summary = "Detalle de un producto por id",
            description = "Devuelve un único producto del catálogo de FakeStoreAPI."
    )
    public Product getProduct(@PathVariable long id) {
        return fakeStoreService.getProduct(id);
    }

    @GetMapping("/products/categories")
    @Operation(
            summary = "Catálogo de categorías",
            description = "Lista las categorías disponibles (electronics, jewelery, men's clothing…)."
    )
    public List<String> getCategories() {
        return fakeStoreService.getCategories();
    }
}
