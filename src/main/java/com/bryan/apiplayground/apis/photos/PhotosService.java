package com.bryan.apiplayground.apis.photos;

import com.bryan.apiplayground.common.exception.ApiKeyNotConfiguredException;
import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class PhotosService {

    private static final String RANDOM_URL =
            "https://api.unsplash.com/photos/random?query={query}&client_id={key}";

    private final RestClient restClient;
    private final String accessKey;

    public PhotosService(RestClient restClient,
                         @Value("${unsplash.access-key:}") String accessKey) {
        this.restClient = restClient;
        this.accessKey = accessKey;
    }

    public PhotoResponse getRandom(String query) {
        if (accessKey == null || accessKey.isBlank()) {
            throw new ApiKeyNotConfiguredException(
                    "La API key de Unsplash no está configurada. Añade UNSPLASH_ACCESS_KEY a tu .env");
        }
        try {
            var raw = restClient.get()
                    .uri(RANDOM_URL, query, accessKey)
                    .retrieve()
                    .body(UnsplashRaw.class);
            if (raw == null) {
                throw new ExternalApiException("Unsplash returned an empty payload", 502);
            }
            return new PhotoResponse(
                    raw.id(),
                    raw.description(),
                    raw.altDescription(),
                    raw.urls() == null ? null : raw.urls().regular(),
                    raw.urls() == null ? null : raw.urls().thumb(),
                    raw.user() == null ? null : raw.user().name(),
                    raw.user() == null ? null : raw.user().username(),
                    raw.width(),
                    raw.height()
            );
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "Unsplash returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "Unsplash unreachable: " + e.getMessage(), 0, e);
        }
    }

    private record UnsplashRaw(
            String id,
            String description,
            @JsonProperty("alt_description") String altDescription,
            Urls urls,
            User user,
            int width,
            int height
    ) {
    }

    private record Urls(String regular, String thumb) {
    }

    private record User(String name, String username) {
    }
}
