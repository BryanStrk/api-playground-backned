package com.bryan.apiplayground.catalog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalog")
@Tag(name = "Catálogo", description = "Metadatos de las 20 APIs públicas que expone el playground")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    @Operation(
            summary = "Lista las 20 APIs disponibles",
            description = "Devuelve la metadata que el dashboard usa para pintar el grid: id, nombre, "
                    + "categoría, dificultad, si requiere API key, endpoint propio del backend, URL externa "
                    + "y una descripción corta."
    )
    public List<ApiInfo> getCatalog() {
        return catalogService.findAll();
    }
}
