package com.bryan.apiplayground.apis.cats;

import com.bryan.apiplayground.common.exception.ExternalApiException;
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
    private static final ParameterizedTypeReference<List<CatResponse>> LIST_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;
    private final String apiKey;

    public CatsService(RestClient restClient, @Value("${cat.api-key:}") String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    public CatResponse getRandom() {
        try {
            var raw = restClient.get()
                    .uri(SEARCH_URL)
                    .headers(h -> {
                        if (apiKey != null && !apiKey.isBlank()) {
                            h.add("x-api-key", apiKey);
                        }
                    })
                    .retrieve()
                    .body(LIST_TYPE);
            if (raw == null || raw.isEmpty()) {
                throw new ExternalApiException("The Cat API returned an empty payload", 502);
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
}
