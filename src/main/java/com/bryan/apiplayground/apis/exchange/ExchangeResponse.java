package com.bryan.apiplayground.apis.exchange;

import java.util.Map;

public record ExchangeResponse(
        double amount,
        String base,
        String date,
        Map<String, Double> rates
) {
}
