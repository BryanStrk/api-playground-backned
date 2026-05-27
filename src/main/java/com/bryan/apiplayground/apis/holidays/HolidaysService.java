package com.bryan.apiplayground.apis.holidays;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Service
public class HolidaysService {

    private static final String HOLIDAYS_URL =
            "https://date.nager.at/api/v3/PublicHolidays/{year}/{country}";
    private static final String COUNTRIES_URL =
            "https://date.nager.at/api/v3/AvailableCountries";
    private static final ParameterizedTypeReference<List<Holiday>> HOLIDAY_LIST =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<List<Country>> COUNTRY_LIST =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    public HolidaysService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<Holiday> list(int year, String country) {
        try {
            var raw = restClient.get()
                    .uri(HOLIDAYS_URL, year, country.toUpperCase())
                    .retrieve()
                    .body(HOLIDAY_LIST);
            return raw == null ? List.of() : raw;
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "Nager.Date returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "Nager.Date unreachable: " + e.getMessage(), 0, e);
        }
    }

    public List<Country> listCountries() {
        try {
            var raw = restClient.get()
                    .uri(COUNTRIES_URL)
                    .retrieve()
                    .body(COUNTRY_LIST);
            return raw == null ? List.of() : raw;
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "Nager.Date returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "Nager.Date unreachable: " + e.getMessage(), 0, e);
        }
    }
}
