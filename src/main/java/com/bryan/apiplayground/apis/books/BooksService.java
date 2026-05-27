package com.bryan.apiplayground.apis.books;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Service
public class BooksService {

    private static final String SEARCH_URL =
            "https://openlibrary.org/search.json?title={title}&limit={limit}";
    // Open Library blocks UAs without a contact form (returns 403). Override the
    // global "api-playground/1.0" only for this upstream — see openlibrary.org/dev/docs/api/search.
    private static final String USER_AGENT =
            "ApiPlayground/1.0 (https://github.com/BryanStrk; bryanpaicoalbines97@gmail.com)";

    private final RestClient restClient;

    public BooksService(RestClient restClient) {
        this.restClient = restClient;
    }

    public BooksResponse search(String title, int limit) {
        try {
            var raw = restClient.get()
                    .uri(SEARCH_URL, title, limit)
                    .header("User-Agent", USER_AGENT)
                    .retrieve()
                    .body(OpenLibraryRaw.class);
            if (raw == null) {
                return new BooksResponse(0, List.of());
            }
            var docs = raw.docs() == null ? List.<DocRaw>of() : raw.docs();
            var books = docs.stream()
                    .map(d -> new Book(
                            d.key(),
                            d.title(),
                            d.authorName() == null ? List.of() : d.authorName(),
                            d.firstPublishYear(),
                            coverUrl(d.coverId())))
                    .toList();
            return new BooksResponse(raw.numFound(), books);
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "Open Library returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "Open Library unreachable: " + e.getMessage(), 0, e);
        }
    }

    private static String coverUrl(Integer coverId) {
        // -M.jpg (≈180px wide) is the right size for a grid of thumbnails; -L is overkill.
        return coverId == null ? null : "https://covers.openlibrary.org/b/id/" + coverId + "-M.jpg";
    }

    private record OpenLibraryRaw(
            @JsonProperty("numFound") int numFound,
            List<DocRaw> docs
    ) {
    }

    private record DocRaw(
            String key,
            String title,
            @JsonProperty("author_name") List<String> authorName,
            @JsonProperty("first_publish_year") Integer firstPublishYear,
            @JsonProperty("cover_i") Integer coverId
    ) {
    }
}
