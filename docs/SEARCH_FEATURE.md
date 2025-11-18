# Movie/Series Search Feature in AddMovieModal

## Overview
This feature adds integrated TMDB search functionality directly within the movie title input field in the AddMovieModal component. Users can search for movies/series as they type and select from search results to auto-populate metadata.

## User Experience Flow

### Search Triggering
1. **Auto-search**: After typing 3+ characters (configurable), search executes after 150ms delay (configurable)
2. **Manual search**: "Search" button becomes enabled when at least 1 character is entered
3. **Search cancellation**: If a new search is triggered (auto or manual), any in-progress request is cancelled

### Search Results Display
- Dropdown popup appears below the title input field
- Shows top 5 results (configurable)
- Each result displays:
  - Small poster image (thumbnail)
  - Full title
  - Release/air year
- Currently no click action (placeholder for future enrichment logic)

### UI Behavior
- Popup appears when search results are available
- Popup disappears 50ms after input field loses focus
- Search button disabled by default, enabled when input has text
- Loading indicator shown during search

## Configuration

New configuration object in `AddMovieModal.jsx`:

```javascript
const SEARCH_CONFIG = {
  minCharsForAutoSearch: 3,      // Minimum characters to trigger auto-search
  autoSearchDelayMs: 150,        // Delay after typing stops before search
  blurHideDelayMs: 50,           // Delay before hiding popup on blur
  maxResults: 5,                 // Maximum search results to display
  imageBaseUrl: 'https://image.tmdb.org/t/p/w92' // TMDB thumbnail size
};
```

## API Integration

### Endpoints Used
- **Movies**: `GET /api/tmdb/search/movies?title={query}`
- **Series**: `GET /api/tmdb/search/series?title={query}`

### Response Format
```json
[
  {
    "id": 27205,
    "title": "Inception",
    "name": null,
    "posterPath": "/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg",
    "releaseDate": "2010-07-16",
    "firstAirDate": null,
    "overview": "...",
    "mediaType": "movie"
  }
]
```

## Component State Management

New state variables:
- `searchQuery`: Current search input value (synced with `formData.title`)
- `searchResults`: Array of TMDB search results
- `isSearching`: Loading state for search requests
- `showSearchPopup`: Boolean to control popup visibility
- `searchAbortController`: AbortController for cancelling requests

## Implementation Details

### Search Debouncing
Uses `setTimeout` to debounce auto-search:
1. Clear existing timer on every keystroke
2. Start new timer with configured delay
3. Execute search when timer completes
4. Check minimum character requirement

### Request Cancellation
Uses `AbortController`:
1. Create new controller before each search
2. Cancel previous controller if exists
3. Pass signal to axios request
4. Handle abort errors gracefully

### Focus Management
Uses `onBlur` with timeout:
1. When input loses focus, start 50ms timer
2. Timer allows clicking on search results before popup closes
3. Clear timer if focus returns to input
4. Hide popup when timer completes

### Content Type Switching
- Search endpoint changes based on `contentType` (MOVIE/SERIES)
- Clears search results when content type changes
- Reset search state

## CSS Styling

New CSS classes needed:

```css
.search-container {
  position: relative; /* For absolute positioning of popup */
}

.title-input-wrapper {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}

.search-button-inline {
  padding: 6px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 5px;
  background: white;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.search-button-inline:enabled {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.search-button-inline:enabled:hover {
  background: #5568d3;
}

.search-button-inline:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.search-results-popup {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 5px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  max-height: 400px;
  overflow-y: auto;
  z-index: 1000;
  margin-top: 4px;
}

.search-result-item {
  display: flex;
  gap: 12px;
  padding: 10px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;
}

.search-result-item:hover {
  background: #f8f8f8;
}

.search-result-item:last-child {
  border-bottom: none;
}

.search-result-poster {
  width: 46px;
  height: 69px;
  object-fit: cover;
  border-radius: 3px;
  flex-shrink: 0;
  background: #e0e0e0;
}

.search-result-poster-placeholder {
  width: 46px;
  height: 69px;
  background: #e0e0e0;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 20px;
  flex-shrink: 0;
}

.search-result-info {
  flex: 1;
  min-width: 0;
}

.search-result-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.search-result-year {
  font-size: 13px;
  color: #666;
}

.search-loading {
  padding: 12px;
  text-align: center;
  color: #666;
  font-size: 14px;
}

.search-no-results {
  padding: 12px;
  text-align: center;
  color: #999;
  font-size: 14px;
}
```

## Future Enhancements (Not Implemented Yet)

### On Result Click
When a search result is clicked:
1. Call `/api/tmdb/enrich/movie` or `/api/tmdb/enrich/series` with `tmdbId`
2. Auto-populate form fields:
   - Title (confirmed)
   - Genres (comma-separated string)
   - Cover image (download and set imageId)
   - Length (for movies)
   - Number of seasons (for series)
   - Series status (COMPLETE/ONGOING based on TMDB status)
3. Close search popup
4. Show success message/indicator

### Additional Features
- Show TMDB rating/popularity in search results
- Display overview text on hover
- Keyboard navigation (arrow keys to navigate results, Enter to select)
- "View more results" button if more than 5 matches
- Cache recent searches to reduce API calls
- Error handling with retry mechanism

## Testing Checklist

### Manual Testing
- [ ] Auto-search triggers after 3 characters with 150ms delay
- [ ] Search button disabled when input is empty
- [ ] Search button enabled when input has text
- [ ] Manual search cancels auto-search timer
- [ ] Manual search cancels in-progress requests
- [ ] Popup appears with results
- [ ] Popup disappears 50ms after blur
- [ ] Popup stays visible when clicking inside it
- [ ] Results display poster, title, and year correctly
- [ ] No poster shows placeholder icon
- [ ] Content type switch changes search endpoint
- [ ] Loading indicator shows during search
- [ ] "No results" message displays correctly
- [ ] Error handling works gracefully

### Integration Testing
- [ ] Search works for movies
- [ ] Search works for series
- [ ] TMDB API returns expected format
- [ ] Image URLs construct correctly
- [ ] Year extraction works for both releaseDate and firstAirDate

## Known Limitations

1. **Click action not implemented**: Clicking a result does nothing (intentional for phase 1)
2. **No keyboard navigation**: Results can only be clicked with mouse
3. **No pagination**: Only shows first 5 results from TMDB
4. **No caching**: Each search makes a new API request
5. **TMDB API key required**: Feature won't work without configured TMDB access token

## Dependencies

- **Backend**: Existing TMDB integration (TmdbController, TmdbEnrichmentService)
- **Frontend**: axios for HTTP requests
- **TMDB**: Requires valid API access token in backend configuration

## Configuration Required

Backend `application.properties`:
```properties
tmdb.access-token=${TMDB_ACCESS_TOKEN}
tmdb.api.base-url=https://api.themoviedb.org/3
tmdb.image.base-url=https://image.tmdb.org/t/p/w92
```

Environment variable:
```bash
export TMDB_ACCESS_TOKEN=your_bearer_token_here
```
