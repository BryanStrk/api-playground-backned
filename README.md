# API Playground — Backend

Backend REST en **Spring Boot 4** sobre **Java 25** que actúa como proxy/agregador hacia **20 APIs públicas** y expone un endpoint propio de monitorización en vivo (`/health`) pensado para demos en clase.

El frontend (Angular en `localhost:4200`) habla **solo con este backend** — así las API keys nunca llegan al navegador y no hay bloqueos de CORS contra terceros.

---

## Requisitos

- **Java 25** (LTS).
- **Maven 3.9+**.
- Conexión a internet (las APIs externas son públicas).

```bash
java -version   # debe mostrar 25
mvn -v
```

---

## Quickstart

```bash
# 1. Clonar y entrar al proyecto
git clone <repo> api-playground-backend
cd api-playground-backend

# 2. Crear tu .env desde la plantilla
cp .env.example .env
# (opcional) editar .env y poner tus keys

# 3. Arrancar
mvn spring-boot:run
```

Las **13 APIs sin key y las 3 con key opcional funcionan sin tocar nada**. Solo las 4 que requieren key se desactivan (devuelven 503 limpio) hasta que rellenes el `.env`.

Endpoints clave para probar inmediatamente:
- `http://localhost:8080/api/v1/catalog`
- `http://localhost:8080/api/v1/health`
- `http://localhost:8080/swagger-ui.html`

### Cargar el `.env` en IntelliJ

`Run → Edit Configurations → Environment variables → Load from file → .env`

`.env` está en `.gitignore` desde el primer commit — nunca subas keys reales.

---

## Variables de entorno

| Variable | API | Obligatoria | Sin configurar |
|---|---|---|---|
| `TMDB_API_KEY` | TMDB (películas) | **Sí** | `/movies/popular` → 503 |
| `GEMINI_API_KEY` | Google Gemini (IA) | **Sí** | `/ai/generate` → 503 |
| `UNSPLASH_ACCESS_KEY` | Unsplash (fotos) | **Sí** | `/photos/random` → 503 |
| `NEWS_API_KEY` | NewsAPI (noticias) | **Sí** | `/news/headlines` → 503 |
| `NASA_API_KEY` | NASA APOD | Opcional | usa `DEMO_KEY` (rate-limit bajo) |
| `CAT_API_KEY` | The Cat API | Opcional | usa rate-limit anónimo |
| `SPORTSDB_KEY` | TheSportsDB | Opcional | usa key pública `123` |

Dónde sacar las cuatro REQUIRED:
- TMDB → https://www.themoviedb.org/settings/api
- Gemini → https://aistudio.google.com/app/apikey
- Unsplash → https://unsplash.com/developers
- NewsAPI → https://newsapi.org/register

---

## Endpoints

Base path: `/api/v1`.

### Catálogo y monitorización

| Método | Endpoint | Devuelve |
|---|---|---|
| GET | `/catalog` | Metadata de las 20 APIs (id, name, category, difficulty, requiresKey, localEndpoint, externalUrl, description). |
| GET | `/health` | Ping concurrente a las 20 con estado/latencia. Ver sección dedicada abajo. |

### Las 20 APIs proxied

| # | Endpoint propio | Upstream | Key |
|---|---|---|---|
| 1 | `GET /weather?lat=&lon=` | Open-Meteo | — |
| 2 | `GET /movies/popular?lang=es-ES` | TMDB | `TMDB_API_KEY` |
| 3 | `GET /music/search?term=&limit=` | iTunes Search | — |
| 4 | `GET /crypto/price?ids=&vs=eur` | CoinGecko | — |
| 5 | `GET /pokemon/{name}` | PokéAPI | — |
| 6 | `GET /countries/{name}` | REST Countries | — |
| 7 | `POST /ai/generate` body `{"prompt":"..."}` | Gemini 2.5 Flash | `GEMINI_API_KEY` |
| 8 | `GET /posts/{id}` | JSONPlaceholder | — |
| 9 | `GET /cats/random` | The Cat API | `CAT_API_KEY` (opcional) |
| 10 | `GET /meals/search?name=` | TheMealDB | — |
| 11 | `GET /photos/random?query=` | Unsplash | `UNSPLASH_ACCESS_KEY` |
| 12 | `GET /sports/team?name=` | TheSportsDB | `SPORTSDB_KEY` (opcional) |
| 13 | `GET /dictionary/{word}` | Free Dictionary | — |
| 14 | `GET /space/apod` | NASA APOD | `NASA_API_KEY` (opcional) |
| 15 | `GET /exchange?base=EUR&symbols=USD` | Frankfurter | — |
| 16 | `GET /users/random?nat=es` | RandomUser | — |
| 17 | `GET /github/{username}` | GitHub | — |
| 18 | `GET /books/search?title=&limit=10` | Open Library | — |
| 19 | `GET /news/headlines?country=us` | NewsAPI | `NEWS_API_KEY` |
| 20 | `GET /characters/{id}` | Rick and Morty | — |

Cada endpoint devuelve un **DTO `record` propio** del backend, no el JSON crudo del upstream. Los campos snake_case del upstream se traducen a camelCase (por ejemplo `avatar_url` → `avatarUrl`, `strMeal` → `name`, `poster_path` → `posterUrl` ya con el host de TMDB prefijado).

### Documentación interactiva (Swagger)

- UI: `http://localhost:8080/swagger-ui.html`
- Spec OpenAPI: `http://localhost:8080/v3/api-docs`

Los endpoints aparecen agrupados por `@Tag` (Catálogo, Monitorización, Clima, Cine, Música, Finanzas, Juegos, …).

---

## El endpoint `/health`

Pensado para demos en vivo: lanza una petición HTTP en paralelo a las 20 APIs externas y devuelve estado individual + métricas globales.

### Forma de la respuesta

```json
{
  "checkedAt": "2026-05-27T08:30:12.345Z",
  "total": 20,
  "up": 16,
  "down": 0,
  "skipped": 4,
  "totalTimeMs": 2498,
  "results": [
    { "id": "weather",   "name": "Open-Meteo", "category": "Clima",
      "status": "UP",      "httpStatus": 200, "responseTimeMs": 257, "error": null },
    { "id": "movies",    "name": "TMDB",       "category": "Cine",
      "status": "SKIPPED", "httpStatus": null, "responseTimeMs": 0,   "error": "API key no configurada" },
    { "id": "books",     "name": "Open Library", "category": "Cultura",
      "status": "UP",      "httpStatus": 403, "responseTimeMs": 2492, "error": null }
  ]
}
```

### Clasificación de estado

| Estado | Cuándo |
|---|---|
| **UP** | El upstream respondió con HTTP `< 500` dentro del timeout (4s). **404 cuenta como UP**: el host está vivo, solo el path es incompleto. |
| **DOWN** | Timeout, error de conexión o HTTP `5xx`. |
| **SKIPPED** | La API tiene `requiresKey == REQUIRED` y la key no está configurada. No es un fallo del servicio. |

Las APIs con key opcional (NASA, TheSportsDB, The Cat API) **nunca** salen SKIPPED — usan fallback (`DEMO_KEY`, `"123"`, anónimo) y se pinguean siempre.

### Por qué `totalTimeMs` debería parecerse al ping más lento

`HealthService` lanza los 20 pings sobre **virtual threads** de Java 25 (`Executors.newVirtualThreadPerTaskExecutor()`), por lo que el wall-clock total es ≈ `max(ping_i)` en lugar de `sum(ping_i)`.

Con un run típico:

```
totalTimeMs  = 2498   ← wall-clock observado
slowest_ping = 2492   ← la API más lenta del run
sum_of_pings = 7357   ← lo que tardaría en serie
speedup ≈ 2.9x
```

Si quitas la API más lenta del catálogo, `totalTimeMs` baja al siguiente máximo. Es la prueba visual de que la concurrencia funciona.

### Probar que una API caída no rompe el endpoint

Edita temporalmente `CatalogService.java` y cambia el `externalUrl` de cualquier API por `https://this-host-does-not-exist.invalid`. Vuelve a llamar a `/health` — esa API saldrá como `DOWN` con su mensaje de error, y el resto seguirá normalmente. `/health` global siempre responde **200**.

---

## Estructura del proyecto

```
src/main/java/com/bryan/apiplayground/
├── ApiPlaygroundApplication.java
├── config/
│   ├── RestClientConfig.java     # RestClient general (10s/15s) + healthRestClient (4s/4s)
│   ├── CorsConfig.java           # permite http://localhost:4200 (Angular dev)
│   └── OpenApiConfig.java        # info de Swagger
├── common/
│   ├── ApiError.java             # record uniforme de error
│   ├── GlobalExceptionHandler.java
│   └── exception/
│       ├── ExternalApiException.java       # → 502 (4xx/5xx upstream) o 504 (timeout)
│       └── ApiKeyNotConfiguredException.java # → 503
├── catalog/                      # GET /catalog — fuente única de las 20
├── health/                       # GET /health — ping concurrente con virtual threads
└── apis/                         # un paquete por API: DTO(s) record + Service + Controller
    ├── weather/  movies/  music/  crypto/  pokemon/  countries/  ai/
    ├── posts/    cats/    meals/  photos/  sports/  dictionary/  space/
    └── exchange/ users/   github/ books/   news/    characters/
```

Capas estrictas: **Controller → Service → RestClient**. Los DTOs públicos son `record` limpios en camelCase; cuando el upstream usa snake_case o nombres feos (`strMeal`, `idTeam`), el service tiene records `*Raw` privados para parsear y los mapea al DTO público.

---

## Manejo de errores

Todas las respuestas de error usan el mismo shape:

```json
{ "status": 503, "message": "...", "path": "/api/v1/movies/popular", "timestamp": "2026-05-27T08:30:12.345Z" }
```

| Situación | Status | Origen |
|---|---|---|
| Validación del request (parámetro fuera de rango, body inválido) | **400** | `HandlerMethodValidationException` / `MethodArgumentNotValidException` |
| API key REQUIRED no configurada | **503** | `ApiKeyNotConfiguredException` |
| Upstream respondió 4xx/5xx | **502** | `ExternalApiException(upstreamStatus != 0)` |
| Upstream no contestó (timeout/conexión) | **504** | `ExternalApiException(upstreamStatus == 0)` |
| Resto | **500** | handler genérico |

---

## Stack

- Spring Boot **4.0.6** sobre Spring Framework 7.
- Java **25** (`<java.version>25</java.version>`, `maven.compiler.source/target = 25`).
- Jackson **3** (`tools.jackson.*`); las anotaciones siguen en `com.fasterxml.jackson.annotation.*`.
- `springdoc-openapi-starter-webmvc-ui` **3.0.3** (rama compatible con SB4).
- Cliente HTTP: `RestClient` (no `RestTemplate`, no `WebClient`).
- Validación: `spring-boot-starter-validation` (Jakarta Validation).
