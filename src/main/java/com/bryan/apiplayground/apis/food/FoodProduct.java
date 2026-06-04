package com.bryan.apiplayground.apis.food;

public record FoodProduct(
        String barcode,
        String name,
        String brands,
        String imageUrl,
        String nutriscoreGrade,
        Integer novaGroup,
        Nutriments nutriments
) {
}
