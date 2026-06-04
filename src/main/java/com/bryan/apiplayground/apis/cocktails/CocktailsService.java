package com.bryan.apiplayground.apis.cocktails;

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
public class CocktailsService {

    private static final String SEARCH_URL =
            "https://www.thecocktaildb.com/api/json/v1/1/search.php?s={q}";
    private static final String RANDOM_URL =
            "https://www.thecocktaildb.com/api/json/v1/1/random.php";
    private static final String CATEGORIES_URL =
            "https://www.thecocktaildb.com/api/json/v1/1/list.php?c=list";
    private static final String FILTER_CATEGORY_URL =
            "https://www.thecocktaildb.com/api/json/v1/1/filter.php?c={c}";
    private static final String FILTER_ALCOHOLIC_URL =
            "https://www.thecocktaildb.com/api/json/v1/1/filter.php?a={a}";
    private static final String LOOKUP_URL =
            "https://www.thecocktaildb.com/api/json/v1/1/lookup.php?i={id}";
    // TheCocktailDB has had 15 ingredient/measure slots for years; the schema is stable.
    private static final int MAX_INGREDIENT_SLOTS = 15;
    // The drink record carries every str*1..15 column; deserializing into a
    // Map<String,Object> dodges a 30-field record without losing type safety
    // on the hand-mapped fields below.
    private static final ParameterizedTypeReference<CocktailsRawMap> COCKTAILS_RAW_MAP =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    public CocktailsService(RestClient restClient) {
        this.restClient = restClient;
    }

    public CocktailsResponse search(String name, String category) {
        try {
            var raw = restClient.get()
                    .uri(SEARCH_URL, name == null ? "" : name)
                    .retrieve()
                    .body(COCKTAILS_RAW_MAP);
            // TheCocktailDB returns {"drinks": null} when there is no match.
            if (raw == null || raw.drinks() == null) {
                return new CocktailsResponse(List.of());
            }
            var cocktails = raw.drinks().stream().map(CocktailsService::toCocktail);
            // search.php only filters by name, so honor ?category= in memory.
            if (category != null && !category.isBlank()) {
                cocktails = cocktails.filter(c -> category.equalsIgnoreCase(c.category()));
            }
            return new CocktailsResponse(cocktails.toList());
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "TheCocktailDB returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "TheCocktailDB unreachable: " + e.getMessage(), 0, e);
        }
    }

    public CocktailsResponse random() {
        try {
            var raw = restClient.get()
                    .uri(RANDOM_URL)
                    .retrieve()
                    .body(COCKTAILS_RAW_MAP);
            if (raw == null || raw.drinks() == null || raw.drinks().isEmpty()) {
                return new CocktailsResponse(List.of());
            }
            return new CocktailsResponse(List.of(toCocktail(raw.drinks().getFirst())));
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "TheCocktailDB returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "TheCocktailDB unreachable: " + e.getMessage(), 0, e);
        }
    }

    public CocktailsResponse byId(String id) {
        try {
            var raw = restClient.get()
                    .uri(LOOKUP_URL, id)
                    .retrieve()
                    .body(COCKTAILS_RAW_MAP);
            // lookup.php returns {"drinks": null} for an unknown id.
            if (raw == null || raw.drinks() == null || raw.drinks().isEmpty()) {
                return new CocktailsResponse(List.of());
            }
            return new CocktailsResponse(List.of(toCocktail(raw.drinks().getFirst())));
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "TheCocktailDB returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "TheCocktailDB unreachable: " + e.getMessage(), 0, e);
        }
    }

    public List<CocktailCategory> categories() {
        try {
            var raw = restClient.get()
                    .uri(CATEGORIES_URL)
                    .retrieve()
                    .body(CategoriesRaw.class);
            if (raw == null || raw.drinks() == null) return List.of();
            return raw.drinks().stream()
                    .map(c -> new CocktailCategory(c.strCategory()))
                    .toList();
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "TheCocktailDB returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "TheCocktailDB unreachable: " + e.getMessage(), 0, e);
        }
    }

    public CocktailsResponse filterByCategory(String category) {
        try {
            var raw = restClient.get()
                    .uri(FILTER_CATEGORY_URL, category)
                    .retrieve()
                    .body(FilterRaw.class);
            // filter.php returns {"drinks": null} for empty filters too.
            if (raw == null || raw.drinks() == null) return new CocktailsResponse(List.of());
            // filter.php only carries id/name/thumb; wrap as Cocktail so the
            // payload matches search() ({"cocktails": [...]}). Detail fields stay
            // null and ingredients empty — call /cocktails/{id} for the full drink.
            var cocktails = raw.drinks().stream()
                    .map(d -> new Cocktail(
                            d.idDrink(), d.strDrink(), d.strDrinkThumb(),
                            null, null, null, null, List.of()))
                    .toList();
            return new CocktailsResponse(cocktails);
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "TheCocktailDB returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "TheCocktailDB unreachable: " + e.getMessage(), 0, e);
        }
    }

    public CocktailsResponse filterByAlcoholic(String alcoholic) {
        try {
            var raw = restClient.get()
                    .uri(FILTER_ALCOHOLIC_URL, alcoholic)
                    .retrieve()
                    .body(FilterRaw.class);
            if (raw == null || raw.drinks() == null) return new CocktailsResponse(List.of());
            var cocktails = raw.drinks().stream()
                    .map(d -> new Cocktail(
                            d.idDrink(), d.strDrink(), d.strDrinkThumb(),
                            null, null, null, null, List.of()))
                    .toList();
            return new CocktailsResponse(cocktails);
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "TheCocktailDB returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "TheCocktailDB unreachable: " + e.getMessage(), 0, e);
        }
    }

    private static Cocktail toCocktail(Map<String, Object> d) {
        return new Cocktail(
                str(d, "idDrink"),
                str(d, "strDrink"),
                str(d, "strDrinkThumb"),
                str(d, "strCategory"),
                str(d, "strAlcoholic"),
                str(d, "strGlass"),
                str(d, "strInstructions"),
                extractIngredients(d)
        );
    }

    private static List<Ingredient> extractIngredients(Map<String, Object> d) {
        var out = new ArrayList<Ingredient>();
        for (int i = 1; i <= MAX_INGREDIENT_SLOTS; i++) {
            var name = str(d, "strIngredient" + i);
            if (name == null || name.isBlank()) continue;
            var measure = str(d, "strMeasure" + i);
            out.add(new Ingredient(
                    name.trim(),
                    (measure == null || measure.isBlank()) ? null : measure.trim()));
        }
        return out;
    }

    private static String str(Map<String, Object> d, String key) {
        var v = d.get(key);
        return v == null ? null : v.toString();
    }

    private record CocktailsRawMap(List<Map<String, Object>> drinks) {
    }

    private record CategoriesRaw(List<CategoryRaw> drinks) {
    }

    private record CategoryRaw(
            @JsonProperty("strCategory") String strCategory
    ) {
    }

    private record FilterRaw(List<FilterDrinkRaw> drinks) {
    }

    private record FilterDrinkRaw(
            @JsonProperty("idDrink") String idDrink,
            @JsonProperty("strDrink") String strDrink,
            @JsonProperty("strDrinkThumb") String strDrinkThumb
    ) {
    }
}
