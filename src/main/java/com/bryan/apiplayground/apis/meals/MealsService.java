package com.bryan.apiplayground.apis.meals;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Service
public class MealsService {

    private static final String SEARCH_URL = "https://www.themealdb.com/api/json/v1/1/search.php?s={name}";

    private final RestClient restClient;

    public MealsService(RestClient restClient) {
        this.restClient = restClient;
    }

    public MealsResponse search(String name) {
        try {
            var raw = restClient.get()
                    .uri(SEARCH_URL, name)
                    .retrieve()
                    .body(MealsRaw.class);
            // TheMealDB returns {"meals": null} when there is no match.
            if (raw == null || raw.meals() == null) {
                return new MealsResponse(List.of());
            }
            var meals = raw.meals().stream()
                    .map(m -> new Meal(
                            m.idMeal(),
                            m.strMeal(),
                            m.strCategory(),
                            m.strArea(),
                            m.strInstructions(),
                            m.strMealThumb(),
                            m.strYoutube()))
                    .toList();
            return new MealsResponse(meals);
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "TheMealDB returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "TheMealDB unreachable: " + e.getMessage(), 0, e);
        }
    }

    private record MealsRaw(List<MealRaw> meals) {
    }

    private record MealRaw(
            @JsonProperty("idMeal") String idMeal,
            @JsonProperty("strMeal") String strMeal,
            @JsonProperty("strCategory") String strCategory,
            @JsonProperty("strArea") String strArea,
            @JsonProperty("strInstructions") String strInstructions,
            @JsonProperty("strMealThumb") String strMealThumb,
            @JsonProperty("strYoutube") String strYoutube
    ) {
    }
}
