package com.bryan.apiplayground.apis.fakestore;

public record Product(
        long id,
        String title,
        double price,
        String description,
        String category,
        String image,
        Rating rating
) {
}
