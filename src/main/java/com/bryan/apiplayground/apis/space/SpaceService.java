package com.bryan.apiplayground.apis.space;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class SpaceService {

    private static final String APOD_URL =
            "https://api.nasa.gov/planetary/apod?api_key={apiKey}";

    private final RestClient restClient;
    private final String apiKey;

    public SpaceService(RestClient restClient,
                        @Value("${nasa.api-key:DEMO_KEY}") String apiKey) {
        this.restClient = restClient;
        // NASA_API_KEY is OPTIONAL: empty falls back to the public DEMO_KEY (rate-limited).
        this.apiKey = (apiKey == null || apiKey.isBlank()) ? "DEMO_KEY" : apiKey;
    }

    public ApodResponse getApod() {
        try {
            var raw = restClient.get()
                    .uri(APOD_URL, apiKey)
                    .retrieve()
                    .body(ApodResponse.class);
            if (raw == null) {
                throw new ExternalApiException("NASA APOD returned an empty payload", 502);
            }
            return raw;
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "NASA APOD returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "NASA APOD unreachable: " + e.getMessage(), 0, e);
        }
    }
}
