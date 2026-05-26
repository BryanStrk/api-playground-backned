package com.bryan.apiplayground.apis.characters;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Service
public class CharactersService {

    private static final String BASE_URL = "https://rickandmortyapi.com/api/character/{id}";

    private final RestClient restClient;

    public CharactersService(RestClient restClient) {
        this.restClient = restClient;
    }

    public CharacterResponse getById(int id) {
        try {
            var raw = restClient.get()
                    .uri(BASE_URL, id)
                    .retrieve()
                    .body(CharacterRaw.class);
            if (raw == null) {
                throw new ExternalApiException("Rick and Morty API returned an empty payload", 502);
            }
            return new CharacterResponse(
                    raw.id(),
                    raw.name(),
                    raw.status(),
                    raw.species(),
                    raw.gender(),
                    raw.origin() == null ? null : raw.origin().name(),
                    raw.location() == null ? null : raw.location().name(),
                    raw.image(),
                    raw.episode() == null ? 0 : raw.episode().size()
            );
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "Rick and Morty API returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "Rick and Morty API unreachable: " + e.getMessage(), 0, e);
        }
    }

    private record CharacterRaw(
            int id,
            String name,
            String status,
            String species,
            String gender,
            NamedRef origin,
            NamedRef location,
            String image,
            List<String> episode
    ) {
    }

    private record NamedRef(String name) {
    }
}
