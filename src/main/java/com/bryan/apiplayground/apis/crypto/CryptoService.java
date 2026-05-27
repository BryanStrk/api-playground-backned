package com.bryan.apiplayground.apis.crypto;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Service
public class CryptoService {

    private static final String PRICE_URL =
            "https://api.coingecko.com/api/v3/simple/price?ids={ids}&vs_currencies={vs}";
    private static final String SEARCH_URL =
            "https://api.coingecko.com/api/v3/search?query={q}";
    private static final ParameterizedTypeReference<Map<String, Map<String, Double>>> PRICE_TYPE =
            new ParameterizedTypeReference<>() {
            };
    // Header for the free Demo plan on api.coingecko.com; the Pro plan uses
    // x-cg-pro-api-key against pro-api.coingecko.com.
    private static final String DEMO_KEY_HEADER = "x-cg-demo-api-key";

    private final RestClient restClient;
    private final String apiKey;

    public CryptoService(RestClient restClient,
                         @Value("${coingecko.api-key:}") String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    public CryptoPriceResponse getPrice(String ids, String vs) {
        try {
            var raw = restClient.get()
                    .uri(PRICE_URL, ids, vs)
                    .headers(this::applyKey)
                    .retrieve()
                    .body(PRICE_TYPE);
            return new CryptoPriceResponse(raw == null ? Map.of() : raw);
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "CoinGecko returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "CoinGecko unreachable: " + e.getMessage(), 0, e);
        }
    }

    public List<CoinSummary> search(String query) {
        try {
            var raw = restClient.get()
                    .uri(SEARCH_URL, query)
                    .headers(this::applyKey)
                    .retrieve()
                    .body(SearchRaw.class);
            if (raw == null || raw.coins() == null) return List.of();
            return raw.coins();
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "CoinGecko returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "CoinGecko unreachable: " + e.getMessage(), 0, e);
        }
    }

    private void applyKey(HttpHeaders headers) {
        if (!apiKey.isEmpty()) {
            headers.set(DEMO_KEY_HEADER, apiKey);
        }
    }

    // CoinGecko's /search payload mixes coins, exchanges, categories, etc.;
    // we only care about coins and let Jackson skip the rest.
    private record SearchRaw(List<CoinSummary> coins) {
    }
}
