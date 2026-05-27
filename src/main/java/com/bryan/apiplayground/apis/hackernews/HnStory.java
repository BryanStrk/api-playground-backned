package com.bryan.apiplayground.apis.hackernews;

public record HnStory(
        long id,
        String title,
        String url,
        String by,
        int score,
        int descendants,
        long time
) {
}
