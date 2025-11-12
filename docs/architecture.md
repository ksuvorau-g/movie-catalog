# MOVIE CATALOG - ARCHITECTURE

## Overview
Movie Catalog is a REST API service built with Spring Boot that manages a personal catalog of movies and TV series. The application follows a layered architecture pattern with clear separation of concerns.

---

## Architecture Layers

### 1. Presentation Layer (Controller)
**Responsibility**: Handle HTTP requests, validate input, and format responses

**Components**:
- **MovieController**: REST endpoints for movie operations
  - `POST /api/movies` - Add new movie
  - `GET /api/movies` - List all movies with filtering and sorting
  - `GET /api/movies/{id}` - Get movie details
  - `PUT /api/movies/{id}` - Update movie information
  - `DELETE /api/movies/{id}` - Delete movie
  - `PATCH /api/movies/{id}/watched` - Mark movie as watched/unwatched

- **SeriesController**: REST endpoints for TV series operations
  - `POST /api/series` - Add new TV series
  - `GET /api/series` - List all series with filtering and sorting
  - `GET /api/series/{id}` - Get series details
  - `PUT /api/series/{id}` - Update series information
  - `DELETE /api/series/{id}` - Delete series
  - `PATCH /api/series/{id}/seasons/{seasonNumber}` - Mark season as watched/unwatched
  - `PATCH /api/series/{id}/watched` - Mark entire series as watched/unwatched
  - `POST /api/series/{id}/refresh` - Manually trigger season refresh

- **CatalogController**: REST endpoints for combined catalog operations
  - `GET /api/catalog` - Get combined list of movies and series
  - `GET /api/catalog/search` - Search across movies and series

- **RecommendationController**: REST endpoints for recommendations
  - `GET /api/recommendations` - Get next recommended movie/series to watch

- **NotificationController**: REST endpoints for notifications
  - `GET /api/notifications` - Get all notifications
  - `DELETE /api/notifications/{id}` - Dismiss notification

**Technologies**: Spring Web MVC, Spring Validation, SpringDoc OpenAPI

---

### 2. Service Layer (Business Logic)
**Responsibility**: Implement business rules, orchestrate operations, handle transactions

**Components**:

- **MovieService**
  - Add new movie with duplicate warning
  - Update movie information
  - Delete movie
  - Mark movie as watched/unwatched
  - Validate movie data

- **SeriesService**
  - Add new TV series with duplicate warning
  - Update series information
  - Delete series
  - Mark season as watched/unwatched
  - Mark entire series as watched/unwatched
  - Calculate series watch status based on seasons
  - Validate series data

- **CatalogService**
  - Retrieve combined catalog (movies + series)
  - Apply filters (genre, watch status, added by, content type, series-specific)
  - Apply sorting (title, length, date added)
  - Implement pagination (optional)
  - Search functionality across catalog

- **RecommendationService**
  - Implement weighted random recommendation algorithm
  - Priority 1: Series with newly released seasons (previous seasons watched)
  - Priority 2: Older unwatched movies/series (age-based weighting)
  - Priority 3: Recently added unwatched items
  - Filter out fully watched content
  - Consider only series with unwatched seasons

- **SeasonRefreshService**
  - Fetch season information from external sources (IMDB, Kinopoisk)
  - Update series with latest season data
  - Detect new seasons
  - Create notifications for new seasons
  - Handle refresh failures with retry logic
  - Skip series without link description URLs

- **NotificationService**
  - Create notifications for new seasons
  - Retrieve all active notifications
  - Dismiss notifications
  - Filter notifications (only for series with at least one watched season)
  - Manage notification persistence

- **ExternalApiService**
  - Interface with external movie/series data sources
  - Parse IMDB data
  - Parse Kinopoisk data
  - Handle HTTP requests with WebClient
  - Implement retry logic for failed requests
  - Extract season information from external sources

**Technologies**: Spring Core, Spring Transaction Management

---

### 3. Repository Layer (Data Access)
**Responsibility**: Database operations and data persistence

**Components**:

- **MovieRepository** (extends MongoRepository)
  - CRUD operations for movies
  - Custom queries: findByTitle, findByWatchStatus, findByGenre, findByAddedBy
  - Duplicate detection queries
  - Date-based sorting queries

- **SeriesRepository** (extends MongoRepository)
  - CRUD operations for series
  - Custom queries: findByTitle, findByHasNewSeasons, findBySeriesStatus
  - Duplicate detection queries
  - Season-specific queries
  - Date-based sorting queries

- **NotificationRepository** (extends MongoRepository)
  - CRUD operations for notifications
  - Query active notifications
  - Query by series ID
  - Deletion by ID

**Technologies**: Spring Data MongoDB

---

### 4. Domain Model Layer
**Responsibility**: Define data structures and business entities

**Components**:

- **Movie** (MongoDB Document)
  - id: String (MongoDB ObjectId)
  - title: String (required)
  - link: String (optional)
  - linkDescription: String (optional - URL to IMDB/Kinopoisk)
  - comment: String (optional)
  - coverImage: String (optional)
  - length: Integer (optional - in minutes)
  - genres: List<String> (optional)
  - watchStatus: WatchStatus enum (WATCHED, UNWATCHED)
  - addedBy: String (optional)
  - dateAdded: LocalDateTime (automatic)

- **Series** (MongoDB Document)
  - id: String (MongoDB ObjectId)
  - title: String (required)
  - link: String (optional)
  - linkDescription: String (optional - URL to IMDB/Kinopoisk)
  - comment: String (optional)
  - coverImage: String (optional)
  - genres: List<String> (optional)
  - seasons: List<Season> (collection of seasons)
  - seriesWatchStatus: WatchStatus enum (calculated automatically)
  - totalAvailableSeasons: Integer (fetched from external source)
  - hasNewSeasons: Boolean (automatic)
  - seriesStatus: SeriesStatus enum (COMPLETE, ONGOING)
  - addedBy: String (optional)
  - dateAdded: LocalDateTime (automatic)
  - lastSeasonCheck: LocalDateTime (automatic)

- **Season** (Embedded Document)
  - seasonNumber: Integer
  - watchStatus: WatchStatus enum (WATCHED, UNWATCHED)

- **Notification** (MongoDB Document)
  - id: String (MongoDB ObjectId)
  - seriesId: String (reference to Series)
  - seriesTitle: String
  - message: String
  - newSeasonsCount: Integer
  - createdAt: LocalDateTime
  - dismissed: Boolean

- **Enums**:
  - WatchStatus: WATCHED, UNWATCHED
  - SeriesStatus: COMPLETE, ONGOING
  - ContentType: MOVIE, SERIES

**Technologies**: Spring Data MongoDB Annotations, Lombok

---

### 5. DTO Layer (Data Transfer Objects)
**Responsibility**: Define API request/response structures

**Components**:

- **MovieRequest**: Add/update movie request
- **MovieResponse**: Movie details response
- **SeriesRequest**: Add/update series request
- **SeriesResponse**: Series details response
- **CatalogItemResponse**: Unified response for catalog list
- **CatalogFilterRequest**: Filter and sorting parameters
- **RecommendationResponse**: Recommendation details
- **NotificationResponse**: Notification details
- **SeasonRequest**: Season update request
- **ErrorResponse**: Standardized error response

**Technologies**: Lombok, Jackson

---

### 6. Mapper Layer
**Responsibility**: Convert between entities and DTOs

**Components**:
- **MovieMapper**: Movie entity ↔ MovieRequest/MovieResponse
- **SeriesMapper**: Series entity ↔ SeriesRequest/SeriesResponse
- **NotificationMapper**: Notification entity ↔ NotificationResponse
- **CatalogMapper**: Movie/Series entities → CatalogItemResponse

**Technologies**: Manual mapping or MapStruct (future enhancement)

---

### 7. Configuration Layer
**Responsibility**: Application configuration and bean definitions

**Components**:

- **WebClientConfig**
  - Configure WebClient for external API calls
  - Set timeouts, connection pooling
  - Define retry strategies

- **MongoConfig**
  - MongoDB connection configuration
  - Index creation for performance
  - Custom converters if needed

- **OpenApiConfig**
  - Swagger/OpenAPI documentation setup
  - API metadata (title, version, description)
  - Security schemes (if needed in future)

- **SchedulerConfig**
  - Configure scheduler thread pool
  - Define cron expressions from properties

**Technologies**: Spring Configuration, Spring Boot Auto-configuration

---

### 8. Scheduler Layer
**Responsibility**: Background tasks and scheduled jobs

**Components**:

- **SeasonRefreshScheduler**
  - Scheduled task (weekly on Monday at midnight)
  - Triggers SeasonRefreshService
  - Iterates through all series with link description URLs
  - Handles errors and continues processing
  - Logs refresh results

**Technologies**: Spring @Scheduled

---

### 9. Exception Handling Layer
**Responsibility**: Centralized error handling and custom exceptions

**Components**:

- **Global Exception Handler** (@RestControllerAdvice)
  - Handle validation errors
  - Handle not found exceptions
  - Handle external API failures
  - Handle MongoDB errors
  - Return standardized error responses

- **Custom Exceptions**:
  - MovieNotFoundException
  - SeriesNotFoundException
  - DuplicateEntryException (warning, not blocking)
  - ExternalApiException
  - SeasonRefreshException

**Technologies**: Spring Exception Handling

---

## Component Interaction Flow

### Example: Add New Movie
1. Client → MovieController.addMovie(MovieRequest)
2. Controller validates request
3. Controller → MovieService.addMovie()
4. Service checks for duplicates (warning if found)
5. Service → MovieMapper.toEntity()
6. Service → MovieRepository.save()
7. Repository → MongoDB
8. Service → MovieMapper.toResponse()
9. Service → Controller
10. Controller → Client (MovieResponse)

### Example: Get Recommendation
1. Client → RecommendationController.getRecommendation()
2. Controller → RecommendationService.getRecommendation()
3. Service → MovieRepository.findUnwatched()
4. Service → SeriesRepository.findWithUnwatchedSeasons()
5. Service applies weighted random algorithm with priorities
6. Service → CatalogMapper.toRecommendationResponse()
7. Service → Controller
8. Controller → Client (RecommendationResponse)

### Example: Scheduled Season Refresh
1. Scheduler triggers SeasonRefreshScheduler
2. Scheduler → SeasonRefreshService.refreshAllSeries()
3. Service → SeriesRepository.findAllWithLinkDescription()
4. For each series:
   - Service → ExternalApiService.fetchSeasonInfo()
   - ExternalApiService → WebClient → External API (IMDB/Kinopoisk)
   - Service compares with existing data
   - If new seasons: Service → NotificationService.createNotification()
   - Service → SeriesRepository.save()
5. Scheduler logs completion

---

## Data Flow Patterns

### Read Operations
Client → Controller → Service → Repository → MongoDB → Repository → Service → Mapper → Controller → Client

### Write Operations
Client → Controller → Service → Repository → MongoDB → Repository → Service → Mapper → Controller → Client

### Background Operations
Scheduler → Service → Repository → MongoDB
         → ExternalApiService → External API
         → NotificationService → Repository → MongoDB

---

## External Dependencies

1. **MongoDB**: Primary data store
2. **IMDB API/Website**: External source for movie/series data
3. **Kinopoisk API/Website**: External source for movie/series data (Russian movies)
4. **Spring Boot Actuator**: Health checks and metrics
5. **Swagger UI**: Interactive API documentation

---

## Security Considerations (Future)
- Currently: No authentication (single-user system)
- Future: Add Spring Security if multi-user support is needed
- API rate limiting for external sources
- Input validation to prevent injection attacks

---

## Performance Considerations

1. **Database Indexes**:
   - Index on title for fast searches
   - Index on dateAdded for recommendation sorting
   - Index on watchStatus for filtering

2. **Caching** (Future enhancement):
   - Cache external API responses
   - Cache recommendation calculations

3. **Pagination**:
   - Optional for large catalogs
   - Implement when catalog grows

4. **Async Processing**:
   - WebClient for non-blocking external API calls
   - Consider @Async for season refresh if needed

---

## Monitoring and Observability

1. **Spring Boot Actuator Endpoints**:
   - /actuator/health - Application health
   - /actuator/metrics - Application metrics
   - /actuator/info - Application information

2. **Logging**:
   - Logback with SLF4J
   - Log levels: DEBUG for development, INFO for production
   - Log external API calls and errors

3. **Metrics to Track**:
   - API response times
   - External API success/failure rates
   - Season refresh job execution times
   - Database query performance

---

## Testing Strategy

1. **Unit Tests**:
   - Service layer business logic
   - Mapper conversions
   - Recommendation algorithm

2. **Integration Tests**:
   - Controller endpoints with MockMvc
   - Repository operations with embedded MongoDB or TestContainers
   - Full application context loading

3. **Contract Tests** (Future):
   - External API interactions

---

## Future Architecture Enhancements

1. **Caching Layer**: Redis for external API responses
2. **Event-Driven Architecture**: Events for new seasons, watched status changes
3. **Message Queue**: For asynchronous processing of season refreshes
4. **API Gateway**: If service grows into microservices
5. **User Management**: Multi-user support with authentication
6. **Analytics Service**: Track watching patterns and statistics
