package com.bryan.apiplayground.apis.food;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Service
public class FoodService {

    private static final String PRODUCT_URL =
            "https://world.openfoodfacts.org/api/v2/product/{barcode}.json";
    private static final String SEARCH_URL =
            "https://world.openfoodfacts.org/cgi/search.pl?search_terms={q}&json=1&page_size=10";
    // Open Food Facts rate-limits/blocks requests sent with a generic User-Agent.
    // Their docs require a descriptive one identifying the app, so we override the
    // shared RestClient's default header on every Open Food Facts call.
    private static final String USER_AGENT = "APIPlayground/1.0 - educational";

    private final RestClient restClient;

    public FoodService(RestClient restClient) {
        this.restClient = restClient;
    }

    public FoodProduct getProduct(String barcode) {
        try {
            var raw = restClient.get()
                    .uri(PRODUCT_URL, barcode)
                    .header(HttpHeaders.USER_AGENT, USER_AGENT)
                    .retrieve()
                    .body(ProductRaw.class);
            // status == 0 (or a null body/product) means "barcode not found".
            if (raw == null || raw.status() == 0 || raw.product() == null) {
                return null;
            }
            return toProduct(barcode, raw.product());
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "Open Food Facts returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "Open Food Facts unreachable: " + e.getMessage(), 0, e);
        }
    }

    public List<FoodProduct> search(String query) {
        try {
            var raw = restClient.get()
                    .uri(SEARCH_URL, query)
                    .header(HttpHeaders.USER_AGENT, USER_AGENT)
                    .retrieve()
                    .body(SearchRaw.class);
            if (raw == null || raw.products() == null) {
                return List.of();
            }
            return raw.products().stream()
                    .map(p -> toProduct(p.code(), p))
                    .toList();
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "Open Food Facts returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "Open Food Facts unreachable: " + e.getMessage(), 0, e);
        }
    }

    private static FoodProduct toProduct(String barcode, ProductData p) {
        // The search endpoint carries the barcode on the product itself; the
        // single-product endpoint doesn't, so fall back to the requested one.
        var code = (p.code() != null && !p.code().isBlank()) ? p.code() : barcode;
        return new FoodProduct(
                code,
                p.productName(),
                p.brands(),
                p.imageUrl(),
                p.nutriscoreGrade(),
                p.novaGroup(),
                p.nutriments() == null ? null : new Nutriments(
                        p.nutriments().energyKcal100g(),
                        p.nutriments().fat100g(),
                        p.nutriments().sugars100g(),
                        p.nutriments().salt100g(),
                        p.nutriments().proteins100g()
                )
        );
    }

    private record ProductRaw(
            int status,
            String code,
            ProductData product
    ) {
    }

    private record SearchRaw(List<ProductData> products) {
    }

    private record ProductData(
            String code,
            @JsonProperty("product_name") String productName,
            String brands,
            @JsonProperty("image_url") String imageUrl,
            @JsonProperty("nutriscore_grade") String nutriscoreGrade,
            @JsonProperty("nova_group") Integer novaGroup,
            NutrimentsRaw nutriments
    ) {
    }

    private record NutrimentsRaw(
            @JsonProperty("energy-kcal_100g") Double energyKcal100g,
            @JsonProperty("fat_100g") Double fat100g,
            @JsonProperty("sugars_100g") Double sugars100g,
            @JsonProperty("salt_100g") Double salt100g,
            @JsonProperty("proteins_100g") Double proteins100g
    ) {
    }
}
