package com.bryan.apiplayground.apis.news;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/news")
@Tag(name = "Noticias", description = "Búsqueda de noticias vía NewsAPI /everything (requiere NEWS_API_KEY)")
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping("/headlines")
    @Operation(
            summary = "Buscar noticias por término e idioma",
            description = "q: texto libre (por defecto 'world'). language: ISO-639-1 (es, en, fr, de, it, pt, nl…). "
                    + "Orden: más recientes primero, 15 resultados por página. "
                    + "Migrado desde /top-headlines porque NewsAPI restringió ?country= solo a 'us'."
    )
    public NewsResponse getHeadlines(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "en") String language
    ) {
        return newsService.getHeadlines(q, language);
    }
}
