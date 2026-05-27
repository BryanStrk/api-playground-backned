package com.bryan.apiplayground.apis.locations;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Service
public class LocationsService {

    private static final String SEARCH_URL = "https://rickandmortyapi.com/api/location/?name={name}";

    private final RestClient restClient;

    public LocationsService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<Location> search(String query) {
        try {
            var raw = restClient.get()
                    .uri(SEARCH_URL, query)
                    .retrieve()
                    .body(SearchRaw.class);
            if (raw == null || raw.results() == null) return List.of();
            return raw.results().stream()
                    .map(r -> new Location(
                            r.id(),
                            r.name(),
                            r.type(),
                            r.dimension(),
                            r.residents() == null ? 0 : r.residents().size()))
                    .toList();
        } catch (RestClientResponseException e) {
            // No match → R&M returns 404 with {"error": "..."}. Treat as empty.
            if (e.getStatusCode().value() == 404) return List.of();
            throw new ExternalApiException(
                    "Rick and Morty API returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "Rick and Morty API unreachable: " + e.getMessage(), 0, e);
        }
    }

    private record SearchRaw(List<LocationRaw> results) {
    }

    private record LocationRaw(
            int id,
            String name,
            String type,
            String dimension,
            List<String> residents
    ) {
    }
}
