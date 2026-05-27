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
@Tag(name = "Inteligencia Artificial", description = "Generación de texto y chat con Gemini 2.5 Flash (requiere GEMINI_API_KEY)")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/generate")
    @Operation(
            summary = "Genera texto a partir de un prompt",
            description = "Envía el prompt a gemini-2.5-flash y devuelve el texto generado junto con el conteo de tokens. "
                    + "Pensado para single-turn; para mantener contexto entre turnos usa /ai/chat."
    )
    public GenerateResponse generate(@Valid @RequestBody GenerateRequest request) {
        return aiService.generate(request.prompt());
    }

    @PostMapping("/chat")
    @Operation(
            summary = "Conversación con historial",
            description = "Acepta una lista de mensajes con role 'user' o 'model'. El backend los traduce a "
                    + "la estructura contents/parts/role de Gemini, así el modelo recibe todo el historial y "
                    + "responde teniendo en cuenta los turnos anteriores."
    )
    public GenerateResponse chat(@Valid @RequestBody ChatRequest request) {
        return aiService.chat(request.messages());
    }
}
