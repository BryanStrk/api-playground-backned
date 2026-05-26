package com.bryan.apiplayground.apis.ai;

public record GenerateResponse(
        String text,
        String model,
        Integer promptTokens,
        Integer responseTokens,
        Integer totalTokens
) {
}
