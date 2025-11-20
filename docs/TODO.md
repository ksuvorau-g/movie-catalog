# Movie Catalog - Implementation TODO Plan

## Overview
This document tracks the implementation status of features from the raw requirements document.

**Status:** 26 of 49 features completed (Updated: Nov 19, 2025)

**Summary:**
- ✅ **Core CRUD APIs**: Complete for movies and series
- ✅ **Catalog & Search**: Implemented with filtering and sorting
- ✅ **Recommendations**: Weighted algorithm implemented
- ✅ **Notifications**: Basic infrastructure complete
- ✅ **Image Management**: Download, store, retrieve system complete
- ✅ **Series Season Management**: Create with seasons, update seasons, auto-status calculation
- ❌ **External API Integration**: Not implemented (critical gap)
- ❌ **Validation & Error Handling**: Minimal implementation
- ⚠️ **Frontend Season Management**: Season tracking + bulk operations shipped, but notification UI and manual refresh controls still missing
- ❌ **Testing**: Minimal coverage

---

## ✅ Completed Features (22/36)

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

### Frontend Features
- [x] **Frontend - Bulk season operations** - UI controls to mark entire series as watched/unwatched via PATCH /api/series/{id}/watch-status. Implemented in SeasonList.jsx with "Mark All Watched"/"Mark All Unwatched" buttons, progress tracking, and disabled state logic.

### Infrastructure
- [x] **Database indexes for performance** - Verify MongoDB indexes are created on title, watchStatus, dateAdded, addedBy, hasNewSeasons, seriesStatus, priority fields using @Indexed annotations.
- [x] **API documentation with Swagger** - Verify all endpoints have proper Swagger annotations (@Operation, @Tag) for OpenAPI documentation. Endpoints appear to have this.

---

## ❌ Pending Implementation (14/36)

### External API Integration (Critical Gap)

#### Backend - External API Services
- [ ] **Create ExternalApiService** - Implement service to fetch season information from external sources (IMDB, Kinopoisk) using WebClient. Include parsing logic for both sources and retry logic for failures. Web scraping from link URLs.
- [ ] **Create SeasonRefreshService** - Implement service to update series with latest season data from external APIs. Detect new seasons by comparing totalAvailableSeasons. Create notifications for series with at least 1 watched season. Set hasNewSeasons flag. Handle refresh failures.
- [ ] **Create SeasonRefreshScheduler** - Implement @Scheduled task to run weekly (Monday midnight) that triggers SeasonRefreshService for all series with link set. Configure cron expression: scheduler.cron.season-check=0 0 0 * * MON in application.properties.
- [ ] **Manual season refresh endpoint** - POST /api/series/{id}/refresh - Implement actual external API call in SeriesService.refreshSeasons() (currently placeholder). Should fetch latest season data from link URL and update totalAvailableSeasons, seriesStatus, hasNewSeasons.
- [ ] **Notification creation logic** - Implement logic in SeasonRefreshService to create notifications only for series where at least one season has watchStatus=WATCHED when new seasons are detected. Use NotificationService.createNotification().

#### Configuration
- [ ] **Configure WebClient for external APIs** - Create WebClientConfig with timeout settings, connection pooling, and retry strategies for IMDB/Kinopoisk web scraping via WebClient (non-blocking).
- [x] **Configure scheduler settings** - Already configured via `scheduler.cron.season-check` in `src/main/resources/application.properties` and `@EnableScheduling` on `MovieCatalogApplication`.
- [ ] **Exception handling for external API** - Create custom exceptions (ExternalApiException, SeasonRefreshException) and add handling in GlobalExceptionHandler (@RestControllerAdvice) for external API failures with appropriate error responses.
- [ ] **Retry logic for failed refresh** - Implement retry mechanism in ExternalApiService that retries failed requests. Consider using Spring Retry or manual retry with exponential backoff for external source unavailability.

### Frontend Features

#### UI Components
- [x] **Frontend - Recommendation UI** - Implemented in `frontend/src/components/RecommendationsBlock.jsx` (rendered from `App.jsx`) with loading/error states around GET `/api/recommendations`.
- [ ] **Frontend - Notification display** - Create UI component to display active notifications about new seasons via GET /api/notifications. Show notification list with series title, message, new season count. Allow dismissal via DELETE /api/notifications/{id}. Show badge/count of unread notifications.
- [x] **Frontend - Series season management** - `frontend/src/components/SeasonList.jsx` plus `CatalogList.jsx` expand/collapse affordances already allow per-season PATCH operations and progress display.
- [ ] **Frontend - Manual season refresh button** - Add "Refresh Seasons" button to series details to manually trigger season refresh via POST /api/series/{id}/refresh. Show loading state during refresh. Handle errors (no link, API failure).
- [x] **Frontend - Add seasons during series creation** - The series branch of `frontend/src/components/AddMovieModal.jsx` requires number of seasons and seeds the POST `/api/series` body with an initial seasons array.
- [x] **Frontend - Bulk season operations** - Existing SeasonList controls include "Mark All Watched/Unwatched" buttons wired to PATCH `/api/series/{id}/watch-status`.

---

## New Missing Requirements Identified (Nov 14, 2025)

### Backend Validation & Error Handling
- [ ] **Request validation** - Add @Valid annotation to controller request bodies and implement validation constraints in DTOs (e.g., @NotBlank for title, @Min/@Max for seasonNumber, enum validation for watchStatus). Return 400 Bad Request with validation errors.
- [ ] **Global exception handler** - Create @RestControllerAdvice class to handle common exceptions: ResourceNotFoundException (404), ValidationException (400), IllegalArgumentException (400), general Exception (500). Return standardized error response format.
- [ ] **Series link validation** - Validate that link field contains valid IMDB or Kinopoisk URL pattern when provided. Return meaningful error if invalid URL format.

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

### Phase 1: External API Foundation (High Priority)
1. Configure WebClient for external APIs with timeout and retry
2. Create ExternalApiService with IMDB/Kinopoisk web scraping (link URLs)
3. Implement exception handling for external API failures (custom exceptions + global handler)
4. Create SeasonRefreshService with new season detection (compare totalAvailableSeasons)

### Phase 2: Automation (High Priority)
5. Create SeasonRefreshScheduler for weekly checks (Mondays midnight, process series with link)
6. Implement notification creation logic (only for series with ≥1 watched season)
7. Complete manual season refresh endpoint (POST /api/series/{id}/refresh with actual scraping)
8. Add retry logic for failed refreshes (exponential backoff, log failures)

### Phase 3: Backend Validation & Stability (High Priority)
9. Add request validation (@Valid, constraints in DTOs)
10. Create global exception handler (@RestControllerAdvice)
11. Add business rule validations (duplicate seasons, link validation)
12. Implement cascade delete for series + notifications

### Phase 4: Frontend Season Management (Medium Priority)
13. ✅ Create SeasonList component for season tracking (SeasonList.jsx)
14. ✅ Add season inputs to series creation form (AddMovieModal.jsx series flow)
15. ✅ Implement bulk season operations UI (SeasonList bulk buttons)
16. Add manual season refresh button with loading state

### Phase 5: Frontend Notifications & Recommendations (Medium Priority)
17. Create notification display component with badge
18. Add notification dismissal functionality
19. ✅ Create recommendation UI component (RecommendationsBlock)
20. Handle recommendation edge cases (no unwatched content)

### Phase 6: Testing & Documentation (Medium Priority)
21. Write integration tests for series management
22. Write unit tests for recommendation algorithm
23. Add API error documentation to Swagger
24. Add custom repository queries

---

## Notes

### Recent Fixes (Nov 14, 2025)
- ✅ **Series seasons not saving** - Fixed SeriesRequest to include seasons field, updated SeriesService.addSeries() to use seasons from request
- ✅ **Series documentation** - Created comprehensive SERIES_MANAGEMENT_GUIDE.md with API docs, patterns, troubleshooting
- ✅ **Architecture documentation** - Added Series Management Guide section to architecture.md with complete API reference

### Architecture Gaps
The backend architecture and core CRUD operations are solid and complete. The primary feature gap is the **entire external API integration system** for automated season tracking:
- No web scraping implementation for IMDB/Kinopoisk (link URLs)
- No scheduled background jobs for season checking (SeasonRefreshScheduler)
- No external API service layer (ExternalApiService)
- Manual refresh endpoint exists but has placeholder implementation only (SeriesService.refreshSeasons())
- Notification creation exists but not integrated with season refresh workflow

### Validation & Error Handling Gaps
Currently missing comprehensive validation and error handling:
- No @Valid annotations on controller request bodies
- No global exception handler (@RestControllerAdvice)
- No business rule validations (duplicate seasons, link format)
- No standardized error response format
- Limited API error documentation in Swagger

### Critical Dependencies
- External API integration is required for:
  - Automated season detection (comparing totalAvailableSeasons)
  - New season notifications (only for series with ≥1 watched season)
  - Series status tracking (COMPLETE vs ONGOING)
  - Total available seasons count (fetched from IMDB/Kinopoisk)

### Frontend Status
The frontend now includes catalog filtering, `RecommendationsBlock`, `SeasonList` management with bulk toggles, and AddMovieModal support for seeding series seasons. Remaining gaps:
- Notification system UI (display, badge, dismissal)
- Manual season refresh trigger in the UI
- Surface failures/responses from the placeholder POST `/api/series/{id}/refresh`

### Testing Status
Limited test coverage:
- Basic application context test exists
- No integration tests for series management workflows
- No unit tests for recommendation algorithm
- No tests for season watch status calculation
- No tests for duplicate detection

### Repository Layer
SeriesRepository already contains `findByHasNewSeasons`, `findBySeriesStatus`, and `findAllWithLink()`. Remaining gap:
- `findByPriorityGreaterThan(int)` on MovieRepository for priority-based filtering.

```
