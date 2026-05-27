package com.bryan.apiplayground.apis.space;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class SpaceService {

    private static final String APOD_BASE = "https://api.nasa.gov/planetary/apod";

    private final RestClient restClient;
    private final String apiKey;

    public SpaceService(RestClient restClient,
                        @Value("${nasa.api-key:DEMO_KEY}") String apiKey) {
        this.restClient = restClient;
        // NASA_API_KEY is OPTIONAL: empty falls back to the public DEMO_KEY (rate-limited).
        this.apiKey = (apiKey == null || apiKey.isBlank()) ? "DEMO_KEY" : apiKey;
    }

    public ApodResponse getApod(String date) {
        var url = UriComponentsBuilder.fromUriString(APOD_BASE)
                .queryParam("api_key", apiKey);
        if (date != null && !date.isBlank()) {
            url.queryParam("date", date);
        }
        try {
            var raw = restClient.get()
                    .uri(url.build(false).toUriString())
                    .retrieve()
                    .body(ApodRaw.class);
            if (raw == null) {
                throw new ExternalApiException("NASA APOD returned an empty payload", 502);
            }
            return toApodResponse(raw);
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "NASA APOD returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "NASA APOD unreachable: " + e.getMessage(), 0, e);
        }
    }

    public ApodResponse getRandomApod() {
        // count=N flips the response from a single object to an array; we ask
        // for one entry and unwrap it so callers see the same shape as /apod.
        var url = UriComponentsBuilder.fromUriString(APOD_BASE)
                .queryParam("api_key", apiKey)
                .queryParam("count", 1)
                .build(false)
                .toUriString();
        try {
            var raw = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(ApodRaw[].class);
            if (raw == null || raw.length == 0) {
                throw new ExternalApiException("NASA APOD returned an empty payload", 502);
            }
            return toApodResponse(raw[0]);
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "NASA APOD returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "NASA APOD unreachable: " + e.getMessage(), 0, e);
        }
    }

    // APOD ships every asset URL in the single `url` field; whether it's a
    // YouTube/Vimeo embed or a JPEG depends on media_type. Split here so the
    // frontend can use <iframe src={videoUrl}> or <img src={imageUrl}>
    // without checking mediaType again.
    private static ApodResponse toApodResponse(ApodRaw raw) {
        var isVideo = "video".equalsIgnoreCase(raw.mediaType());
        return new ApodResponse(
                raw.date(),
                raw.title(),
                raw.explanation(),
                isVideo ? null : raw.url(),
                isVideo ? raw.url() : null,
                raw.hdurl(),
                raw.mediaType(),
                raw.copyright()
        );
    }

    private record ApodRaw(
            String date,
            String title,
            String explanation,
            String url,
            @JsonProperty("hdurl") String hdurl,
            @JsonProperty("media_type") String mediaType,
            String copyright
    ) {
    }
}
