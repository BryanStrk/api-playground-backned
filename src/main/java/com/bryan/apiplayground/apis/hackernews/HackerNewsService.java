package com.bryan.apiplayground.apis.hackernews;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Service
public class HackerNewsService {

    private static final String IDS_URL =
            "https://hacker-news.firebaseio.com/v0/{type}stories.json";
    private static final String ITEM_URL =
            "https://hacker-news.firebaseio.com/v0/item/{id}.json";
    private static final Set<String> ALLOWED_TYPES = Set.of("top", "new", "best");
    private static final String DEFAULT_TYPE = "top";
    private static final int MAX_LIMIT = 50;
    private static final int DEFAULT_LIMIT = 20;
    private static final ParameterizedTypeReference<List<Long>> ID_LIST =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    public HackerNewsService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<HnStory> getStories(String type, int limit) {
        var effectiveType = ALLOWED_TYPES.contains(type) ? type : DEFAULT_TYPE;
        var effectiveLimit = Math.clamp(limit, 1, MAX_LIMIT);
        var ids = fetchIds(effectiveType);
        if (ids.isEmpty()) return List.of();
        var slice = ids.subList(0, Math.min(effectiveLimit, ids.size()));
        // Fan-out on virtual threads: hitting /item N times sequentially over a
        // remote host is roughly N*RTT seconds; in parallel it stays at ~1*RTT.
        // Same pattern HealthService uses for the 20-api ping.
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = slice.stream()
                    .map(id -> CompletableFuture.supplyAsync(() -> fetchItem(id), executor))
                    .toList();
            return futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .toList();
        }
    }

    private List<Long> fetchIds(String type) {
        try {
            var raw = restClient.get()
                    .uri(IDS_URL, type)
                    .retrieve()
                    .body(ID_LIST);
            return raw == null ? List.of() : raw;
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "Hacker News returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "Hacker News unreachable: " + e.getMessage(), 0, e);
        }
    }

    // Items can be null (deleted/dead posts) or partially populated (Ask HN
    // entries with no url, jobs with no descendants…). Return null on failure
    // so the parallel batch filters it out instead of blowing up the whole call.
    private HnStory fetchItem(long id) {
        try {
            var item = restClient.get()
                    .uri(ITEM_URL, id)
                    .retrieve()
                    .body(HnItemRaw.class);
            if (item == null || item.id() == null) return null;
            return new HnStory(
                    item.id(),
                    item.title(),
                    item.url(),
                    item.by(),
                    item.score() == null ? 0 : item.score(),
                    item.descendants() == null ? 0 : item.descendants(),
                    item.time() == null ? 0L : item.time()
            );
        } catch (Exception e) {
            return null;
        }
    }

    private record HnItemRaw(
            Long id,
            String title,
            String url,
            String by,
            Integer score,
            Integer descendants,
            Long time
    ) {
    }
}
