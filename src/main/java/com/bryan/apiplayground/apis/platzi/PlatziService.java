package com.bryan.apiplayground.apis.platzi;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class PlatziService {

    private static final String PRODUCTS_URL = "https://api.escuelajs.co/api/v1/products";
    private static final String PRODUCT_BY_ID_URL = "https://api.escuelajs.co/api/v1/products/{id}";
    private static final String CATEGORIES_URL = "https://api.escuelajs.co/api/v1/categories";

    // Platzi's seed data stores image URLs inside noisy strings: a single slot
    // often arrives as ["https://i.imgur.com/x.jpg"] (a JSON array embedded in a
    // string) or with escaped quotes/brackets. Pulling the first http(s) run that
    // stops at any quote/bracket/whitespace recovers the real URL and drops the junk.
    private static final Pattern HTTP_URL = Pattern.compile("https?://[^\"\\\\\\[\\]\\s]+");

    private final RestClient restClient;

    public PlatziService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<PlatziProduct> getProducts() {
        try {
            var raw = restClient.get()
                    .uri(PRODUCTS_URL)
                    .retrieve()
                    .body(PlatziProductRaw[].class);
            if (raw == null) return List.of();
            return List.of(raw).stream().map(PlatziService::toProduct).toList();
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "Platzi returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "Platzi unreachable: " + e.getMessage(), 0, e);
        }
    }

    public PlatziProduct getProduct(long id) {
        try {
            var raw = restClient.get()
                    .uri(PRODUCT_BY_ID_URL, id)
                    .retrieve()
                    .body(PlatziProductRaw.class);
            return raw == null ? null : toProduct(raw);
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "Platzi returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "Platzi unreachable: " + e.getMessage(), 0, e);
        }
    }

    public List<Category> getCategories() {
        try {
            var raw = restClient.get()
                    .uri(CATEGORIES_URL)
                    .retrieve()
                    .body(CategoryRaw[].class);
            if (raw == null) return List.of();
            return List.of(raw).stream().map(PlatziService::toCategory).toList();
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "Platzi returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "Platzi unreachable: " + e.getMessage(), 0, e);
        }
    }

    private static PlatziProduct toProduct(PlatziProductRaw raw) {
        var images = raw.images() == null ? List.<String>of()
                : raw.images().stream()
                        .map(PlatziService::sanitizeUrl)
                        .filter(s -> s != null)
                        .toList();
        return new PlatziProduct(
                raw.id(),
                raw.title(),
                raw.price(),
                raw.description(),
                raw.category() == null ? null : toCategory(raw.category()),
                images
        );
    }

    private static Category toCategory(CategoryRaw raw) {
        return new Category(raw.id(), raw.name(), sanitizeUrl(raw.image()));
    }

    // Returns the first well-formed http(s) URL inside a possibly-broken string,
    // or null when there is none so the caller can drop the slot.
    private static String sanitizeUrl(String raw) {
        if (raw == null || raw.isBlank()) return null;
        var matcher = HTTP_URL.matcher(raw);
        return matcher.find() ? matcher.group() : null;
    }

    private record PlatziProductRaw(
            long id,
            String title,
            double price,
            String description,
            CategoryRaw category,
            List<String> images
    ) {
    }

    private record CategoryRaw(
            long id,
            String name,
            String image
    ) {
    }
}
