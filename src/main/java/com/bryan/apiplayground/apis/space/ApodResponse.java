package com.bryan.apiplayground.apis.space;

public record ApodResponse(
        String date,
        String title,
        String explanation,
        String imageUrl,
        String videoUrl,
        String hdUrl,
        String mediaType,
        String copyright
) {
}
