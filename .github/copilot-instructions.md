# Movie Catalog - AI Agent Instructions

## Project Overview
Movie Catalog is a personal media tracking system with a Spring Boot REST API backend and React frontend. The app manages movies and TV series with intelligent recommendations, automated season tracking via web scraping (IMDB/Kinopoisk), and notification system for new seasons.

**Tech Stack**: Java 21, Spring Boot 3.5.7, MongoDB, React 18, Docker

## Architecture Principles

### Layered Architecture Pattern
The backend follows strict layering: Controller → Service → Repository → MongoDB. Never skip layers or call repositories directly from controllers.

**Key Domain Models:**
- `Movie`: Single films with `WatchStatus` (WATCHED/UNWATCHED), priority, length
- `Series`: TV shows with embedded `Season[]` objects, auto-calculated `seriesWatchStatus`, `hasNewSeasons` flag
- `Notification`: Generated when scraper detects new seasons (only for series with ≥1 watched season) - no auto-dismissal
- `Image`: Metadata for downloaded images with `movieId` reference, stored separately from movies/series

### Watch Status Rules
- Movies: Direct `watchStatus` field
- Series: `seriesWatchStatus` auto-calculated from seasons (ALL seasons watched = WATCHED, otherwise UNWATCHED)
- Never manually set `seriesWatchStatus` - it's derived in `SeriesService.calculateSeriesWatchStatus()`

### Recommendation Algorithm (RecommendationService)
Weighted random selection with priority tiers:
1. **Manual priority** (user-set `priority` > 0): `baseWeight * (1 + priority)`
2. **New seasons flag**: `baseWeight * 10` (series with `hasNewSeasons=true`)
3. **Age-based weighting**: `Math.log(daysOld + 2) + 1` (older unwatched content prioritized)

Only returns unwatched content (movies with `UNWATCHED` status, series with unwatched seasons).

## Development Workflows

### Local Development Commands (Makefile)
```bash
make run      # Full stack (backend + frontend + MongoDB) in Docker - blocking foreground
make run-bg   # Full stack (backend + frontend + MongoDB) in Docker - non-blocking background - use this when using playwright tools
make serve    # Backend in Docker, frontend with hot reload (npm start) - best for UI work
make test     # Run backend Maven tests in Docker
```

**Hot Reload Setup**: `make serve` starts backend containers, then runs `npm start` in `frontend/` with Webpack dev server on port 3000. All `src/` changes auto-refresh browser. (Don't use `make serve`, hot reload is not working correctly)

### Testing
- Backend tests: `src/test/java/com/moviecat/` - Use `@SpringBootTest` for integration tests
- No frontend tests currently configured
- Run via `make test` or `mvn test` (requires Docker MongoDB for integration tests)

### Docker Architecture
- `docker-compose.yml`: 3 services (mongodb, app, frontend)
- Backend: Multi-stage Dockerfile (Maven build → JRE runtime)
- Frontend: Build with Webpack → serve via nginx
- Shared volume: `./images:/app/images` for image storage

## Code Patterns & Conventions

### Lombok Usage
All models/DTOs use Lombok annotations - **always include these together**:
```java
@Data               // Getters, setters, equals, hashCode, toString
@Builder            // Builder pattern for object creation
@NoArgsConstructor  // Required for MongoDB/Jackson
@AllArgsConstructor // Required for @Builder
```

Services use `@RequiredArgsConstructor` with `private final` fields for dependency injection (no `@Autowired`).

### Entity-DTO Mapping
Manual mapping in service methods (no MapStruct). Pattern:
- `CatalogService.movieToResponse()` - Entity → DTO
- Request DTOs validated with `@Valid` in controllers
- Response DTOs built with `.builder()` pattern

### Filtering & Sorting (CatalogService)
Always prioritize unwatched items first via `createWatchStatusComparator()`, then apply secondary sort (title/dateAdded/length). Supports combined movie+series queries with `contentType` filter.

### MongoDB Indexing
Critical indexes defined with `@Indexed` in domain models:
- `title`, `watchStatus`, `addedBy`, `dateAdded` - Used for filtering/sorting
- Auto-created via `spring.data.mongodb.auto-index-creation=true`

### External API Integration
- `ExternalApiService` uses Spring WebFlux `WebClient` (non-blocking)
- Scrapes season data from `link` URLs
- Weekly scheduled refresh via `@Scheduled(cron = "${scheduler.cron.season-check}")` - Mondays midnight
- Only processes series with `link` set

### Image Management System
- `ImageService` downloads images from URLs, stores locally with UUID filenames
- MongoDB `Image` entity tracks metadata (originalUrl, filename, contentType, fileSize, movieId)
- Images stored in `images/` directory (gitignored except `.gitkeep`)
- Auto-detection of content types (JPEG, PNG, GIF, WebP)
- Movie/Series `coverImage` field stores `/api/images/{id}` reference, not external URLs
- Workflow: POST `/api/images/download` with `imageUrl` → get `id` → use in `coverImage` field
- No auto-cleanup - images persist until explicitly deleted via DELETE `/api/images/{id}`

## API Conventions

### Endpoint Patterns
- Collection resources: `GET /api/movies`, `GET /api/series`
- Single resource: `GET /api/movies/{id}`
- Status updates: `PATCH /api/movies/{id}/watched`, `PATCH /api/series/{id}/seasons/{seasonNumber}`
- Actions: `POST /api/series/{id}/refresh` (manual season refresh)
- Combined catalog: `GET /api/catalog` (returns unified movie+series list)
- Images: `POST /api/images/download` (download from URL), `GET /api/images/{id}` (retrieve file), `DELETE /api/images/{id}`

### Query Parameters
Standard filters: `genre`, `watchStatus`, `addedBy`, `sortBy`  
Series-specific: `hasNewSeasons`, `seriesStatus` (COMPLETE/ONGOING)  
Example: `GET /api/catalog?watchStatus=UNWATCHED&sortBy=dateAdded`

### Response Format
All responses use DTO builders with consistent fields:
- `CatalogItemResponse` - Unified format for both movies and series
- Include `contentType` (MOVIE/SERIES) for client-side handling
- Series responses include `hasNewSeasons`, `totalAvailableSeasons`, `seriesStatus`

## Configuration & Properties

### Key Properties (`application.properties`)
```properties
server.port=8080
spring.data.mongodb.uri=mongodb://localhost:27017/moviecat  # Override in docker-compose
scheduler.cron.season-check=0 0 0 * * MON  # Weekly season refresh
image.storage.path=images  # Local filesystem for downloaded images (UUID filenames)
```

### Environment Profiles
- Default: Local MongoDB on 27017
- Docker: `SPRING_PROFILES_ACTIVE=docker` with `mongodb://mongodb:27017/moviecat`

## Frontend Integration

### API Base URL
Development: `http://localhost:8080/api`  
Production (Docker): `/api` (nginx proxies to backend)

### Main Components
- `CatalogList.jsx`: Grid display with filtering, uses `axios` for API calls
- `FilterPanel.jsx`: Watch status, genre, type filters
- `AddMovieModal.jsx`: Form for creating new entries

### State Management
Simple React hooks (no Redux) - fetch on mount, local state for filters/search.

## Common Tasks

### Adding New Entity Field
1. Add to domain model (`Movie.java`/`Series.java`) with `@Indexed` if queried
2. Add to request/response DTOs with Lombok annotations
3. Update service mapping methods (`movieToResponse()`, etc.)
4. Update repository if custom query needed
5. Clear/rebuild: `make run` (full rebuild)

### Adding New Filter
1. Add parameter to `CatalogService.getCatalog()`
2. Add stream filter in appropriate section (movies/series)
3. Add query param to `CatalogController` endpoint
4. Document in Swagger annotations

### Debugging MongoDB Queries
Use `logging.level.com.moviecat=DEBUG` in `application.properties` - logs all repository calls and service operations. Connect to MongoDB shell: `docker exec -it moviecat-mongodb mongosh moviecat`

## Swagger/OpenAPI
Access at `http://localhost:8080/swagger-ui.html` - auto-generated from SpringDoc annotations on controllers. Use for API testing without Postman.

## Critical Files Reference
- `docs/architecture.md` - Detailed system design and component interactions
- `docs/IMAGE_SYSTEM_SUMMARY.md` - Complete image management system documentation
- `CatalogService.java` - Core filtering/sorting logic with unified catalog handling
- `RecommendationService.java` - Weighted recommendation algorithm implementation
- `ImageService.java` - Image download, storage, and retrieval with WebClient
- `docker-compose.yml` - Full service orchestration and networking
- `Makefile` - Developer workflow commands (run/serve/test)

# Important Tips
Use playwright tools for frontend testing when running in background mode (`make run-bg`). Always validate new endpoints with Swagger UI. Follow layered architecture strictly to maintain code quality and separation of concerns.
NEVER run docker or docker-compose commands directly. Use the provided Makefile commands for consistency. You can use `make kill-webpack` to stop any webpack dev server processes running on port 3000. Also `make run-bg` to run and rebuild docker containers in the background when using playwright tools.