package com.bryan.apiplayground.apis.github;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class GithubService {

    private static final String USER_URL = "https://api.github.com/users/{username}";

    private final RestClient restClient;

    public GithubService(RestClient restClient) {
        this.restClient = restClient;
    }

    public GithubUserResponse getUser(String username) {
        try {
            var user = restClient.get()
                    .uri(USER_URL, username)
                    .header("Accept", "application/vnd.github+json")
                    .retrieve()
                    .body(GithubUserResponse.class);
            if (user == null) {
                throw new ExternalApiException("GitHub returned an empty payload", 502);
            }
            return user;
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "GitHub returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "GitHub unreachable: " + e.getMessage(), 0, e);
        }
    }
}
