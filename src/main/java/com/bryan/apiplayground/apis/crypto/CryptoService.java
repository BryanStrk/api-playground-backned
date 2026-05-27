package com.bryan.apiplayground.apis.crypto;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import org.springframework.core.ParameterizedTypeReference;
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

    private final RestClient restClient;

    public CryptoService(RestClient restClient) {
        this.restClient = restClient;
    }

    public CryptoPriceResponse getPrice(String ids, String vs) {
        try {
            var raw = restClient.get()
                    .uri(PRICE_URL, ids, vs)
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

    // CoinGecko's /search payload mixes coins, exchanges, categories, etc.;
    // we only care about coins and let Jackson skip the rest.
    private record SearchRaw(List<CoinSummary> coins) {
    }
}
