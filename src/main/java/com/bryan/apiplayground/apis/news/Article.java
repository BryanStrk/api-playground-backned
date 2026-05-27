package com.bryan.apiplayground.apis.news;

public record Article(
        String sourceName,
        String author,
        String title,
        String description,
        String url,
        String imageUrl,
        String publishedAt
) {
}
