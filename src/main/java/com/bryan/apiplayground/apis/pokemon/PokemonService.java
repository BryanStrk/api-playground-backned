package com.bryan.apiplayground.apis.pokemon;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PokemonService {

    private static final String BASE_URL = "https://pokeapi.co/api/v2/pokemon/{name}";
    private static final String TYPE_URL = "https://pokeapi.co/api/v2/type/{type}";

    private final RestClient restClient;

    public PokemonService(RestClient restClient) {
        this.restClient = restClient;
    }

    public PokemonTypeResponse listByType(String type) {
        try {
            var raw = restClient.get()
                    .uri(TYPE_URL, type.toLowerCase())
                    .retrieve()
                    .body(PokeTypeRaw.class);
            if (raw == null) {
                throw new ExternalApiException("PokeAPI returned an empty payload", 502);
            }
            var names = (raw.pokemon() == null ? List.<PokemonSlot>of() : raw.pokemon()).stream()
                    .map(p -> p.pokemon() == null ? null : p.pokemon().name())
                    .filter(n -> n != null)
                    .toList();
            return new PokemonTypeResponse(raw.name(), names.size(), names);
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "PokeAPI returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "PokeAPI unreachable: " + e.getMessage(), 0, e);
        }
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
            return new PokemonResponse(
                    raw.id(),
                    raw.name(),
                    raw.height(),
                    raw.weight(),
                    types,
                    spriteUrl(raw.sprites()),
                    statsMap(raw.stats())
            );
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "PokeAPI returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "PokeAPI unreachable: " + e.getMessage(), 0, e);
        }
    }

    // Prefer the high-resolution official artwork render and fall back to the
    // small pixel sprite so demos still get something when the artwork is null.
    private static String spriteUrl(Sprites sprites) {
        if (sprites == null) return null;
        if (sprites.other() != null
                && sprites.other().officialArtwork() != null
                && sprites.other().officialArtwork().frontDefault() != null) {
            return sprites.other().officialArtwork().frontDefault();
        }
        return sprites.frontDefault();
    }

    private static Map<String, Integer> statsMap(List<StatEntry> stats) {
        if (stats == null) return Map.of();
        var out = new LinkedHashMap<String, Integer>();
        for (var s : stats) {
            if (s.stat() != null && s.stat().name() != null) {
                out.put(s.stat().name(), s.baseStat());
            }
        }
        return out;
    }

    private record PokeApiRaw(
            int id,
            String name,
            int height,
            int weight,
            List<TypeSlot> types,
            Sprites sprites,
            List<StatEntry> stats
    ) {
    }

    private record TypeSlot(int slot, NamedRef type) {
    }

    private record NamedRef(String name) {
    }

    private record Sprites(
            @JsonProperty("front_default") String frontDefault,
            Other other
    ) {
    }

    private record Other(
            @JsonProperty("official-artwork") OfficialArtwork officialArtwork
    ) {
    }

    private record OfficialArtwork(
            @JsonProperty("front_default") String frontDefault
    ) {
    }

    private record StatEntry(
            @JsonProperty("base_stat") int baseStat,
            NamedRef stat
    ) {
    }

    private record PokeTypeRaw(String name, List<PokemonSlot> pokemon) {
    }

    private record PokemonSlot(NamedRef pokemon, int slot) {
    }
}
