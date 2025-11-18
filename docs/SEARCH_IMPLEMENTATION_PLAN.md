# Search Feature Implementation Plan

## Phase 1: Core Search Functionality (Current Scope)

### Step 1: Add Configuration Constants
**File**: `frontend/src/components/AddMovieModal.jsx`

Add configuration object at the top of the component file:
```javascript
const SEARCH_CONFIG = {
  minCharsForAutoSearch: 3,
  autoSearchDelayMs: 150,
  blurHideDelayMs: 50,
  maxResults: 5,
  imageBaseUrl: 'https://image.tmdb.org/t/p/w92'
};
```

### Step 2: Add State Management
**File**: `frontend/src/components/AddMovieModal.jsx`

Add new state variables:
```javascript
const [searchResults, setSearchResults] = useState([]);
const [isSearching, setIsSearching] = useState(false);
const [showSearchPopup, setShowSearchPopup] = useState(false);
const [searchTimerRef] = useState({ current: null });
const [abortControllerRef] = useState({ current: null });
const [blurTimerRef] = useState({ current: null });
```

### Step 3: Implement Search Function
**File**: `frontend/src/components/AddMovieModal.jsx`

Create `performSearch` function:
```javascript
const performSearch = async (query, isManual = false) => {
  // Cancel any existing search
  if (abortControllerRef.current) {
    abortControllerRef.current.abort();
  }
  
  // Clear auto-search timer if manual search
  if (isManual && searchTimerRef.current) {
    clearTimeout(searchTimerRef.current);
    searchTimerRef.current = null;
  }
  
  // Validate query
  if (!query || query.trim().length === 0) {
    setSearchResults([]);
    setShowSearchPopup(false);
    return;
  }
  
  // Check minimum characters for auto-search
  if (!isManual && query.length < SEARCH_CONFIG.minCharsForAutoSearch) {
    return;
  }
  
  // Create new abort controller
  const controller = new AbortController();
  abortControllerRef.current = controller;
  
  setIsSearching(true);
  setShowSearchPopup(false);
  
  try {
    const endpoint = formData.contentType === 'MOVIE' 
      ? '/api/tmdb/search/movies'
      : '/api/tmdb/search/series';
    
    const response = await axios.get(endpoint, {
      params: { title: query.trim() },
      signal: controller.signal
    });
    
    const results = response.data.slice(0, SEARCH_CONFIG.maxResults);
    setSearchResults(results);
    setShowSearchPopup(results.length > 0);
  } catch (error) {
    if (error.name === 'CanceledError') {
      console.log('Search cancelled');
    } else {
      console.error('Search error:', error);
      setSearchResults([]);
    }
  } finally {
    setIsSearching(false);
    abortControllerRef.current = null;
  }
};
```

### Step 4: Update handleChange for Title Field
**File**: `frontend/src/components/AddMovieModal.jsx`

Modify the title input handler to trigger auto-search:
```javascript
// In handleChange function, add this case for title field
if (name === 'title') {
  // Clear existing timer
  if (searchTimerRef.current) {
    clearTimeout(searchTimerRef.current);
  }
  
  // Set new timer for auto-search
  if (value.length >= SEARCH_CONFIG.minCharsForAutoSearch) {
    searchTimerRef.current = setTimeout(() => {
      performSearch(value, false);
    }, SEARCH_CONFIG.autoSearchDelayMs);
  } else {
    // Clear results if below minimum
    setSearchResults([]);
    setShowSearchPopup(false);
  }
}
```

### Step 5: Add Manual Search Handler
**File**: `frontend/src/components/AddMovieModal.jsx`

Create handler for search button:
```javascript
const handleManualSearch = (e) => {
  e.preventDefault();
  e.stopPropagation();
  performSearch(formData.title, true);
};
```

### Step 6: Add Focus Management
**File**: `frontend/src/components/AddMovieModal.jsx`

Create handlers for blur and focus:
```javascript
const handleTitleBlur = () => {
  // Delay hiding to allow clicking on results
  blurTimerRef.current = setTimeout(() => {
    setShowSearchPopup(false);
  }, SEARCH_CONFIG.blurHideDelayMs);
};

const handleTitleFocus = () => {
  // Clear blur timer if focus returns
  if (blurTimerRef.current) {
    clearTimeout(blurTimerRef.current);
    blurTimerRef.current = null;
  }
  
  // Show popup if we have results
  if (searchResults.length > 0) {
    setShowSearchPopup(true);
  }
};
```

### Step 7: Add Result Item Rendering
**File**: `frontend/src/components/AddMovieModal.jsx`

Create component for rendering search results:
```javascript
const renderSearchResults = () => {
  if (!showSearchPopup) return null;
  
  if (isSearching) {
    return (
      <div className="search-results-popup">
        <div className="search-loading">Searching...</div>
      </div>
    );
  }
  
  if (searchResults.length === 0) {
    return (
      <div className="search-results-popup">
        <div className="search-no-results">No results found</div>
      </div>
    );
  }
  
  return (
    <div className="search-results-popup">
      {searchResults.map((result) => {
        const title = result.title || result.name || 'Unknown Title';
        const year = result.releaseDate 
          ? new Date(result.releaseDate).getFullYear()
          : result.firstAirDate 
            ? new Date(result.firstAirDate).getFullYear()
            : '';
        const posterUrl = result.posterPath
          ? `${SEARCH_CONFIG.imageBaseUrl}${result.posterPath}`
          : null;
        
        return (
          <div
            key={result.id}
            className="search-result-item"
            onMouseDown={(e) => e.preventDefault()} // Prevent blur on click
            onClick={() => handleResultClick(result)}
          >
            {posterUrl ? (
              <img
                src={posterUrl}
                alt={title}
                className="search-result-poster"
              />
            ) : (
              <div className="search-result-poster-placeholder">
                🎬
              </div>
            )}
            <div className="search-result-info">
              <div className="search-result-title">{title}</div>
              {year && <div className="search-result-year">{year}</div>}
            </div>
          </div>
        );
      })}
    </div>
  );
};

const handleResultClick = async (result) => {
  // Close popup immediately
  setShowSearchPopup(false);
  
  // Populate title with original_title/original_name
  const originalTitle = result.original_title || result.original_name || result.title || result.name;
  
  // Build TMDB URL for description
  const tmdbUrl = formData.contentType === 'MOVIE'
    ? `https://www.themoviedb.org/movie/${result.id}`
    : `https://www.themoviedb.org/tv/${result.id}`;
  
  // Get genres (joining genre names with commas)
  const genres = result.genres ? result.genres.join(', ') : '';
  
  // Update form data
  setFormData(prev => ({
    ...prev,
    title: originalTitle,
    link: tmdbUrl,
    genres: genres,
    // For series, add number of seasons if available
    ...(formData.contentType === 'SERIES' && result.numberOfSeasons && {
      numberOfSeasons: result.numberOfSeasons
    })
  }));
  
  // If poster available, download and upload via image API
  if (result.poster_path) {
    try {
      setIsLoadingImage(true);
      const fullPosterUrl = result.poster_path; // Backend returns full URL
      
      const response = await axios.post(`${API_BASE_URL}/images/download`, {
        imageUrl: fullPosterUrl
      });
      
      if (response.data) {
        setImageId(response.data.id);
        setImagePreview(`${API_BASE_URL}/images/${response.data.id}`);
        setFormData(prev => ({
          ...prev,
          coverImage: `/api/images/${response.data.id}`
        }));
      }
    } catch (error) {
      console.error('Error downloading poster:', error);
      setErrors(prev => ({
        ...prev,
        coverImage: 'Failed to download poster image'
      }));
    } finally {
      setIsLoadingImage(false);
    }
  }
};
```

### Step 8: Update Title Input JSX
**File**: `frontend/src/components/AddMovieModal.jsx`

Replace the title input section with new search-enabled version:
```jsx
<div className="form-group">
  <label htmlFor="title" className="form-label">
    Title <span className="required">*</span>
  </label>
  <div className="search-container">
    <div className="title-input-wrapper">
      <input
        type="text"
        id="title"
        name="title"
        className={`form-input ${errors.title ? 'error' : ''}`}
        value={formData.title}
        onChange={handleChange}
        onBlur={handleTitleBlur}
        onFocus={handleTitleFocus}
        placeholder={`Search for ${formData.contentType === 'MOVIE' ? 'movie' : 'series'} title`}
        autoFocus
        style={{ flex: 1 }}
      />
      <button
        type="button"
        className="search-button-inline"
        onClick={handleManualSearch}
        disabled={!formData.title || formData.title.trim().length === 0}
      >
        Search
      </button>
    </div>
    {errors.title && <span className="error-message">{errors.title}</span>}
    {renderSearchResults()}
  </div>
</div>
```

### Step 9: Update Cleanup Function
**File**: `frontend/src/components/AddMovieModal.jsx`

Update `handleClose` to clear search state:
```javascript
const handleClose = () => {
  // Clear timers
  if (searchTimerRef.current) {
    clearTimeout(searchTimerRef.current);
    searchTimerRef.current = null;
  }
  if (blurTimerRef.current) {
    clearTimeout(blurTimerRef.current);
    blurTimerRef.current = null;
  }
  
  // Cancel any ongoing search
  if (abortControllerRef.current) {
    abortControllerRef.current.abort();
    abortControllerRef.current = null;
  }
  
  // Clear search state
  setSearchResults([]);
  setIsSearching(false);
  setShowSearchPopup(false);
  
  // ... existing cleanup code
};
```

### Step 10: Add CSS Styles
**File**: `frontend/src/styles.css`

Add new CSS classes at the end of the file:
```css
/* Search Feature Styles */
.search-container {
  position: relative;
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
  font-size: 13px;
  font-weight: 600;
}

.search-button-inline:enabled {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.search-button-inline:enabled:hover {
  background: #5568d3;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(102, 126, 234, 0.3);
}

.search-button-inline:disabled {
  cursor: not-allowed;
  opacity: 0.5;
  background: #f5f5f5;
  color: #999;
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

### Step 11: Handle Content Type Changes
**File**: `frontend/src/components/AddMovieModal.jsx`

Update content type change handler:
```javascript
// Add to handleChange for contentType
if (name === 'contentType') {
  // Clear search results when switching types
  setSearchResults([]);
  setShowSearchPopup(false);
  
  // Cancel any ongoing search
  if (abortControllerRef.current) {
    abortControllerRef.current.abort();
  }
  if (searchTimerRef.current) {
    clearTimeout(searchTimerRef.current);
    searchTimerRef.current = null;
  }
  
  // Trigger new search if we have a query
  if (formData.title && formData.title.length >= SEARCH_CONFIG.minCharsForAutoSearch) {
    setTimeout(() => {
      performSearch(formData.title, false);
    }, 100);
  }
}
```

## Phase 2: Enrichment Integration (IMPLEMENTED)

### Requirements
When a search result is clicked:
1. ✅ **Close the search popup immediately**
2. ✅ **Populate title** with `original_title` (movies) or `original_name` (series)
3. ✅ **Populate coverImage**: Download poster via `/api/images/download` endpoint
4. ✅ **Populate link**: Build TMDB URL (`https://www.themoviedb.org/movie/{id}` or `https://www.themoviedb.org/tv/{id}`)
5. ✅ **Populate genres**: Join genre array with commas
6. ✅ **For series**: Populate `numberOfSeasons` field if available

### Implementation Details

The `handleResultClick` function:
- Closes popup immediately
- Extracts `original_title`/`original_name` for title field
- Builds TMDB URL based on content type and result ID
- Extracts and joins genres from search result
- For series, populates number of seasons if present
- Downloads poster image using backend `/api/images/download` endpoint
- Updates form with image ID and preview
- Shows loading indicator during image download
- Handles errors gracefully with user-friendly messages

### Error Handling
- Image download failures show error message
- Failed downloads don't block form population
- User can manually enter/correct any field
- Loading state prevents duplicate submissions

## Testing Strategy

### Unit Tests
- Search debouncing logic
- Request cancellation
- Focus/blur behavior
- Result rendering

### Integration Tests
- Full search flow (type → search → results)
- Content type switching
- Manual vs auto-search
- Network error handling

### E2E Tests (with Playwright)
1. Open AddMovieModal
2. Type "Inception" in title field
3. Verify auto-search after 3 chars + delay
4. Verify results popup appears
5. Verify poster images load
6. Verify manual search button behavior
7. Switch content type
8. Verify search updates

## Rollout Plan

1. **Development**: Implement Phase 1 on feature branch
2. **Testing**: Manual testing + E2E tests
3. **Code Review**: Review with team
4. **Staging**: Deploy to staging environment
5. **Production**: Deploy with feature flag (optional)
6. **Monitor**: Check for errors, user feedback
7. **Phase 2**: Plan enrichment integration

## Configuration Management

Consider moving SEARCH_CONFIG to a separate config file:

**File**: `frontend/src/config/searchConfig.js`
```javascript
export const SEARCH_CONFIG = {
  minCharsForAutoSearch: 3,
  autoSearchDelayMs: 150,
  blurHideDelayMs: 50,
  maxResults: 5,
  imageBaseUrl: 'https://image.tmdb.org/t/p/w92'
};
```

This allows easy adjustment without modifying component code.

## Performance Considerations

1. **Request cancellation**: Prevents race conditions and unnecessary API calls
2. **Debouncing**: Reduces API calls during typing
3. **Result limiting**: Only fetches/displays top 5 results
4. **Lazy image loading**: Consider adding for poster images
5. **Caching**: Future enhancement to cache recent searches

## Accessibility

1. **Keyboard navigation**: Add in Phase 2
2. **ARIA labels**: Add to search button and results
3. **Screen reader support**: Announce search results
4. **Focus management**: Ensure logical tab order

## Browser Compatibility

- Modern browsers (Chrome, Firefox, Safari, Edge)
- AbortController supported in all modern browsers
- CSS Grid/Flexbox for layout
- No polyfills required for target browsers
