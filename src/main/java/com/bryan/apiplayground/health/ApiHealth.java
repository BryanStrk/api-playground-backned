package com.bryan.apiplayground.health;

public record ApiHealth(
        String id,
        String name,
        String category,
        HealthStatus status,
        Integer httpStatus,
        long responseTimeMs,
        String error
) {
}
