package com.bryan.apiplayground.apis.dictionary;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dictionary")
@Tag(name = "Idiomas", description = "Diccionario inglés vía Free Dictionary (sin API key)")
public class DictionaryController {

    private final DictionaryService dictionaryService;

    public DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @GetMapping("/{word}")
    @Operation(
            summary = "Definiciones en inglés de una palabra",
            description = "Devuelve una entrada por etimología, con sus significados (parte de la oración) y definiciones."
    )
    public List<DictionaryEntry> lookup(@PathVariable String word) {
        return dictionaryService.lookup(word);
    }
}
