# Movie Catalog - Implementation TODO Plan

## Overview
This document tracks the implementation status of features from the raw requirements document.

**Status:** 31 of 49 features completed (Updated: Nov 22, 2025)

**Summary:**
- ✅ **Core CRUD APIs**: Complete for movies and series
- ✅ **Catalog & Search**: Implemented with filtering and sorting
- ✅ **Recommendations**: Weighted algorithm implemented
- ✅ **Notifications**: Basic infrastructure complete
- ✅ **Image Management**: Download, store, retrieve system complete
- ✅ **Series Season Management**: Create with seasons, update seasons, auto-status calculation
- ✅ **TMDB Integration**: Complete integration for season refresh (manual + bulk)
- ⚠️ **External API Integration**: TMDB implemented, IMDB/Kinopoisk scraping not implemented
- ❌ **Validation & Error Handling**: Minimal implementation
- ⚠️ **Frontend Season Management**: Season tracking, bulk operations, and manual refresh button all shipped, but notification UI still missing
- ✅ **Automated Season Checking**: Scheduler implemented (notifications integration pending)
- ❌ **Testing**: Minimal coverage

---

## ✅ Completed Features (30/49)

### Core Movie Operations
- [x] **Add new movie endpoint** - POST /api/movies - Create endpoint to add a new movie with all fields (title required, link, comment, coverImage, length, genre, addedBy optional). Include duplicate warning.
- [x] **Mark movie as watched/unwatched** - PATCH /api/movies/{id}/watch-status - Create endpoint to update watch status between WATCHED and UNWATCHED states.
- [x] **Set movie priority** - PATCH /api/movies/{id}/priority - Create endpoint to manually adjust priority (numeric value, default 0) for recommendation weighting.
- [x] **Edit movie capability** - PUT /api/movies/{id} - Update endpoint exists to edit movie information after creation.
- [x] **Delete movie capability** - DELETE /api/movies/{id} - Delete endpoint exists to remove movies from catalog.
- [x] **Get movie by ID** - GET /api/movies/{id} - Retrieve single movie details endpoint implemented.
- [x] **Get all movies** - GET /api/movies - List all movies endpoint implemented.

### Core Series Operations
- [x] **Add new TV series endpoint** - POST /api/series - **FIXED** - Now properly saves seasons array from request. Create endpoint to add a TV series with title (required), optional fields (link, comment, coverImage, genre, addedBy), and seasons array. Include duplicate warning.
- [x] **Mark series season as watched/unwatched** - PATCH /api/series/{id}/seasons/{seasonNumber}/watch-status - Create endpoint to mark individual seasons. Auto-creates season if doesn't exist. Series watch status auto-calculated (all seasons watched = WATCHED).
- [x] **Mark entire series as watched/unwatched** - PATCH /api/series/{id}/watch-status - Create endpoint to mark all seasons at once with given watch status.
- [x] **Set series priority** - PATCH /api/series/{id}/priority - Create endpoint to manually adjust series priority for recommendation weighting.
- [x] **Edit series capability** - PUT /api/series/{id} - **UPDATED** - Now supports updating seasons array. Update endpoint exists to edit series information after creation.
- [x] **Delete series capability** - DELETE /api/series/{id} - Delete endpoint exists to remove series from catalog.
- [x] **Get series by ID** - GET /api/series/{id} - Retrieve single series details endpoint implemented.
- [x] **Get all series** - GET /api/series - List all series endpoint implemented.

### Catalog & Search
- [x] **Retrieve full catalog list** - GET /api/catalog - Implement unified endpoint returning both movies and series with filtering (genre, watchStatus, addedBy, contentType, hasNewSeasons, seriesStatus) and sorting (unwatched first, then priority, then dateAdded).
- [x] **Search functionality** - GET /api/catalog/search?query={query} - Search endpoint exists to find movies/series by title or other attributes.
- [x] **Duplicate prevention with warning** - Implement duplicate detection by title (case-insensitive) in MovieService and SeriesService. Log warning but allow addition.

### Recommendations
- [x] **Get movie recommendation** - GET /api/recommendations - Implement weighted random recommendation algorithm considering: manual priority > series with new seasons > age-based weighting for both movies and series.

### Notifications
- [x] **Get all notifications endpoint** - GET /api/notifications - Endpoint exists and returns active notifications. Verify filtering logic for series with watched seasons.
- [x] **Dismiss notification endpoint** - DELETE /api/notifications/{id} - Endpoint exists to dismiss individual notifications by marking dismissed=true.

### TMDB Integration
- [x] **TMDB API Service** - Complete WebClient-based integration with TMDB API implemented in TmdbApiService. Fetches movie/series metadata, season counts, and cover images.
- [x] **Manual season refresh endpoint** - POST /api/series/{id}/refresh - Fully implemented in SeriesService.refreshSeasons() with TMDB API integration. Updates totalAvailableSeasons, seriesStatus, hasNewSeasons, and syncs season list.
- [x] **Bulk refresh endpoint** - POST /api/series/refresh-all - Implemented in SeriesService.refreshAllSeriesWithTmdbId(). Returns BulkRefreshResponse with success/failure counts.
- [x] **TMDB enrichment endpoints** - POST /api/tmdb/enrich/movie and /api/tmdb/enrich/series for fetching metadata during content creation.

### Frontend Features
- [x] **Frontend - Bulk season operations** - UI controls to mark entire series as watched/unwatched via PATCH /api/series/{id}/watch-status. Implemented in SeasonList.jsx with "Mark All Watched"/"Mark All Unwatched" buttons, progress tracking, and disabled state logic.
- [x] **Frontend - Manual season refresh button** - "Fetch Seasons" button in SeasonList.jsx triggers POST /api/series/{id}/refresh with loading state and error handling. Only shown when series has TMDB link.

### Infrastructure
- [x] **Database indexes for performance** - Verify MongoDB indexes are created on title, watchStatus, dateAdded, addedBy, hasNewSeasons, seriesStatus, priority fields using @Indexed annotations.
- [x] **API documentation with Swagger** - Verify all endpoints have proper Swagger annotations (@Operation, @Tag) for OpenAPI documentation. Endpoints appear to have this.

---

## ❌ Pending Implementation (18/49)

### Automated Season Checking (Critical Gap)
- [x] **Create SeasonRefreshScheduler** - Implemented @Scheduled task in `SeasonRefreshScheduler.java` that runs weekly (Monday midnight) calling SeriesService.refreshAllSeriesWithTmdbId(). Configured with cron expression: scheduler.cron.season-check=0 0 0 * * MON in application.properties. @EnableScheduling already enabled in MovieCatalogApplication.
- [ ] **Notification creation logic during refresh** - Modify SeriesService.refreshSeasons() to check if series has ≥1 watched season. When new seasons detected (hasNewSeasons=true), call NotificationService.createNotification() with series details and new season count.
- [ ] **Integration notification logic into bulk refresh** - Update SeriesService.refreshAllSeriesWithTmdbId() to track and create notifications for series with new seasons and watched content.

### External API Integration (Optional Enhancement)
- [ ] **IMDB/Kinopoisk web scraping** - Optional: Implement web scraping service for IMDB/Kinopoisk as fallback when TMDB data unavailable. Currently TMDB integration is complete and sufficient.

#### Configuration & Error Handling
- [x] **Configure WebClient for TMDB API** - TmdbWebClient configured in TmdbConfig with timeout settings and base URL. Uses Spring WebFlux WebClient (non-blocking).
- [x] **Configure scheduler settings** - Already configured via `scheduler.cron.season-check` in `src/main/resources/application.properties`.
- [ ] **Enable scheduled tasks** - Add @EnableScheduling annotation to main application class (MovieCatalogApplication).
- [ ] **Exception handling for external API** - Create custom exceptions (ExternalApiException, SeasonRefreshException) and add handling in GlobalExceptionHandler (@RestControllerAdvice) for TMDB API failures with appropriate error responses.
- [ ] **Retry logic for failed refresh** - Consider implementing retry mechanism using Spring Retry or manual retry with exponential backoff for TMDB API unavailability.

### Frontend Features

#### UI Components
- [x] **Frontend - Recommendation UI** - Implemented in `frontend/src/components/RecommendationsBlock.jsx` (rendered from `App.jsx`) with loading/error states around GET `/api/recommendations`.
- [ ] **Frontend - Notification display** - Create UI component to display active notifications about new seasons via GET /api/notifications. Show notification list with series title, message, new season count. Allow dismissal via DELETE /api/notifications/{id}. Show badge/count of unread notifications in header.
- [x] **Frontend - Series season management** - `frontend/src/components/SeasonList.jsx` plus `CatalogList.jsx` expand/collapse affordances already allow per-season PATCH operations and progress display.
- [x] **Frontend - Manual season refresh button** - "Fetch Seasons" button in `SeasonList.jsx` calls POST /api/series/{id}/refresh with loading state, error handling, and TMDB link detection.
- [x] **Frontend - Add seasons during series creation** - The series branch of `frontend/src/components/AddMovieModal.jsx` requires number of seasons and seeds the POST `/api/series` body with an initial seasons array.
- [x] **Frontend - Bulk season operations** - Existing SeasonList controls include "Mark All Watched/Unwatched" buttons wired to PATCH `/api/series/{id}/watch-status`.
- [x] **Frontend - Bulk refresh all series** - "Refresh All Series" button in App.jsx header calls POST /api/series/refresh-all with loading state and displays result notification.

---

## New Missing Requirements Identified (Nov 14, 2025)

### Backend Validation & Error Handling
- [ ] **Request validation** - Add @Valid annotation to controller request bodies and implement validation constraints in DTOs (e.g., @NotBlank for title, @Min/@Max for seasonNumber, enum validation for watchStatus). Return 400 Bad Request with validation errors. Note: ImageController already uses @Valid on ImageDownloadRequest.
- [ ] **Global exception handler** - Create @RestControllerAdvice class to handle common exceptions: ResourceNotFoundException (404), ValidationException (400), IllegalArgumentException (400), general Exception (500), WebClientResponseException (TMDB API failures). Return standardized error response format.
- [ ] **Series link validation** - Validate that link field contains valid TMDB/IMDB/Kinopoisk URL pattern when provided. Return meaningful error if invalid URL format.

### Data Consistency & Business Rules
- [ ] **Prevent season number duplicates** - Add validation in SeriesService to prevent adding duplicate season numbers to the same series. When using PATCH to add season, check if seasonNumber already exists.
- [ ] **Cascade delete** - When deleting a series, ensure associated notifications are also deleted or marked as inactive. Add cascade logic to SeriesService.deleteSeries().
- [x] **Series watch status recalculation trigger** - `SeriesService` already calls `updateSeriesWatchStatus()` in `addSeason`, `removeLastSeason`, `updateSeasonWatchStatus`, and `updateSeriesWatchStatus` to keep state consistent (unit tests still missing).

### Repository Layer Enhancements
- [x] **SeriesRepository custom queries** - `SeriesRepository.java` already exposes `findByHasNewSeasons`, `findBySeriesStatus`, and `findAllWithLink()` for scheduler-driven refresh.
- [ ] **Movie priority query** - Add `findByPriorityGreaterThan(int)` to `MovieRepository` to support weighting/reporting use cases.
- [x] **Case-insensitive duplicate detection** - Both repositories include `findByTitleIgnoreCase()` and the add service methods log duplicate warnings before insert.

### Documentation & Testing
- [ ] **API error response documentation** - Document all possible error responses in Swagger annotations (@ApiResponse). Include HTTP status codes, error message formats, validation error structures.
- [ ] **Integration tests for series management** - Create integration tests for: creating series with seasons, updating seasons preserves other data, watch status calculation, bulk season operations, season auto-creation on PATCH.
- [ ] **Unit tests for recommendation algorithm** - Test recommendation weighting: manual priority multiplier, new seasons 10x boost, age-based logarithmic weighting, weighted random selection.

---

## Implementation Priority

### Phase 1: Complete Automated Season Tracking (High Priority) ⭐
1. Add @EnableScheduling to MovieCatalogApplication
2. Create SeasonRefreshScheduler with @Scheduled task (weekly Monday midnight)
3. Implement notification creation in SeriesService.refreshSeasons() (check for ≥1 watched season)
4. Integrate notification logic into bulk refresh (refreshAllSeriesWithTmdbId)

### Phase 2: Frontend Notification UI (High Priority)
5. Create NotificationPanel component with badge count in header
6. Display notification list with series details and dismissal buttons
7. Add real-time notification polling or refresh on catalog updates

### Phase 3: Backend Validation & Stability (Medium Priority)
8. Add request validation (@Valid, constraints in DTOs)
9. Create global exception handler (@RestControllerAdvice)
10. Add business rule validations (duplicate seasons, TMDB link validation)
11. Implement cascade delete for series + notifications
12. Add retry logic for TMDB API failures

### Phase 4: Testing & Documentation (Medium Priority)
13. Write integration tests for series management workflows
14. Write unit tests for recommendation algorithm
15. Write tests for TMDB integration and season refresh logic
16. Add API error documentation to Swagger annotations
17. Document notification workflow in architecture docs

### Phase 5: Optional Enhancements (Low Priority)
18. Add IMDB/Kinopoisk web scraping as fallback (TMDB is primary)
19. Implement advanced retry strategies with exponential backoff
20. Add custom repository queries (findByPriorityGreaterThan)
21. Performance optimization and caching for TMDB responses

---

## Notes

### Recent Updates (Nov 22, 2025)
- ✅ **TMDB Integration Complete** - Full TmdbApiService implementation with WebClient for movie/series metadata
- ✅ **Manual Season Refresh** - SeriesService.refreshSeasons() fully implemented with TMDB integration
- ✅ **Bulk Refresh Endpoint** - POST /api/series/refresh-all with success/failure tracking
- ✅ **Frontend Refresh Button** - SeasonList.jsx "Fetch Seasons" button with loading states
- ✅ **Frontend Bulk Refresh** - App.jsx header button for refreshing all series

### Architecture Status
The backend architecture and core CRUD operations are solid and complete. TMDB integration is fully implemented. The primary remaining gaps are:
- **No automated scheduler** - SeasonRefreshScheduler not implemented (manual/bulk refresh works, but no weekly automation)
- **No notification creation during refresh** - When new seasons detected, notifications not automatically created
- **Frontend notification UI missing** - Backend notifications API ready, but no UI to display/dismiss them
- **No validation layer** - Missing @Valid annotations and @RestControllerAdvice for error handling

### Validation & Error Handling Gaps
Currently missing comprehensive validation and error handling:
- No @Valid annotations on controller request bodies (except ImageController)
- No global exception handler (@RestControllerAdvice)
- No business rule validations (duplicate seasons, TMDB link format)
- No standardized error response format
- Limited API error documentation in Swagger
- No retry logic for TMDB API failures

### TMDB Integration Status
✅ **Complete:**
- TmdbApiService with WebClient (non-blocking)
- Movie/series metadata enrichment
- Season count and status fetching
- Manual refresh endpoint (POST /api/series/{id}/refresh)
- Bulk refresh endpoint (POST /api/series/refresh-all)
- Frontend refresh buttons (per-series and bulk)

❌ **Missing:**
- Automated weekly scheduler (@Scheduled task)
- Notification creation when new seasons detected
- Retry logic and circuit breaker patterns

### Frontend Status
✅ **Complete:**
- Catalog filtering and search
- RecommendationsBlock with loading states
- SeasonList with per-season tracking
- Bulk season operations (Mark All Watched/Unwatched)
- Manual refresh button ("Fetch Seasons")
- Bulk refresh all series button
- AddMovieModal with season creation

❌ **Missing:**
- Notification display component (badge + list)
- Notification dismissal UI

### Testing Status
Limited test coverage:
- Basic application context test exists
- SeriesServiceTest has 3 unit tests for refreshSeasons() method
- Integration tests exist for controllers (MovieController, SeriesController, CatalogController, RecommendationController)
- No integration tests for TMDB API integration
- No tests for notification creation workflow
- No tests for recommendation algorithm weighting
- No tests for automated scheduler (when implemented)

### Repository Layer
✅ **Complete:**
- SeriesRepository: `findByHasNewSeasons`, `findBySeriesStatus`, `findAllWithLink()`
- Both repositories: `findByTitleIgnoreCase()` for duplicate detection

❌ **Missing:**
- `findByPriorityGreaterThan(int)` on MovieRepository for priority-based filtering

---

## Next Steps Priority

### Immediate Focus (Complete Automation Loop)
1. **Create SeasonRefreshScheduler** - @Scheduled task calling bulk refresh weekly
2. **Add notification creation** - Modify refreshSeasons() to create notifications for watched series
3. **Frontend notification UI** - Display notifications with badge count and dismissal

These 3 tasks complete the core automated season tracking feature described in requirements.

### After Automation
4. Add @Valid validation and @RestControllerAdvice error handling
5. Write integration tests for notification workflow
6. Add retry logic for TMDB API resilience

```
