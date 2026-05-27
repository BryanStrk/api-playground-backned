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
                    "crypto",
                    "CoinGecko",
                    "Finanzas",
                    Difficulty.EASY,
                    KeyRequirement.NONE,
                    "/api/v1/crypto/price",
                    "https://api.coingecko.com/api/v3/simple/price",
                    "Precio actual de criptomonedas en la divisa indicada."
            ),
            new ApiInfo(
                    "pokemon",
                    "PokéAPI",
                    "Juegos",
                    Difficulty.EASY,
                    KeyRequirement.NONE,
                    "/api/v1/pokemon/{name}",
                    "https://pokeapi.co/api/v2/pokemon",
                    "Datos de un Pokémon por nombre."
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
                    "ai",
                    "Google Gemini",
                    "Inteligencia Artificial",
                    Difficulty.MEDIUM,
                    KeyRequirement.REQUIRED,
                    "/api/v1/ai/generate",
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent",
                    "Generación de texto con Gemini 2.5 Flash. Requiere GEMINI_API_KEY."
            ),
            new ApiInfo(
                    "cats",
                    "The Cat API",
                    "Animales",
                    Difficulty.EASY,
                    KeyRequirement.OPTIONAL,
                    "/api/v1/cats/random",
                    "https://api.thecatapi.com/v1/images/search",
                    "Imagen aleatoria de gato. La key mejora el rate limit."
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
                    "dictionary",
                    "Free Dictionary",
                    "Idiomas",
                    Difficulty.EASY,
                    KeyRequirement.NONE,
                    "/api/v1/dictionary/{word}",
                    "https://api.dictionaryapi.dev/api/v2/entries/en",
                    "Definiciones en inglés palabra a palabra."
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
                    "exchange",
                    "Frankfurter",
                    "Finanzas",
                    Difficulty.EASY,
                    KeyRequirement.NONE,
                    "/api/v1/exchange",
                    "https://api.frankfurter.dev/v1/latest",
                    "Tipos de cambio entre divisas."
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
                    "github",
                    "GitHub",
                    "Desarrollo",
                    Difficulty.EASY,
                    KeyRequirement.NONE,
                    "/api/v1/github/{username}",
                    "https://api.github.com/users",
                    "Perfil público de un usuario de GitHub."
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
                    "characters",
                    "Rick and Morty",
                    "Entretenimiento",
                    Difficulty.EASY,
                    KeyRequirement.NONE,
                    "/api/v1/characters/{id}",
                    "https://rickandmortyapi.com/api/character",
                    "Personaje de Rick and Morty por id."
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
                    "dota",
                    "OpenDota",
                    "Videojuegos",
                    Difficulty.EASY,
                    KeyRequirement.NONE,
                    "/api/v1/dota/heroes",
                    "https://docs.opendota.com",
                    "Datos públicos de Dota 2: héroes y partidas pro."
            )
    );

    public List<ApiInfo> findAll() {
        return APIS;
    }
}
