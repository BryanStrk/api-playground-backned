package com.bryan.apiplayground.apis.balldontlie;

public record BdlStadium(
        int id,
        String name,
        String city,
        String country,
        Integer capacity,
        Double latitude,
        Double longitude
) {
}
