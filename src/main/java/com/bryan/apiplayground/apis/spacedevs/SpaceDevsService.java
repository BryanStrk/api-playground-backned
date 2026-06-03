package com.bryan.apiplayground.apis.spacedevs;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Service
public class SpaceDevsService {

    // Production base. The Launch Library 2 API limits anonymous callers to
    // 15 requests/hour per IP, so the response MUST be cached (see TTL below) or
    // a public dashboard would 429 almost immediately. The dev mirror
    // (lldev.thespacedevs.com) has no limit but serves stale data — used only for
    // the health ping (see CatalogService.externalUrl), never for live data here.
    // mode=detailed (not list) is required: the lightweight list mode omits
    // launch_service_provider and pad, which the response contract needs. The
    // 12-minute cache keeps the heavier payload off the rate-limited hot path.
    private static final String UPCOMING_URL =
            "https://ll.thespacedevs.com/2.3.0/launches/upcoming/?mode=detailed&limit=10";
    // 12-minute window → at most 5 upstream calls/hour even under constant load,
    // comfortably inside the 15/hour cap.
    private static final long TTL_MS = 12 * 60 * 1000L;

    private final RestClient restClient;
    // A single shared snapshot per JVM keeps the hot path off the network.
    // volatile + synchronized refresh mirrors WorldcupService's cache.
    private volatile Snapshot snapshot;

    public SpaceDevsService(RestClient restClient) {
        this.restClient = restClient;
    }

    public LaunchesResponse getUpcomingLaunches() {
        var current = snapshot;
        if (current != null && System.currentTimeMillis() - current.fetchedAt() < TTL_MS) {
            return current.response();
        }
        return refresh().response();
    }

    private synchronized Snapshot refresh() {
        // Double-check: a concurrent caller may have refreshed while we waited on
        // the monitor — don't burn one of the 15 hourly requests needlessly.
        var current = snapshot;
        if (current != null && System.currentTimeMillis() - current.fetchedAt() < TTL_MS) {
            return current;
        }
        snapshot = fetch();
        return snapshot;
    }

    private Snapshot fetch() {
        try {
            var raw = restClient.get()
                    .uri(UPCOMING_URL)
                    .retrieve()
                    .body(LaunchesRaw.class);
            if (raw == null || raw.results() == null) {
                return new Snapshot(new LaunchesResponse(0, List.of()), System.currentTimeMillis());
            }
            var launches = raw.results().stream().map(SpaceDevsService::toLaunch).toList();
            var response = new LaunchesResponse(raw.count(), launches);
            return new Snapshot(response, System.currentTimeMillis());
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "The Space Devs returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "The Space Devs unreachable: " + e.getMessage(), 0, e);
        }
    }

    private static Launch toLaunch(LaunchRaw raw) {
        return new Launch(
                raw.id(),
                raw.name(),
                raw.net(),
                raw.status() == null ? null : raw.status().name(),
                raw.provider() == null ? null : raw.provider().name(),
                raw.pad() == null ? null : raw.pad().name(),
                raw.pad() == null || raw.pad().location() == null ? null : raw.pad().location().name(),
                raw.image() == null ? null : raw.image().imageUrl()
        );
    }

    private record Snapshot(LaunchesResponse response, long fetchedAt) {
    }

    private record LaunchesRaw(int count, List<LaunchRaw> results) {
    }

    private record LaunchRaw(
            String id,
            String name,
            String net,
            StatusRaw status,
            @JsonProperty("launch_service_provider") ProviderRaw provider,
            PadRaw pad,
            ImageRaw image
    ) {
    }

    private record StatusRaw(String name) {
    }

    private record ProviderRaw(String name) {
    }

    private record PadRaw(String name, LocationRaw location) {
    }

    private record LocationRaw(String name) {
    }

    // In LL2 v2.3.0 the launch image is an object, not a plain URL string.
    private record ImageRaw(@JsonProperty("image_url") String imageUrl) {
    }
}
