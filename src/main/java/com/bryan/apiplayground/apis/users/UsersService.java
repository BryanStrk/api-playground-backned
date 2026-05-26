package com.bryan.apiplayground.apis.users;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.stream.Stream;

@Service
public class UsersService {

    private static final String BASE_URL = "https://randomuser.me/api/?nat={nat}";

    private final RestClient restClient;

    public UsersService(RestClient restClient) {
        this.restClient = restClient;
    }

    public UserResponse getRandom(String nat) {
        try {
            var raw = restClient.get()
                    .uri(BASE_URL, nat)
                    .retrieve()
                    .body(RandomUserRaw.class);
            if (raw == null || raw.results() == null || raw.results().isEmpty()) {
                throw new ExternalApiException("RandomUser returned no results", 502);
            }
            var u = raw.results().getFirst();
            var fullName = Stream.of(
                            u.name() == null ? null : u.name().title(),
                            u.name() == null ? null : u.name().first(),
                            u.name() == null ? null : u.name().last())
                    .filter(s -> s != null && !s.isBlank())
                    .reduce((a, b) -> a + " " + b)
                    .orElse(null);
            return new UserResponse(
                    fullName,
                    u.email(),
                    u.gender(),
                    u.nat(),
                    u.picture() == null ? null : u.picture().large(),
                    u.location() == null ? null : u.location().city(),
                    u.location() == null ? null : u.location().country()
            );
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "RandomUser returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "RandomUser unreachable: " + e.getMessage(), 0, e);
        }
    }

    private record RandomUserRaw(List<UserRaw> results) {
    }

    private record UserRaw(
            Name name,
            String email,
            String gender,
            String nat,
            Picture picture,
            Location location
    ) {
    }

    private record Name(String title, String first, String last) {
    }

    private record Picture(String large) {
    }

    private record Location(String city, String country) {
    }
}
