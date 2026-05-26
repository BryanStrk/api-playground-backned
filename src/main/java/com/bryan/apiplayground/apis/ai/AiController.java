package com.bryan.apiplayground.apis.ai;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "Inteligencia Artificial", description = "Generación de texto con Gemini 2.5 Flash (requiere GEMINI_API_KEY)")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/generate")
    @Operation(
            summary = "Genera texto a partir de un prompt",
            description = "Envía el prompt a gemini-2.5-flash y devuelve el texto generado junto con el conteo de tokens."
    )
    public GenerateResponse generate(@Valid @RequestBody GenerateRequest request) {
        return aiService.generate(request.prompt());
    }
}
