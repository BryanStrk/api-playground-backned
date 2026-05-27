package com.bryan.apiplayground.apis.countries;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Service
public class CountriesService {

    private static final String BASE_URL = "https://restcountries.com/v3.1/name/{name}";
    private static final ParameterizedTypeReference<List<CountryRaw>> LIST_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    public CountriesService(RestClient restClient) {
        this.restClient = restClient;
    }

    public CountryResponse getByName(String name) {
        try {
            var raw = restClient.get()
                    .uri(BASE_URL, name)
                    .retrieve()
                    .body(LIST_TYPE);
            if (raw == null || raw.isEmpty()) {
                throw new ExternalApiException("REST Countries returned no matches for " + name, 502);
            }
            return toResponse(raw.getFirst());
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "REST Countries returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "REST Countries unreachable: " + e.getMessage(), 0, e);
        }
    }

    private CountryResponse toResponse(CountryRaw c) {
        var common = c.name() == null ? null : c.name().common();
        var official = c.name() == null ? null : c.name().official();
        var capital = c.capital() == null || c.capital().isEmpty() ? null : c.capital().getFirst();
        var languages = c.languages() == null ? List.<String>of() : List.copyOf(c.languages().values());
        // Prefer PNG so <img> tags work everywhere; fall back to SVG only if PNG is missing.
        String flagUrl = null;
        String flagAlt = null;
        if (c.flags() != null) {
            flagUrl = c.flags().png() != null ? c.flags().png() : c.flags().svg();
            flagAlt = c.flags().alt();
        }
        var map = c.maps() == null ? null : c.maps().googleMaps();
        return new CountryResponse(
                common,
                official,
                capital,
                c.region(),
                c.subregion(),
                c.population(),
                c.area(),
                languages,
                flagUrl,
                flagAlt,
                map
        );
    }

    private record CountryRaw(
            CountryName name,
            List<String> capital,
            String region,
            String subregion,
            long population,
            double area,
            Map<String, String> languages,
            Flags flags,
            Maps maps
    ) {
    }

    private record CountryName(String common, String official) {
    }

    private record Flags(String png, String svg, String alt) {
    }

    private record Maps(@JsonProperty("googleMaps") String googleMaps) {
    }
}
