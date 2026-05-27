package com.bryan.apiplayground.apis.news;

import com.bryan.apiplayground.common.exception.ApiKeyNotConfiguredException;
import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
public class NewsService {

    // /top-headlines locks ?country= to "us" only (per NewsAPI docs), which made
    // every non-US dashboard query return zero results. /everything searches the
    // full archive by ?q= and ?language= and works for every supported locale.
    private static final String EVERYTHING_BASE =
            "https://newsapi.org/v2/everything";
    // /everything requires a non-empty q; "world" gives a broad international
    // feed when the user hasn't typed anything yet.
    private static final String DEFAULT_QUERY = "world";
    private static final int PAGE_SIZE = 15;

    private final RestClient restClient;
    private final String apiKey;

    public NewsService(RestClient restClient,
                       @Value("${news.api-key:}") String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    public NewsResponse getHeadlines(String q, String language) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ApiKeyNotConfiguredException(
                    "La API key de NewsAPI no está configurada. Añade NEWS_API_KEY a tu .env");
        }
        var effectiveQuery = (q == null || q.isBlank()) ? DEFAULT_QUERY : q;
        var url = UriComponentsBuilder.fromUriString(EVERYTHING_BASE)
                .queryParam("q", effectiveQuery)
                .queryParam("language", language)
                .queryParam("sortBy", "publishedAt")
                .queryParam("pageSize", PAGE_SIZE)
                .build(false)
                .toUriString();
        try {
            var raw = restClient.get()
                    .uri(url)
                    // Header keeps the API key out of URL strings (logs, browser
                    // history if anyone ever proxies this from the front).
                    .header("X-Api-Key", apiKey)
                    .retrieve()
                    .body(NewsRaw.class);
            if (raw == null) {
                return new NewsResponse(0, List.of());
            }
            var articles = (raw.articles() == null ? List.<ArticleRaw>of() : raw.articles()).stream()
                    .map(a -> new Article(
                            a.source() == null ? null : a.source().name(),
                            a.author(),
                            a.title(),
                            a.description(),
                            a.url(),
                            a.urlToImage(),
                            a.publishedAt()))
                    .toList();
            return new NewsResponse(raw.totalResults(), articles);
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "NewsAPI returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "NewsAPI unreachable: " + e.getMessage(), 0, e);
        }
    }

    private record NewsRaw(
            String status,
            int totalResults,
            List<ArticleRaw> articles
    ) {
    }

    private record ArticleRaw(
            Source source,
            String author,
            String title,
            String description,
            String url,
            @JsonProperty("urlToImage") String urlToImage,
            @JsonProperty("publishedAt") String publishedAt
    ) {
    }

    private record Source(String id, String name) {
    }
}
