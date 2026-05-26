package com.bryan.apiplayground.apis.crypto;

import java.util.Map;

public record CryptoPriceResponse(Map<String, Map<String, Double>> prices) {
}
