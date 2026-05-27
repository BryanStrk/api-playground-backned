package com.bryan.apiplayground.apis.exchange;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ExchangeService {

    private static final String LATEST_URL =
            "https://api.frankfurter.app/latest?from={from}&to={to}";
    private static final String CURRENCIES_URL =
            "https://api.frankfurter.app/currencies";
    private static final ParameterizedTypeReference<Map<String, String>> STRING_MAP_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    public ExchangeService(RestClient restClient) {
        this.restClient = restClient;
    }

    public ExchangeResponse getLatest(String from, String to) {
        try {
            var raw = restClient.get()
                    .uri(LATEST_URL, from, to)
                    .retrieve()
                    .body(ExchangeResponse.class);
            if (raw == null) {
                throw new ExternalApiException("Frankfurter returned an empty payload", 502);
            }
            return raw;
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "Frankfurter returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "Frankfurter unreachable: " + e.getMessage(), 0, e);
        }
    }

    public List<Currency> listCurrencies() {
        try {
            // The upstream payload is a flat {"CODE": "Name"} map; flatten it
            // into a sorted list so the frontend gets a stable dropdown order.
            var raw = restClient.get()
                    .uri(CURRENCIES_URL)
                    .retrieve()
                    .body(STRING_MAP_TYPE);
            if (raw == null) return List.of();
            var out = new ArrayList<Currency>(raw.size());
            raw.forEach((code, name) -> out.add(new Currency(code, name)));
            out.sort((a, b) -> a.code().compareTo(b.code()));
            return out;
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "Frankfurter returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "Frankfurter unreachable: " + e.getMessage(), 0, e);
        }
    }
}
