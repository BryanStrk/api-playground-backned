package com.bryan.apiplayground.health;

import java.time.Instant;
import java.util.List;

public record HealthReport(
        Instant checkedAt,
        int total,
        int up,
        int down,
        int skipped,
        long totalTimeMs,
        List<ApiHealth> results
) {
}
