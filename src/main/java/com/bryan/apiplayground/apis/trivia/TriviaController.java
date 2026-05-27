package com.bryan.apiplayground.apis.trivia;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trivia")
@Tag(name = "Juegos", description = "Preguntas de cultura general vía OpenTriviaDB (sin API key)")
public class TriviaController {

    private final TriviaService triviaService;

    public TriviaController(TriviaService triviaService) {
        this.triviaService = triviaService;
    }

    @GetMapping
    @Operation(
            summary = "Preguntas de trivia",
            description = "amount: número de preguntas (default 10). category: id numérico (de /trivia/categories). "
                    + "difficulty: easy | medium | hard. type: multiple (test) o boolean (verdadero/falso)."
    )
    public List<TriviaQuestion> getQuestions(
            @RequestParam(defaultValue = "10") int amount,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String type
    ) {
        return triviaService.getQuestions(amount, category, difficulty, type);
    }

    @GetMapping("/categories")
    @Operation(
            summary = "Catálogo de categorías",
            description = "Devuelve los ids y nombres de categorías que OpenTriviaDB soporta, para poblar el filtro."
    )
    public List<TriviaCategory> getCategories() {
        return triviaService.getCategories();
    }
}
