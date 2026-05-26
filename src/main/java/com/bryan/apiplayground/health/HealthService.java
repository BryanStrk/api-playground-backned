package com.bryan.apiplayground.health;

import com.bryan.apiplayground.catalog.ApiInfo;
import com.bryan.apiplayground.catalog.CatalogService;
import com.bryan.apiplayground.catalog.KeyRequirement;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Service
public class HealthService {

    private final CatalogService catalogService;
    private final RestClient healthRestClient;
    // Maps api id → whether its REQUIRED key is configured. Only REQUIRED keys are
    // tracked; OPTIONAL ones have working defaults (DEMO_KEY for NASA, "123" for
    // TheSportsDB) so they're never SKIPPED.
    private final Map<String, Boolean> requiredKeyConfigured;

    public HealthService(CatalogService catalogService,
                         @Qualifier("healthRestClient") RestClient healthRestClient,
                         @Value("${tmdb.api-key:}") String tmdbKey,
                         @Value("${gemini.api-key:}") String geminiKey,
                         @Value("${unsplash.access-key:}") String unsplashKey,
                         @Value("${news.api-key:}") String newsKey) {
        this.catalogService = catalogService;
        this.healthRestClient = healthRestClient;
        this.requiredKeyConfigured = Map.of(
                "movies", isSet(tmdbKey),
                "ai", isSet(geminiKey),
                "photos", isSet(unsplashKey),
                "news", isSet(newsKey)
        );
    }

    public HealthReport checkAll() {
        var startedAt = Instant.now();
        var start = System.nanoTime();
        // Virtual threads (Java 25): one thread per upstream means the wall-clock
        // is dominated by the slowest API, not the sum. The try-with-resources on
        // the executor blocks until every task finishes, so totalTimeMs reflects
        // the real fan-out duration.
        List<ApiHealth> results;
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = catalogService.findAll().stream()
                    .map(api -> CompletableFuture.supplyAsync(() -> check(api), executor))
                    .toList();
            results = futures.stream().map(CompletableFuture::join).toList();
        }
        var totalMs = (System.nanoTime() - start) / 1_000_000;
        var up = (int) results.stream().filter(r -> r.status() == HealthStatus.UP).count();
        var down = (int) results.stream().filter(r -> r.status() == HealthStatus.DOWN).count();
        var skipped = (int) results.stream().filter(r -> r.status() == HealthStatus.SKIPPED).count();
        return new HealthReport(startedAt, results.size(), up, down, skipped, totalMs, results);
    }

    private ApiHealth check(ApiInfo api) {
        if (api.requiresKey() == KeyRequirement.REQUIRED
                && !requiredKeyConfigured.getOrDefault(api.id(), Boolean.FALSE)) {
            return new ApiHealth(api.id(), api.name(), api.category(),
                    HealthStatus.SKIPPED, null, 0L, "API key no configurada");
        }
        var start = System.nanoTime();
        try {
            // exchange() returns whatever we extract without throwing on 4xx/5xx,
            // so we can classify the status ourselves instead of catching the
            // RestClientResponseException hierarchy.
            int httpStatus = healthRestClient.get()
                    .uri(api.externalUrl())
                    .exchange((req, res) -> res.getStatusCode().value());
            var elapsed = elapsedMs(start);
            // 404 counts as UP because it means the host answered. Only 5xx and
            // transport failures are real "service is broken" signals.
            if (httpStatus < 500) {
                return new ApiHealth(api.id(), api.name(), api.category(),
                        HealthStatus.UP, httpStatus, elapsed, null);
            }
            return new ApiHealth(api.id(), api.name(), api.category(),
                    HealthStatus.DOWN, httpStatus, elapsed, "Upstream returned " + httpStatus);
        } catch (ResourceAccessException e) {
            return new ApiHealth(api.id(), api.name(), api.category(),
                    HealthStatus.DOWN, null, elapsedMs(start), e.getMessage());
        } catch (Exception e) {
            return new ApiHealth(api.id(), api.name(), api.category(),
                    HealthStatus.DOWN, null, elapsedMs(start), e.getMessage());
        }
    }

    private static long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    private static boolean isSet(String value) {
        return value != null && !value.isBlank();
    }
}
