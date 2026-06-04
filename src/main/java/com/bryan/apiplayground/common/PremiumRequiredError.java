package com.bryan.apiplayground.common;

import java.time.Instant;

public record PremiumRequiredError(
        int status,
        String message,
        String path,
        Instant timestamp,
        String requiredPlan,
        String feature
) {

    public static PremiumRequiredError of(int status, String message, String path,
                                          String requiredPlan, String feature) {
        return new PremiumRequiredError(status, message, path, Instant.now(), requiredPlan, feature);
    }
}
