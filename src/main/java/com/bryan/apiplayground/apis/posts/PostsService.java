package com.bryan.apiplayground.apis.posts;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class PostsService {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com/posts/{id}";

    private final RestClient restClient;

    public PostsService(RestClient restClient) {
        this.restClient = restClient;
    }

    public PostResponse getById(int id) {
        try {
            var post = restClient.get()
                    .uri(BASE_URL, id)
                    .retrieve()
                    .body(PostResponse.class);
            if (post == null) {
                throw new ExternalApiException("JSONPlaceholder returned an empty payload", 502);
            }
            return post;
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "JSONPlaceholder returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "JSONPlaceholder unreachable: " + e.getMessage(), 0, e);
        }
    }
}
