package com.bryan.apiplayground.apis.ai;

import com.bryan.apiplayground.common.exception.ApiKeyNotConfiguredException;
import com.bryan.apiplayground.common.exception.ExternalApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Service
public class AiService {

    private static final String GENERATE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key={apiKey}";

    private final RestClient restClient;
    private final String apiKey;

    public AiService(RestClient restClient,
                     @Value("${gemini.api-key:}") String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    public GenerateResponse generate(String prompt) {
        requireKey();
        var contents = List.of(new Content("user", List.of(new Part(prompt))));
        return callGemini(contents);
    }

    public GenerateResponse chat(List<ChatMessage> messages) {
        requireKey();
        var contents = messages.stream()
                .map(m -> new Content(m.role(), List.of(new Part(m.text()))))
                .toList();
        return callGemini(contents);
    }

    private GenerateResponse callGemini(List<Content> contents) {
        try {
            var raw = restClient.post()
                    .uri(GENERATE_URL, apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new GeminiRequest(contents))
                    .retrieve()
                    .body(GeminiResponse.class);
            if (raw == null || raw.candidates() == null || raw.candidates().isEmpty()) {
                throw new ExternalApiException("Gemini returned no candidates", 502);
            }
            var first = raw.candidates().getFirst();
            var text = (first.content() == null || first.content().parts() == null || first.content().parts().isEmpty())
                    ? null
                    : first.content().parts().getFirst().text();
            var usage = raw.usageMetadata();
            return new GenerateResponse(
                    text,
                    raw.modelVersion(),
                    usage == null ? null : usage.promptTokenCount(),
                    usage == null ? null : usage.candidatesTokenCount(),
                    usage == null ? null : usage.totalTokenCount()
            );
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                    "Gemini returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "Gemini unreachable: " + e.getMessage(), 0, e);
        }
    }

    private void requireKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ApiKeyNotConfiguredException(
                    "La API key de Gemini no está configurada. Añade GEMINI_API_KEY a tu .env");
        }
    }

    private record GeminiRequest(List<Content> contents) {
    }

    // role is "user" or "model"; Gemini uses it to differentiate turns in chat mode.
    // For single-turn /generate the role stays "user" — it doesn't change Gemini's
    // behaviour but keeps both code paths funneling through the same builder.
    private record Content(String role, List<Part> parts) {
    }

    private record Part(String text) {
    }

    private record GeminiResponse(
            List<Candidate> candidates,
            @JsonProperty("usageMetadata") UsageMetadata usageMetadata,
            @JsonProperty("modelVersion") String modelVersion
    ) {
    }

    private record Candidate(Content content, String finishReason, int index) {
    }

    private record UsageMetadata(
            @JsonProperty("promptTokenCount") Integer promptTokenCount,
            @JsonProperty("candidatesTokenCount") Integer candidatesTokenCount,
            @JsonProperty("totalTokenCount") Integer totalTokenCount
    ) {
    }
}
