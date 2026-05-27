package com.bryan.apiplayground.apis.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChatMessage(
        @NotBlank
        @Pattern(regexp = "user|model", message = "role debe ser 'user' o 'model'")
        String role,

        @NotBlank(message = "text no puede estar vacío")
        String text
) {
}
