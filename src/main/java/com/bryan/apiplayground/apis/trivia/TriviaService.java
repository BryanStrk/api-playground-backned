package com.bryan.apiplayground.apis.trivia;

import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class TriviaService {

    private static final String API_BASE = "https://opentdb.com/api.php";
    private static final String CATEGORIES_URL = "https://opentdb.com/api_category.php";

    private final RestClient restClient;

    public TriviaService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<TriviaQuestion> getQuestions(int amount, Integer category, String difficulty, String type) {
        // Ask the upstream for url3986-encoded text so we don't get HTML entities
        // like &quot; / &#039; embedded in the strings; we decode them ourselves.
        var builder = UriComponentsBuilder.fromUriString(API_BASE)
                .queryParam("amount", amount)
                .queryParam("encoding", "url3986");
        if (category != null) builder.queryParam("category", category);
        if (notBlank(difficulty)) builder.queryParam("difficulty", difficulty);
        if (notBlank(type)) builder.queryParam("type", type);
        try {
            var raw = restClient.get()
                    .uri(builder.build(false).toUriString())
                    .retrieve()
                    .body(QuestionsRaw.class);
            if (raw == null || raw.results() == null) return List.of();
            return raw.results().stream()
                    .map(q -> new TriviaQuestion(
                            decode(q.category()),
                            decode(q.type()),
                            decode(q.difficulty()),
                            decode(q.question()),
                            decode(q.correctAnswer()),
                            q.incorrectAnswers() == null
                                    ? List.of()
                                    : q.incorrectAnswers().stream().map(TriviaService::decode).toList()))
                    .toList();
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "OpenTriviaDB returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "OpenTriviaDB unreachable: " + e.getMessage(), 0, e);
        }
    }

    public List<TriviaCategory> getCategories() {
        try {
            var raw = restClient.get()
                    .uri(CATEGORIES_URL)
                    .retrieve()
                    .body(CategoriesRaw.class);
            return raw == null || raw.triviaCategories() == null ? List.of() : raw.triviaCategories();
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "OpenTriviaDB returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "OpenTriviaDB unreachable: " + e.getMessage(), 0, e);
        }
    }

    // Even with encoding=url3986 the upstream still slips HTML entities into
    // the strings (e.g. &quot;, &#039;, &amp;), so we both URL-decode (for the
    // percent-escapes the param promises) AND HTML-unescape (for the entities
    // it leaves behind). Both passes wrapped so a malformed entry never kills
    // the whole batch.
    private static String decode(String s) {
        if (s == null) return null;
        try {
            return HtmlUtils.htmlUnescape(URLDecoder.decode(s, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            return HtmlUtils.htmlUnescape(s);
        }
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private record QuestionsRaw(
            @JsonProperty("response_code") int responseCode,
            List<QuestionRaw> results
    ) {
    }

    private record QuestionRaw(
            String category,
            String type,
            String difficulty,
            String question,
            @JsonProperty("correct_answer") String correctAnswer,
            @JsonProperty("incorrect_answers") List<String> incorrectAnswers
    ) {
    }

    private record CategoriesRaw(
            @JsonProperty("trivia_categories") List<TriviaCategory> triviaCategories
    ) {
    }
}
