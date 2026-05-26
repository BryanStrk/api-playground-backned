package com.bryan.apiplayground.apis.photos;

public record PhotoResponse(
        String id,
        String description,
        String altDescription,
        String regularUrl,
        String thumbUrl,
        String authorName,
        String authorUsername,
        int width,
        int height
) {
}
