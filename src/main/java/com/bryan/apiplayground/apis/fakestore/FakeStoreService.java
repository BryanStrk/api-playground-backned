package com.bryan.apiplayground.apis.fakestore;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Service
public class FakeStoreService {

    private static final String PRODUCTS_URL = "https://fakestoreapi.com/products";
    private static final String PRODUCT_BY_ID_URL = "https://fakestoreapi.com/products/{id}";
    private static final String CATEGORIES_URL = "https://fakestoreapi.com/products/categories";

    private final RestClient restClient;

    public FakeStoreService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<Product> getProducts() {
        try {
            // FakeStoreAPI's field names match the record components 1:1
            // (id, title, price, description, category, image, rating{rate,count}),
            // so Jackson binds the array straight into Product[] — no Raw mapping.
            var products = restClient.get()
                    .uri(PRODUCTS_URL)
                    .retrieve()
                    .body(Product[].class);
            return products == null ? List.of() : List.of(products);
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "FakeStoreAPI returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "FakeStoreAPI unreachable: " + e.getMessage(), 0, e);
        }
    }

    public Product getProduct(long id) {
        try {
            return restClient.get()
                    .uri(PRODUCT_BY_ID_URL, id)
                    .retrieve()
                    .body(Product.class);
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "FakeStoreAPI returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "FakeStoreAPI unreachable: " + e.getMessage(), 0, e);
        }
    }

    public List<String> getCategories() {
        try {
            var categories = restClient.get()
                    .uri(CATEGORIES_URL)
                    .retrieve()
                    .body(String[].class);
            return categories == null ? List.of() : List.of(categories);
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "FakeStoreAPI returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "FakeStoreAPI unreachable: " + e.getMessage(), 0, e);
        }
    }
}
