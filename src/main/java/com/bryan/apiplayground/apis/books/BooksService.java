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

    // Asking for a small projection (fields=) keeps the upstream payload tiny;
    // the unprojected response weighs hundreds of KB per page and used to make
    // /books/search take ~9s. Order of the fields list matches Book record's
    // canonical order so the diff between DTO and projection stays obvious.
    private static final String SEARCH_URL =
            "https://openlibrary.org/search.json?title={title}&limit={limit}"
                    + "&fields=key,title,author_name,first_publish_year,cover_i,ia,ebook_access";
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
            var books = docs.stream().map(BooksService::toBook).toList();
            return new BooksResponse(raw.numFound(), books);
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "Open Library returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "Open Library unreachable: " + e.getMessage(), 0, e);
        }
    }

    private static Book toBook(DocRaw d) {
        var author = (d.authorName() == null || d.authorName().isEmpty())
                ? null
                : d.authorName().getFirst();
        // ia is a list of Internet Archive identifiers; the first entry is the
        // one Open Library treats as the canonical scan, perfect for the
        // /details/{iaId} reading link the frontend can build.
        var iaId = (d.ia() == null || d.ia().isEmpty()) ? null : d.ia().getFirst();
        return new Book(
                d.key(),
                d.title(),
                author,
                d.firstPublishYear(),
                d.coverId(),
                coverUrl(d.coverId()),
                iaId,
                d.ebookAccess()
        );
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
            @JsonProperty("cover_i") Integer coverId,
            List<String> ia,
            @JsonProperty("ebook_access") String ebookAccess
    ) {
    }
}
