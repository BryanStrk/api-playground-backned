package com.bryan.apiplayground.apis.hackernews;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hn")
@Tag(name = "Noticias", description = "Historias de Hacker News (sin API key)")
public class HackerNewsController {

    private final HackerNewsService hackerNewsService;

    public HackerNewsController(HackerNewsService hackerNewsService) {
        this.hackerNewsService = hackerNewsService;
    }

    @GetMapping("/stories")
    @Operation(
            summary = "Historias de Hacker News",
            description = "type: top (por defecto) | new | best. limit: 1–50 (default 20). "
                    + "El servicio pide la lista de ids al feed indicado y resuelve cada historia "
                    + "en paralelo con virtual threads — un único round-trip aparente en lugar de N."
    )
    public List<HnStory> getStories(
            @RequestParam(defaultValue = "top") String type,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return hackerNewsService.getStories(type, limit);
    }
}
