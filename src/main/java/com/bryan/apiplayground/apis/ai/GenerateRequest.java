package com.bryan.apiplayground.apis.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GenerateRequest(
        @NotBlank(message = "prompt no puede estar vacío")
        @Size(max = 8000, message = "prompt supera los 8000 caracteres")
        String prompt
) {
}
