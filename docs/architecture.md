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
  - comment: String (optional)
  - coverImage: String (optional)
  - genres: List<String> (optional)
  - seasons: List<Season> (collection of seasons, never empty; backend auto-creates season 1/UNWATCHED if client omits the list)
  - watchStatus: WatchStatus enum (calculated automatically)
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
  - Iterates through all series
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
3. Service → SeriesRepository.findAll()
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

## Series Management Guide

### Overview
TV Series have more complex management than movies due to season tracking, automatic status calculation, and external data synchronization. This section provides comprehensive guidance for working with series.

---

### API Endpoints

#### 1. Create New Series
**Endpoint**: `POST /api/series`

**Request Body** (`SeriesRequest`):
```json
{
  "title": "Breaking Bad",
  "link": "https://imdb.com/title/tt0903747",
  "comment": "Highly recommended by friends",
  "coverImage": "/api/images/abc123",
  "genres": ["Crime", "Drama", "Thriller"],
  "seasons": [
    {"seasonNumber": 1, "watchStatus": "WATCHED"},
    {"seasonNumber": 2, "watchStatus": "UNWATCHED"}
  ],
  "addedBy": "John",
  "priority": 5
}
```

**Field Descriptions**:
- `title` (String, required): Series title
- `link` (String, optional): URL for IMDB/Kinopoisk page (used for season refresh)
- `comment` (String, optional): Personal notes or review
- `coverImage` (String, optional): Image reference (use `/api/images/{id}` format)
- `genres` (List<String>, optional): Genre tags (e.g., ["Drama", "Comedy"])
- `seasons` (List<Season>, optional): Initial seasons with watch status
  - `seasonNumber` (Integer): Season number (1, 2, 3, etc.)
  - `watchStatus` (String): "WATCHED" or "UNWATCHED"
- `addedBy` (String, optional): Person who added the series
- `priority` (Integer, optional): Manual priority for recommendations (default: 0)

**Response** (`SeriesResponse`):
```json
{
  "id": "507f1f77bcf86cd799439011",
  "title": "Breaking Bad",
  "link": "https://imdb.com/title/tt0903747",
  "comment": "Highly recommended by friends",
  "coverImage": "/api/images/abc123",
  "genres": ["Crime", "Drama", "Thriller"],
  "seasons": [
    {"seasonNumber": 1, "watchStatus": "WATCHED"},
    {"seasonNumber": 2, "watchStatus": "UNWATCHED"}
  ],
  "watchStatus": "UNWATCHED",
  "totalAvailableSeasons": null,
  "hasNewSeasons": false,
  "seriesStatus": null,
  "addedBy": "John",
  "dateAdded": "2025-11-14T10:30:00",
  "lastSeasonCheck": null,
  "priority": 5
}
```

**Important Notes**:
- `watchStatus` is auto-calculated: WATCHED if all seasons are watched, otherwise UNWATCHED
- `seasons` can be empty initially and added later via season watch status updates
- If no seasons provided, `watchStatus` defaults to UNWATCHED
- `hasNewSeasons` is false until external refresh detects new seasons

---

#### 2. Update Series Information
**Endpoint**: `PUT /api/series/{id}`

**Request Body**: Same as `POST /api/series` (SeriesRequest)

**Behavior**:
- Updates all provided fields
- If `seasons` array is provided, replaces existing seasons entirely
- If `seasons` is null or omitted, existing seasons are preserved
- `watchStatus` is recalculated if seasons are updated
- Does NOT affect auto-managed fields: `totalAvailableSeasons`, `hasNewSeasons`, `seriesStatus`, `lastSeasonCheck`

**Example - Update without affecting seasons**:
```json
{
  "title": "Breaking Bad (Updated)",
  "comment": "One of the best series ever",
  "genres": ["Crime", "Drama"],
  "priority": 10
}
```

---

#### 3. Mark Individual Season as Watched/Unwatched
**Endpoint**: `PATCH /api/series/{id}/seasons/{seasonNumber}/watch-status`

**Request Body** (`WatchStatusRequest`):
```json
{
  "watchStatus": "WATCHED"
}
```

**Behavior**:
- If season doesn't exist, creates it automatically with the specified watch status
- Updates watch status of existing season
- Recalculates `watchStatus` based on all seasons
- Returns updated series with all seasons

**Example Response**:
```json
{
  "id": "507f1f77bcf86cd799439011",
  "title": "Breaking Bad",
  "seasons": [
    {"seasonNumber": 1, "watchStatus": "WATCHED"},
    {"seasonNumber": 2, "watchStatus": "WATCHED"},
    {"seasonNumber": 3, "watchStatus": "UNWATCHED"}
  ],
  "watchStatus": "UNWATCHED",
  ...
}
```

---

#### 4. Mark Entire Series as Watched/Unwatched
**Endpoint**: `PATCH /api/series/{id}/watch-status`

**Request Body** (`WatchStatusRequest`):
```json
{
  "watchStatus": "WATCHED"
}
```

**Behavior**:
- Updates watch status of ALL existing seasons to the specified value
- Recalculates `watchStatus` (will match the provided status)
- Does NOT create new seasons - only affects existing seasons
- Useful for marking entire series as watched or resetting to unwatched

**Use Cases**:
- Mark all seasons as watched after binge-watching
- Reset series to unwatched for rewatching

---

#### 5. Update Series Priority
**Endpoint**: `PATCH /api/series/{id}/priority`

**Request Body** (`PriorityRequest`):
```json
{
  "priority": 10
}
```

**Behavior**:
- Updates manual priority for recommendations
- Higher values increase recommendation probability
- Default priority is 0
- Can be negative to lower recommendation probability

---

#### 6. Manually Refresh Seasons
**Endpoint**: `POST /api/series/{id}/refresh`

**Request Body**: None

**Behavior**:
- Triggers manual season refresh from external source (IMDB/Kinopoisk)
- Requires `link` field to be set on the series
- Updates `totalAvailableSeasons`, `seriesStatus`, and `hasNewSeasons` flags
- Updates `lastSeasonCheck` timestamp
- Currently a placeholder - full implementation pending

---

#### 7. Get Series by ID
**Endpoint**: `GET /api/series/{id}`

**Response**: Complete series details (SeriesResponse)

---

#### 8. Get All Series
**Endpoint**: `GET /api/series`

**Response**: Array of all series (List<SeriesResponse>)

---

#### 9. Delete Series
**Endpoint**: `DELETE /api/series/{id}`

**Response**: HTTP 204 No Content

**Behavior**:
- Permanently removes series from database
- Removes all associated seasons
- Cannot be undone

---

### Season Management Patterns

#### Pattern 1: Add Series with Initial Seasons
```bash
# Create series with first 3 seasons
curl -X POST http://localhost:8080/api/series \
  -H "Content-Type: application/json" \
  -d '{
    "title": "The Wire",
    "seasons": [
      {"seasonNumber": 1, "watchStatus": "UNWATCHED"},
      {"seasonNumber": 2, "watchStatus": "UNWATCHED"},
      {"seasonNumber": 3, "watchStatus": "UNWATCHED"}
    ]
  }'
```

#### Pattern 2: Add Series Without Seasons, Add Later
```bash
# Step 1: Create series without seasons
curl -X POST http://localhost:8080/api/series \
  -H "Content-Type: application/json" \
  -d '{"title": "The Sopranos"}'

# Step 2: Add seasons one by one as you discover them
curl -X PATCH http://localhost:8080/api/series/{id}/seasons/1/watch-status \
  -H "Content-Type: application/json" \
  -d '{"watchStatus": "UNWATCHED"}'
```

#### Pattern 3: Progressive Season Tracking
```bash
# Mark season 1 as watched
curl -X PATCH http://localhost:8080/api/series/{id}/seasons/1/watch-status \
  -H "Content-Type: application/json" \
  -d '{"watchStatus": "WATCHED"}'

# Mark season 2 as watched
curl -X PATCH http://localhost:8080/api/series/{id}/seasons/2/watch-status \
  -H "Content-Type: application/json" \
  -d '{"watchStatus": "WATCHED"}'

# Series remains UNWATCHED until all seasons marked
```

#### Pattern 4: Bulk Update All Seasons
```bash
# Mark entire series as watched (affects all existing seasons)
curl -X PATCH http://localhost:8080/api/series/{id}/watch-status \
  -H "Content-Type: application/json" \
  -d '{"watchStatus": "WATCHED"}'
```

#### Pattern 5: Update Seasons via PUT
```bash
# Replace all seasons at once
curl -X PUT http://localhost:8080/api/series/{id} \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Game of Thrones",
    "seasons": [
      {"seasonNumber": 1, "watchStatus": "WATCHED"},
      {"seasonNumber": 2, "watchStatus": "WATCHED"},
      {"seasonNumber": 3, "watchStatus": "WATCHED"},
      {"seasonNumber": 4, "watchStatus": "UNWATCHED"},
      {"seasonNumber": 5, "watchStatus": "UNWATCHED"}
    ]
  }'
```

---

### Watch Status Calculation Rules

**Series Watch Status Logic**:
1. Service enforces at least one season; if the client omits `seasons`, season 1 is inserted with `UNWATCHED`
2. If all seasons have `watchStatus = WATCHED` → `watchStatus = WATCHED`
3. If any season has `watchStatus = UNWATCHED` → `watchStatus = UNWATCHED`

**Automatic Recalculation Triggers**:
- When series is created with seasons
- When season watch status is updated (individual or bulk)
- When seasons array is updated via PUT request
- After external season refresh (if implemented)

**Important Behaviors**:
- Users can skip seasons (mark Season 3 as watched without watching Season 2)
- There's no enforced season order
- Seasons are marked manually - no episode-level tracking
- Adding a new unwatched season to a fully watched series changes status to UNWATCHED

---

### Best Practices

#### For Initial Setup
1. **With Known Seasons**: Include all seasons in POST request
2. **Unknown Seasons**: Create series with `link` field, trigger manual refresh to fetch seasons
3. **Gradual Tracking**: Create without seasons, add via PATCH as you watch

#### For Season Updates
1. **Individual Updates**: Use `PATCH /seasons/{seasonNumber}/watch-status` for single season changes
2. **Bulk Updates**: Use `PATCH /watch-status` for marking entire series
3. **Complete Replacement**: Use `PUT` with full seasons array for restructuring

#### For External Integration
1. Always set `link` field to IMDB or Kinopoisk URL
2. Use `POST /{id}/refresh` to manually sync seasons
3. Automatic refresh runs weekly (Mondays at midnight)
4. External data populates: `totalAvailableSeasons`, `seriesStatus`, `hasNewSeasons`

#### For Recommendations
1. Set higher `priority` for series you're actively watching
2. Series with `hasNewSeasons=true` automatically get high recommendation weight
3. Only series with unwatched seasons appear in recommendations
4. Marking all seasons as watched removes series from recommendations

---

### Common Scenarios

#### Scenario 1: Starting a New Series
```json
// POST /api/series
{
  "title": "Stranger Things",
  "link": "https://www.imdb.com/title/tt4574334",
  "seasons": [
    {"seasonNumber": 1, "watchStatus": "UNWATCHED"}
  ],
  "priority": 8
}
```

#### Scenario 2: Finished Season, Moving to Next
```json
// PATCH /api/series/{id}/seasons/1/watch-status
{"watchStatus": "WATCHED"}

// Then add next season
// PATCH /api/series/{id}/seasons/2/watch-status
{"watchStatus": "UNWATCHED"}
```

#### Scenario 3: Series Complete, Mark All Watched
```json
// PATCH /api/series/{id}/watch-status
{"watchStatus": "WATCHED"}
```

#### Scenario 4: Rewatch Series
```json
// PATCH /api/series/{id}/watch-status
{"watchStatus": "UNWATCHED"}
// All seasons reset to unwatched
```

#### Scenario 5: Skip to Latest Season
```json
// Can mark any season without prerequisite
// PATCH /api/series/{id}/seasons/5/watch-status
{"watchStatus": "WATCHED"}
// Seasons 1-4 remain unwatched, series status = UNWATCHED
```

---

### Troubleshooting

**Problem**: Seasons not saving when creating series
- **Solution**: Ensure `seasons` array is included in POST request body
- **Solution**: Verify JSON structure matches Season model (seasonNumber + watchStatus)

**Problem**: Series watch status not updating
- **Solution**: Check that `updateSeriesWatchStatus()` is called after season changes
- **Solution**: Verify all seasons are properly saved in database

**Problem**: Manual refresh not working
- **Solution**: Ensure `link` field is set with valid IMDB/Kinopoisk URL
- **Solution**: Check external API service implementation status

**Problem**: Series not appearing in recommendations
- **Solution**: Ensure at least one season has `watchStatus = UNWATCHED`
- **Solution**: Check if `watchStatus = UNWATCHED`

---

## Future Architecture Enhancements

1. **Caching Layer**: Redis for external API responses
2. **Event-Driven Architecture**: Events for new seasons, watched status changes
3. **Message Queue**: For asynchronous processing of season refreshes
4. **API Gateway**: If service grows into microservices
5. **User Management**: Multi-user support with authentication
6. **Analytics Service**: Track watching patterns and statistics

```
