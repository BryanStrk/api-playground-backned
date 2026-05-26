package com.bryan.apiplayground.apis.books;

import java.util.List;

public record BooksResponse(int numFound, List<Book> books) {
}
