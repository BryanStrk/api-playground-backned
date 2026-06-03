package com.bryan.apiplayground.apis.platzi;

import java.util.List;

public record PlatziProduct(
        long id,
        String title,
        double price,
        String description,
        Category category,
        List<String> images
) {
}
