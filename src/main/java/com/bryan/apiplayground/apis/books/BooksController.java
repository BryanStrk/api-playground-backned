package com.bryan.apiplayground.apis.books;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Cultura", description = "Libros vía Open Library Search (sin API key)")
public class BooksController {

    private final BooksService booksService;

    public BooksController(BooksService booksService) {
        this.booksService = booksService;
    }

    @GetMapping("/search")
    @Operation(
            summary = "Buscar libros por título",
            description = "Devuelve los primeros resultados con autor, año y portada. El campo numFound expone el total disponible aunque limitemos la página."
    )
    public BooksResponse search(
            @RequestParam String title,
            @RequestParam(defaultValue = "12") int limit
    ) {
        return booksService.search(title, limit);
    }
}
