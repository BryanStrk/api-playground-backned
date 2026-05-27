package com.bryan.apiplayground.apis.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ChatRequest(
        @NotEmpty(message = "messages no puede estar vacío")
        List<@Valid ChatMessage> messages
) {
}
