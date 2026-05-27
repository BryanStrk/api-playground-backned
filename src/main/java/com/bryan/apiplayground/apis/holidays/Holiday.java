package com.bryan.apiplayground.apis.holidays;

import java.util.List;

public record Holiday(
        String date,
        String localName,
        String name,
        List<String> types,
        String countryCode
) {
}
