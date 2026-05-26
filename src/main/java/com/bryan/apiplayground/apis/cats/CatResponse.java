package com.bryan.apiplayground.apis.cats;

public record CatResponse(
        String id,
        String url,
        int width,
        int height
) {
}
