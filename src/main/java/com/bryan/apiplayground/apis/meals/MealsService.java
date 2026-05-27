package com.bryan.apiplayground.apis.meals;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MealsService {

    private static final String SEARCH_URL =
            "https://www.themealdb.com/api/json/v1/1/search.php?s={q}";
    private static final String CATEGORIES_URL =
            "https://www.themealdb.com/api/json/v1/1/categories.php";
    private static final String FILTER_URL =
            "https://www.themealdb.com/api/json/v1/1/filter.php?c={category}";
    // TheMealDB has had 20 ingredient/measure slots for years; the schema is stable.
    private static final int MAX_INGREDIENT_SLOTS = 20;
    // The meal record carries every str*1..20 column; deserializing into a
    // Map<String,Object> dodges a 40-field record without losing type safety
    // on the hand-mapped fields below.
    private static final ParameterizedTypeReference<MealsRawMap> MEALS_RAW_MAP =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    public MealsService(RestClient restClient) {
        this.restClient = restClient;
    }

    public MealsResponse search(String q) {
        try {
            var raw = restClient.get()
                    .uri(SEARCH_URL, q)
                    .retrieve()
                    .body(MEALS_RAW_MAP);
            // TheMealDB returns {"meals": null} when there is no match.
            if (raw == null || raw.meals() == null) {
                return new MealsResponse(List.of());
            }
            var meals = raw.meals().stream().map(MealsService::toMeal).toList();
            return new MealsResponse(meals);
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "TheMealDB returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "TheMealDB unreachable: " + e.getMessage(), 0, e);
        }
    }

    public List<MealCategory> categories() {
        try {
            var raw = restClient.get()
                    .uri(CATEGORIES_URL)
                    .retrieve()
                    .body(CategoriesRaw.class);
            if (raw == null || raw.categories() == null) return List.of();
            return raw.categories().stream()
                    .map(c -> new MealCategory(
                            c.idCategory(),
                            c.strCategory(),
                            c.strCategoryThumb(),
                            c.strCategoryDescription()))
                    .toList();
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "TheMealDB returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "TheMealDB unreachable: " + e.getMessage(), 0, e);
        }
    }

    public List<MealSummary> filterByCategory(String category) {
        try {
            var raw = restClient.get()
                    .uri(FILTER_URL, category)
                    .retrieve()
                    .body(FilterRaw.class);
            // filter.php returns {"meals": null} for empty filters too.
            if (raw == null || raw.meals() == null) return List.of();
            return raw.meals().stream()
                    .map(m -> new MealSummary(m.idMeal(), m.strMeal(), m.strMealThumb()))
                    .toList();
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "TheMealDB returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "TheMealDB unreachable: " + e.getMessage(), 0, e);
        }
    }

    private static Meal toMeal(Map<String, Object> m) {
        return new Meal(
                str(m, "idMeal"),
                str(m, "strMeal"),
                str(m, "strCategory"),
                str(m, "strArea"),
                str(m, "strInstructions"),
                str(m, "strMealThumb"),
                str(m, "strYoutube"),
                extractIngredients(m)
        );
    }

    private static List<Ingredient> extractIngredients(Map<String, Object> m) {
        var out = new ArrayList<Ingredient>();
        for (int i = 1; i <= MAX_INGREDIENT_SLOTS; i++) {
            var name = str(m, "strIngredient" + i);
            if (name == null || name.isBlank()) continue;
            var measure = str(m, "strMeasure" + i);
            out.add(new Ingredient(
                    name.trim(),
                    (measure == null || measure.isBlank()) ? null : measure.trim()));
        }
        return out;
    }

    private static String str(Map<String, Object> m, String key) {
        var v = m.get(key);
        return v == null ? null : v.toString();
    }

    private record MealsRawMap(List<Map<String, Object>> meals) {
    }

    private record CategoriesRaw(List<CategoryRaw> categories) {
    }

    private record CategoryRaw(
            @JsonProperty("idCategory") String idCategory,
            @JsonProperty("strCategory") String strCategory,
            @JsonProperty("strCategoryThumb") String strCategoryThumb,
            @JsonProperty("strCategoryDescription") String strCategoryDescription
    ) {
    }

    private record FilterRaw(List<FilterMealRaw> meals) {
    }

    private record FilterMealRaw(
            @JsonProperty("idMeal") String idMeal,
            @JsonProperty("strMeal") String strMeal,
            @JsonProperty("strMealThumb") String strMealThumb
    ) {
    }
}
