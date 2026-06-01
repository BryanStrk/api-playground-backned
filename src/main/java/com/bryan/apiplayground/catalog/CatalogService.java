package com.bryan.apiplayground.catalog;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Single source of truth for the 20 public APIs the playground proxies.
 * The catalog endpoint surfaces this list to the frontend so the dashboard
 * can render the grid, and {@code HealthService} reuses it to know which
 * upstreams to ping — keep the list in sync with the controllers in
 * {@code com.bryan.apiplayground.apis.*}.
 */
@Service
public class CatalogService {

    private static final List<ApiInfo> APIS = List.of(
            new ApiInfo(
                    "weather",
                    "Open-Meteo",
                    "Clima",
                    Difficulty.EASY,
                    KeyRequirement.NONE,
                    "/api/v1/weather",
                    "https://api.open-meteo.com/v1/forecast",
                    "Pronóstico del tiempo por coordenadas, sin API key."
            ),
            new ApiInfo(
                    "movies",
                    "TMDB",
                    "Cine",
                    Difficulty.MEDIUM,
                    KeyRequirement.REQUIRED,
                    "/api/v1/movies/popular",
                    "https://api.themoviedb.org/3/movie/popular",
                    "Películas populares en TMDB. Requiere TMDB_API_KEY."
            ),
            new ApiInfo(
                    "music",
                    "iTunes Search",
                    "Música",
                    Difficulty.EASY,
                    KeyRequirement.NONE,
                    "/api/v1/music/search",
                    "https://itunes.apple.com/search",
                    "Búsqueda de canciones en el catálogo de iTunes."
            ),
            new ApiInfo(
                    "countries",
                    "REST Countries",
                    "Geografía",
                    Difficulty.EASY,
                    KeyRequirement.NONE,
                    "/api/v1/countries/{name}",
                    "https://restcountries.com/v3.1/name",
                    "Información de un país por nombre."
            ),
            new ApiInfo(
                    "meals",
                    "TheMealDB",
                    "Cocina",
                    Difficulty.EASY,
                    KeyRequirement.NONE,
                    "/api/v1/meals/search",
                    "https://www.themealdb.com/api/json/v1/1/search.php",
                    "Búsqueda de recetas por nombre."
            ),
            new ApiInfo(
                    "cocktails",
                    "TheCocktailDB",
                    "Bebida",
                    Difficulty.EASY,
                    KeyRequirement.NONE,
                    "/api/v1/cocktails/search",
                    "https://www.thecocktaildb.com/api/json/v1/1/search.php",
                    "Búsqueda de cócteles por nombre."
            ),
            new ApiInfo(
                    "photos",
                    "Unsplash",
                    "Fotografía",
                    Difficulty.MEDIUM,
                    KeyRequirement.REQUIRED,
                    "/api/v1/photos/random",
                    "https://api.unsplash.com/photos/random",
                    "Foto aleatoria por término. Requiere UNSPLASH_ACCESS_KEY."
            ),
            new ApiInfo(
                    "sports",
                    "Football-Data",
                    "Deportes",
                    Difficulty.MEDIUM,
                    KeyRequirement.REQUIRED,
                    "/api/v1/sports/standings",
                    "https://api.football-data.org/v4/competitions/PD/standings",
                    "Clasificación de las grandes ligas de fútbol (Football-Data.org). Requiere FOOTBALL_DATA_KEY."
            ),
            new ApiInfo(
                    "space",
                    "NASA APOD",
                    "Ciencia",
                    Difficulty.EASY,
                    KeyRequirement.OPTIONAL,
                    "/api/v1/space/apod",
                    "https://api.nasa.gov/planetary/apod",
                    "Astronomy Picture of the Day. Usa DEMO_KEY si no se configura NASA_API_KEY."
            ),
            new ApiInfo(
                    "users",
                    "RandomUser",
                    "Demos",
                    Difficulty.EASY,
                    KeyRequirement.NONE,
                    "/api/v1/users/random",
                    "https://randomuser.me/api",
                    "Usuario aleatorio para mockear UIs."
            ),
            new ApiInfo(
                    "books",
                    "Open Library",
                    "Cultura",
                    Difficulty.EASY,
                    KeyRequirement.NONE,
                    "/api/v1/books/search",
                    "https://openlibrary.org/search.json",
                    "Búsqueda de libros por título."
            ),
            new ApiInfo(
                    "news",
                    "NewsAPI",
                    "Noticias",
                    Difficulty.MEDIUM,
                    KeyRequirement.REQUIRED,
                    "/api/v1/news/headlines",
                    "https://newsapi.org/v2/everything",
                    "Búsqueda de noticias por término e idioma. Requiere NEWS_API_KEY."
            ),
            new ApiInfo(
                    "qrcode",
                    "QR Code",
                    "Herramientas",
                    Difficulty.EASY,
                    KeyRequirement.NONE,
                    "/api/v1/qrcode",
                    "https://goqr.me/api/",
                    "Genera códigos QR a partir de texto o URL."
            ),
            new ApiInfo(
                    "holidays",
                    "Festivos",
                    "Cultura",
                    Difficulty.EASY,
                    KeyRequirement.NONE,
                    "/api/v1/holidays",
                    "https://date.nager.at",
                    "Festivos públicos por país y año."
            ),
            new ApiInfo(
                    "trivia",
                    "Trivia",
                    "Juegos",
                    Difficulty.MEDIUM,
                    KeyRequirement.NONE,
                    "/api/v1/trivia",
                    "https://opentdb.com",
                    "Preguntas de cultura general para jugar."
            ),
            new ApiInfo(
                    "hn",
                    "Hacker News",
                    "Noticias",
                    Difficulty.EASY,
                    KeyRequirement.NONE,
                    "/api/v1/hn/stories",
                    "https://github.com/HackerNews/API",
                    "Las noticias y debates tech del momento."
            ),
            new ApiInfo(
                    "worldcup",
                    "Mundial 2026",
                    "Deportes",
                    Difficulty.MEDIUM,
                    KeyRequirement.NONE,
                    "/api/v1/worldcup/matches",
                    "https://github.com/openfootball/worldcup.json",
                    "Partidos, grupos y datos del Mundial 2026 (openfootball, sin key)."
            ),
            new ApiInfo(
                    "balldontlie",
                    "Mundial (BALLDONTLIE)",
                    "Deportes",
                    Difficulty.MEDIUM,
                    KeyRequirement.REQUIRED,
                    "/api/v1/balldontlie/teams",
                    "https://fifa.balldontlie.io/",
                    "Datos live del Mundial con sistema de tiers (FREE/ALL-STAR/GOAT). Requiere BALLDONTLIE_API_KEY."
            )
    );

    public List<ApiInfo> findAll() {
        return APIS;
    }
}
