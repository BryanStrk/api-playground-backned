package com.bryan.apiplayground.apis.exchange;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class ExchangeService {

    private static final String LATEST_URL =
            "https://api.frankfurter.dev/v1/latest?base={base}&symbols={symbols}";

    private final RestClient restClient;

    public ExchangeService(RestClient restClient) {
        this.restClient = restClient;
    }

    public ExchangeResponse getLatest(String base, String symbols) {
        try {
            var raw = restClient.get()
                    .uri(LATEST_URL, base, symbols)
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
}
