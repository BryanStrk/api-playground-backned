package com.bryan.apiplayground.apis.dota;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Service
public class DotaService {

    private static final String HEROES_URL = "https://api.opendota.com/api/heroStats";
    private static final String PRO_MATCHES_URL = "https://api.opendota.com/api/proMatches";
    // OpenDota ships hero art as paths relative to Valve's CDN; the dashboard
    // needs absolute URLs to render <img>. Prefix here so the front never has
    // to know about Steam's static host.
    private static final String STEAM_CDN = "https://cdn.cloudflare.steamstatic.com";
    private static final int DEFAULT_MATCH_LIMIT = 20;
    private static final int MAX_MATCH_LIMIT = 100;
    private static final ParameterizedTypeReference<List<HeroRaw>> HERO_LIST =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<List<MatchRaw>> MATCH_LIST =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    public DotaService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<DotaHero> listHeroes() {
        try {
            var raw = restClient.get()
                    .uri(HEROES_URL)
                    .retrieve()
                    .body(HERO_LIST);
            if (raw == null) return List.of();
            return raw.stream()
                    .map(h -> new DotaHero(
                            h.id(),
                            h.name(),
                            h.localizedName(),
                            h.primaryAttr(),
                            h.attackType(),
                            h.roles() == null ? List.of() : h.roles(),
                            absolute(h.img()),
                            absolute(h.icon())))
                    .toList();
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "OpenDota returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "OpenDota unreachable: " + e.getMessage(), 0, e);
        }
    }

    public List<ProMatch> listProMatches(int limit) {
        var effectiveLimit = Math.clamp(limit, 1, MAX_MATCH_LIMIT);
        try {
            var raw = restClient.get()
                    .uri(PRO_MATCHES_URL)
                    .retrieve()
                    .body(MATCH_LIST);
            if (raw == null) return List.of();
            return raw.stream()
                    .limit(effectiveLimit)
                    .map(m -> new ProMatch(
                            m.matchId(),
                            m.leagueName(),
                            m.startTime(),
                            m.duration(),
                            m.radiantWin(),
                            m.radiantName(),
                            m.direName()))
                    .toList();
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "OpenDota returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "OpenDota unreachable: " + e.getMessage(), 0, e);
        }
    }

    public int defaultMatchLimit() {
        return DEFAULT_MATCH_LIMIT;
    }

    private static String absolute(String path) {
        return (path == null || path.isBlank()) ? null : STEAM_CDN + path;
    }

    private record HeroRaw(
            int id,
            String name,
            @JsonProperty("localized_name") String localizedName,
            @JsonProperty("primary_attr") String primaryAttr,
            @JsonProperty("attack_type") String attackType,
            List<String> roles,
            String img,
            String icon
    ) {
    }

    private record MatchRaw(
            @JsonProperty("match_id") long matchId,
            @JsonProperty("league_name") String leagueName,
            @JsonProperty("start_time") long startTime,
            int duration,
            @JsonProperty("radiant_win") boolean radiantWin,
            @JsonProperty("radiant_name") String radiantName,
            @JsonProperty("dire_name") String direName
    ) {
    }
}
