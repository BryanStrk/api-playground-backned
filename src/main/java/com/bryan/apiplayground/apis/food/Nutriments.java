package com.bryan.apiplayground.apis.food;

public record Nutriments(
        Double energyKcal100g,
        Double fat100g,
        Double sugars100g,
        Double salt100g,
        Double proteins100g
) {
}
