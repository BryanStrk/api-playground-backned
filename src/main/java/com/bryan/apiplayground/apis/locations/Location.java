package com.bryan.apiplayground.apis.locations;

public record Location(
        int id,
        String name,
        String type,
        String dimension,
        int residentCount
) {
}
