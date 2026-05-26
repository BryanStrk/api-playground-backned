package com.bryan.apiplayground.apis.books;

import java.util.List;

public record Book(
        String key,
        String title,
        List<String> authors,
        Integer firstPublishYear,
        String coverUrl
) {
}
