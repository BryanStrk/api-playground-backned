package com.bryan.apiplayground.apis.balldontlie;

import java.util.List;

public record BdlTier(
        String plan,
        double priceUsd,
        int rateLimitPerMinute,
        List<String> features
) {
}
