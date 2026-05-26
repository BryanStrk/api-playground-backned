package com.bryan.apiplayground.apis.news;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/news")
@Tag(name = "Noticias", description = "Titulares vía NewsAPI (requiere NEWS_API_KEY)")
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping("/headlines")
    @Operation(
            summary = "Titulares por país",
            description = "Devuelve los principales titulares del país indicado. country = ISO 3166-1 alfa-2 (us, gb, es…)."
    )
    public NewsResponse getHeadlines(@RequestParam(defaultValue = "us") String country) {
        return newsService.getHeadlines(country);
    }
}
