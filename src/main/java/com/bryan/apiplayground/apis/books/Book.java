package com.bryan.apiplayground.apis.books;

public record Book(
        String workKey,
        String title,
        String author,
        Integer year,
        Integer coverId,
        String coverUrl,
        String iaId,
        String ebookAccess
) {
}
