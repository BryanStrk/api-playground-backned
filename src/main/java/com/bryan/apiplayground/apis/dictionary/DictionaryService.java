package com.bryan.apiplayground.apis.dictionary;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Service
public class DictionaryService {

    private static final String BASE_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/{word}";
    private static final ParameterizedTypeReference<List<DictionaryEntry>> LIST_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    public DictionaryService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<DictionaryEntry> lookup(String word) {
        try {
            var entries = restClient.get()
                    .uri(BASE_URL, word)
                    .retrieve()
                    .body(LIST_TYPE);
            return entries == null ? List.of() : entries;
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "Free Dictionary returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "Free Dictionary unreachable: " + e.getMessage(), 0, e);
        }
    }
}
