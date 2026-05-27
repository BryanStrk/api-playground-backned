package com.bryan.apiplayground.apis.cats;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Service
public class CatsService {

    private static final String SEARCH_URL = "https://api.thecatapi.com/v1/images/search";
    private static final String SEARCH_BY_BREED_URL =
            "https://api.thecatapi.com/v1/images/search?breed_ids={breed}";
    private static final String BREEDS_URL = "https://api.thecatapi.com/v1/breeds";
    private static final ParameterizedTypeReference<List<CatResponse>> CAT_LIST_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<List<BreedRaw>> BREED_LIST_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;
    private final String apiKey;

    public CatsService(RestClient restClient, @Value("${cat.api-key:}") String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    public CatResponse getRandom(String breed) {
        try {
            var spec = (breed == null || breed.isBlank())
                    ? restClient.get().uri(SEARCH_URL)
                    : restClient.get().uri(SEARCH_BY_BREED_URL, breed);
            var raw = spec
                    .headers(h -> {
                        if (apiKey != null && !apiKey.isBlank()) {
                            h.add("x-api-key", apiKey);
                        }
                    })
                    .retrieve()
                    .body(CAT_LIST_TYPE);
            if (raw == null || raw.isEmpty()) {
                throw new ExternalApiException(
                        breed == null || breed.isBlank()
                                ? "The Cat API returned an empty payload"
                                : "The Cat API has no images for breed '" + breed + "'",
                        502);
            }
            return raw.getFirst();
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "The Cat API returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "The Cat API unreachable: " + e.getMessage(), 0, e);
        }
    }

    public List<CatBreed> listBreeds() {
        try {
            var raw = restClient.get()
                    .uri(BREEDS_URL)
                    .headers(h -> {
                        if (apiKey != null && !apiKey.isBlank()) {
                            h.add("x-api-key", apiKey);
                        }
                    })
                    .retrieve()
                    .body(BREED_LIST_TYPE);
            if (raw == null) return List.of();
            return raw.stream()
                    .map(b -> new CatBreed(b.id(), b.name(), b.description(),
                            b.temperament(), b.origin(), parseYears(b.lifeSpan())))
                    .toList();
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "The Cat API returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "The Cat API unreachable: " + e.getMessage(), 0, e);
        }
    }

    // The Cat API ships life_span as a free-form string like "12 - 15"; pick the
    // upper bound so the frontend has one integer to render.
    private static Integer parseYears(String lifeSpan) {
        if (lifeSpan == null || lifeSpan.isBlank()) return null;
        try {
            var parts = lifeSpan.replace(" ", "").split("-");
            return Integer.parseInt(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private record BreedRaw(
            String id,
            String name,
            String description,
            String temperament,
            String origin,
            @JsonProperty("life_span") String lifeSpan
    ) {
    }
}
