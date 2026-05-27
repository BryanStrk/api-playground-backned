package com.bryan.apiplayground.apis.cats;

import com.fasterxml.jackson.annotation.JsonAlias;

public record CatResponse(
        String id,
        @JsonAlias("url") String imageUrl,
        int width,
        int height
) {
}
