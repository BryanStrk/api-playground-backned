package com.bryan.apiplayground.apis.movies;

public record TrailerResponse(
        String youtubeKey,
        String youtubeUrl,
        String name
) {
}
