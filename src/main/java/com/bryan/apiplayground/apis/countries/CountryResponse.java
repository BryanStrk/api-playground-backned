package com.bryan.apiplayground.apis.countries;

import java.util.List;

public record CountryResponse(
        String commonName,
        String officialName,
        String capital,
        String region,
        String subregion,
        long population,
        double area,
        List<String> languages,
        String flagUrl,
        String mapUrl
) {
}
