package com.bryan.apiplayground.apis.pokemon;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Service
public class PokemonService {

    private static final String BASE_URL = "https://pokeapi.co/api/v2/pokemon/{name}";

    private final RestClient restClient;

    public PokemonService(RestClient restClient) {
        this.restClient = restClient;
    }

    public PokemonResponse getByName(String name) {
        try {
            var raw = restClient.get()
                    .uri(BASE_URL, name.toLowerCase())
                    .retrieve()
                    .body(PokeApiRaw.class);
            if (raw == null) {
                throw new ExternalApiException("PokeAPI returned an empty payload", 502);
            }
            var types = raw.types() == null
                    ? List.<String>of()
                    : raw.types().stream().map(t -> t.type().name()).toList();
            var image = raw.sprites() == null ? null : raw.sprites().frontDefault();
            return new PokemonResponse(raw.id(), raw.name(), raw.height(), raw.weight(), types, image);
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "PokeAPI returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "PokeAPI unreachable: " + e.getMessage(), 0, e);
        }
    }

    private record PokeApiRaw(
            int id,
            String name,
            int height,
            int weight,
            List<TypeSlot> types,
            Sprites sprites
    ) {
    }

    private record TypeSlot(int slot, NamedRef type) {
    }

    private record NamedRef(String name) {
    }

    private record Sprites(@JsonProperty("front_default") String frontDefault) {
    }
}
