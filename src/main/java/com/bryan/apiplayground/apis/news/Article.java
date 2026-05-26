package com.bryan.apiplayground.apis.news;

public record Article(
        String source,
        String author,
        String title,
        String description,
        String url,
        String imageUrl,
        String publishedAt
) {
}
