# Series Management Guide

## Quick Reference

### Core Concepts
- **Series**: TV shows tracked with season-level granularity
- **Season**: Individual season with a number and watch status (WATCHED/UNWATCHED)
- **Series Watch Status**: Auto-calculated - WATCHED if all seasons watched, otherwise UNWATCHED
- **Seasons can be**: Added during creation, added progressively, or fetched from external sources
- **Season Invariant**: Backend always persists at least one season; if the client omits the array, season 1 (UNWATCHED) is inserted automatically

---

## API Endpoints Summary

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/series` | Create new series (can omit seasons; backend adds default season 1) |
| GET | `/api/series` | List all series |
| GET | `/api/series/{id}` | Get single series details |
| PUT | `/api/series/{id}` | Update series (optionally replace seasons) |
| DELETE | `/api/series/{id}` | Delete series |
| PATCH | `/api/series/{id}/seasons/{seasonNumber}/watch-status` | Mark individual season (creates if not exists) |
| PATCH | `/api/series/{id}/watch-status` | Mark all seasons in series |
| PATCH | `/api/series/{id}/priority` | Update recommendation priority |
| POST | `/api/series/{id}/refresh` | Manually refresh seasons from external source |

---

## Common Use Cases

### 1. Create Series with Known Seasons

**When to use**: You know all seasons upfront

```bash
curl -X POST http://localhost:8080/api/series \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Breaking Bad",
    "link": "https://www.imdb.com/title/tt0903747",
    "genres": ["Crime", "Drama"],
    "seasons": [
      {"seasonNumber": 1, "watchStatus": "UNWATCHED"},
      {"seasonNumber": 2, "watchStatus": "UNWATCHED"},
      {"seasonNumber": 3, "watchStatus": "UNWATCHED"},
      {"seasonNumber": 4, "watchStatus": "UNWATCHED"},
      {"seasonNumber": 5, "watchStatus": "UNWATCHED"}
    ],
    "priority": 5
  }'
```

### 2. Create Series Without Explicit Seasons (Auto Default)

**When to use**: You don't care about enumerating seasons up front

```bash
# Step 1: Create series without a seasons array (backend adds season 1 / UNWATCHED)
curl -X POST http://localhost:8080/api/series \
  -H "Content-Type: application/json" \
  -d '{
    "title": "The Sopranos",
    "genres": ["Crime", "Drama"]
  }'

# Step 2: Update the auto-created season or add additional ones as you watch
curl -X PATCH http://localhost:8080/api/series/{id}/seasons/1/watch-status \
  -H "Content-Type: application/json" \
  -d '{"watchStatus": "UNWATCHED"}'
```

**Note**: The response from step 1 already includes `seasons: [{"seasonNumber": 1, "watchStatus": "UNWATCHED"}]`. Additional PATCH calls can introduce season 2, 3, etc., whenever you are ready.

### 3. Mark Season as Watched (Progressive Tracking)

**When to use**: Track progress as you watch

```bash
# Finished season 1
curl -X PATCH http://localhost:8080/api/series/{id}/seasons/1/watch-status \
  -H "Content-Type: application/json" \
  -d '{"watchStatus": "WATCHED"}'

# Starting season 2 (auto-creates if doesn't exist)
curl -X PATCH http://localhost:8080/api/series/{id}/seasons/2/watch-status \
  -H "Content-Type: application/json" \
  -d '{"watchStatus": "UNWATCHED"}'

# Finished season 2
curl -X PATCH http://localhost:8080/api/series/{id}/seasons/2/watch-status \
  -H "Content-Type: application/json" \
  -d '{"watchStatus": "WATCHED"}'
```

### 4. Mark Entire Series as Watched

**When to use**: Finished binge-watching all seasons

```bash
curl -X PATCH http://localhost:8080/api/series/{id}/watch-status \
  -H "Content-Type: application/json" \
  -d '{"watchStatus": "WATCHED"}'
```

**Result**: All existing seasons marked as WATCHED, watchStatus becomes WATCHED

### 5. Reset Series for Rewatching

**When to use**: Want to rewatch the entire series

```bash
curl -X PATCH http://localhost:8080/api/series/{id}/watch-status \
  -H "Content-Type: application/json" \
  -d '{"watchStatus": "UNWATCHED"}'
```

**Result**: All seasons marked as UNWATCHED, watchStatus becomes UNWATCHED

### 6. Update Series Metadata (Keep Seasons Unchanged)

**When to use**: Fix title, add comment, change priority - don't touch seasons

```bash
curl -X PUT http://localhost:8080/api/series/{id} \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Breaking Bad (2008-2013)",
    "comment": "One of the best series ever made",
    "genres": ["Crime", "Drama", "Thriller"],
    "priority": 10
  }'
```

**Note**: Omit `seasons` field to preserve existing seasons

### 7. Replace All Seasons at Once

**When to use**: Restructure season list, import from external source

```bash
curl -X PUT http://localhost:8080/api/series/{id} \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Game of Thrones",
    "seasons": [
      {"seasonNumber": 1, "watchStatus": "WATCHED"},
      {"seasonNumber": 2, "watchStatus": "WATCHED"},
      {"seasonNumber": 3, "watchStatus": "WATCHED"},
      {"seasonNumber": 4, "watchStatus": "UNWATCHED"},
      {"seasonNumber": 5, "watchStatus": "UNWATCHED"},
      {"seasonNumber": 6, "watchStatus": "UNWATCHED"},
      {"seasonNumber": 7, "watchStatus": "UNWATCHED"},
      {"seasonNumber": 8, "watchStatus": "UNWATCHED"}
    ]
  }'
```

**Warning**: This REPLACES all existing seasons

### 8. Skip Seasons (Non-Sequential Watching)

**When to use**: Jumping to latest season without watching previous ones

```bash
# Mark season 5 as watched (seasons 1-4 remain unwatched or don't exist)
curl -X PATCH http://localhost:8080/api/series/{id}/seasons/5/watch-status \
  -H "Content-Type: application/json" \
  -d '{"watchStatus": "WATCHED"}'
```

**Result**: Season 5 is WATCHED, series status remains UNWATCHED (not all seasons watched)

### 9. Boost Series Priority for Recommendations

**When to use**: Want this series to appear more often in recommendations

```bash
curl -X PATCH http://localhost:8080/api/series/{id}/priority \
  -H "Content-Type: application/json" \
  -d '{"priority": 15}'
```

**Effect**: Higher priority = higher probability in recommendations (default is 0)

### 10. Manually Refresh Seasons from External Source

**When to use**: Check if new seasons released

```bash
curl -X POST http://localhost:8080/api/series/{id}/refresh
```

**Requirements**: Series must have `link` field set to IMDB/Kinopoisk URL

**Updates**: `totalAvailableSeasons`, `seriesStatus`, `hasNewSeasons`, `lastSeasonCheck`

---

## Data Structures

### SeriesRequest (POST/PUT)
```json
{
  "title": "string (required)",
  "link": "string (optional)",
  "comment": "string (optional)",
  "coverImage": "string (optional)",
  "genres": ["string"] (optional),
  "seasons": [
    {
      "seasonNumber": "integer (required)",
      "watchStatus": "WATCHED|UNWATCHED (required)"
    }
  ] (optional, but if omitted or empty the backend auto-adds season 1 with UNWATCHED status),
  "addedBy": "string (optional)",
  "priority": "integer (optional, default: 0)"
}
```

### WatchStatusRequest (PATCH season or series)
```json
{
  "watchStatus": "WATCHED|UNWATCHED"
}
```

### PriorityRequest (PATCH priority)
```json
{
  "priority": "integer"
}
```

### SeriesResponse (All responses)
```json
{
  "id": "string",
  "title": "string",
  "link": "string",
  "comment": "string",
  "coverImage": "string",
  "genres": ["string"],
  "seasons": [
    {
      "seasonNumber": "integer",
      "watchStatus": "WATCHED|UNWATCHED"
    }
  ],
  "watchStatus": "WATCHED|UNWATCHED (auto-calculated)",
  "totalAvailableSeasons": "integer (from external source)",
  "hasNewSeasons": "boolean (auto-calculated)",
  "seriesStatus": "COMPLETE|ONGOING (from external source)",
  "addedBy": "string",
  "dateAdded": "ISO datetime (auto-generated)",
  "lastSeasonCheck": "ISO datetime (auto-updated)",
  "priority": "integer"
}
```

---

## Business Rules

### Season Management
1. **Creation**: Seasons can be provided during series creation or omitted; if the payload has no seasons (or an empty array) the backend inserts season 1 with UNWATCHED status so the series is never empty
2. **Auto-Creation**: PATCH individual season creates it if it doesn't exist
3. **No Order Enforcement**: Can mark any season without marking previous ones
4. **Bulk Operations**: PATCH series watch-status affects ALL existing seasons
5. **Replacement**: PUT with seasons array replaces all existing seasons

### Watch Status Calculation
1. **Default Season**: Because at least one season always exists, omitting seasons simply results in a single UNWATCHED season 1, keeping `watchStatus = UNWATCHED` until you update it
2. **All Watched**: All seasons WATCHED → watchStatus = WATCHED
3. **Any Unwatched**: Any season UNWATCHED → watchStatus = UNWATCHED
4. **Auto-Recalculation**: Happens after every season change

### Recommendations
1. **Inclusion Criteria**: Only series with unwatched seasons (watchStatus = UNWATCHED)
2. **High Priority**: Series with hasNewSeasons=true get 10x weight
3. **Manual Priority**: Higher priority values increase recommendation probability
4. **Age-Based**: Older unwatched series get higher weight

### External Integration
1. **Link Required**: Must set `link` field for external refresh to work
2. **Automatic Refresh**: Runs weekly on Mondays at midnight
3. **Manual Refresh**: Can be triggered anytime via POST /{id}/refresh
4. **Updates**: External refresh updates totalAvailableSeasons, seriesStatus, hasNewSeasons

---

## Workflow Examples

### Workflow 1: Discover and Track New Series
```bash
# 1. Add series with IMDB link
POST /api/series
{
  "title": "The Last of Us",
  "link": "https://www.imdb.com/title/tt3581920"
}

# 2. Manually refresh to get season info
POST /api/series/{id}/refresh

# 3. Check response - seasons auto-populated
GET /api/series/{id}

# 4. Start watching season 1
PATCH /api/series/{id}/seasons/1/watch-status
{"watchStatus": "UNWATCHED"}

# 5. Finish season 1
PATCH /api/series/{id}/seasons/1/watch-status
{"watchStatus": "WATCHED"}
```

### Workflow 2: Binge-Watch Completed Series
```bash
# 1. Add series with all known seasons
POST /api/series
{
  "title": "The Wire",
  "seasons": [
    {"seasonNumber": 1, "watchStatus": "UNWATCHED"},
    {"seasonNumber": 2, "watchStatus": "UNWATCHED"},
    {"seasonNumber": 3, "watchStatus": "UNWATCHED"},
    {"seasonNumber": 4, "watchStatus": "UNWATCHED"},
    {"seasonNumber": 5, "watchStatus": "UNWATCHED"}
  ]
}

# 2. Track progress by marking each season
PATCH /api/series/{id}/seasons/1/watch-status {"watchStatus": "WATCHED"}
PATCH /api/series/{id}/seasons/2/watch-status {"watchStatus": "WATCHED"}
# ... continue for each season

# 3. Or mark all at once when finished
PATCH /api/series/{id}/watch-status {"watchStatus": "WATCHED"}
```

### Workflow 3: Update Series with New Season
```bash
# 1. Get current series state
GET /api/series/{id}

# 2. Add new season (system detected or manual)
PATCH /api/series/{id}/seasons/6/watch-status
{"watchStatus": "UNWATCHED"}

# 3. Series automatically appears in recommendations (hasNewSeasons logic)

# 4. After watching
PATCH /api/series/{id}/seasons/6/watch-status
{"watchStatus": "WATCHED"}
```

---

## Troubleshooting

### Problem: Seasons not saving when creating series

**Symptoms**: POST request succeeds but seasons array is empty in response

**Solutions**:
1. Check `seasons` field is included in request body
2. Verify JSON structure: `[{"seasonNumber": 1, "watchStatus": "UNWATCHED"}]`
3. Ensure watchStatus is "WATCHED" or "UNWATCHED" (case-sensitive)
4. Check backend logs for validation errors

### Problem: Series watch status not updating

**Symptoms**: Marking seasons doesn't change watchStatus

**Solutions**:
1. Verify ALL seasons are marked WATCHED (check response)
2. Ensure `updateSeriesWatchStatus()` is called in service layer
3. Check MongoDB document has seasons array properly saved
4. Restart application if watch status stuck

### Problem: Cannot add seasons to existing series

**Symptoms**: PATCH season endpoint returns error

**Solutions**:
1. Verify series ID is correct (GET /api/series/{id} first)
2. Check endpoint path: `/seasons/{seasonNumber}/watch-status`
3. Ensure request body has watchStatus field
4. Verify season number is integer, not string

### Problem: PUT request removes all seasons

**Symptoms**: After PUT, seasons array is empty

**Solutions**:
1. Don't include `"seasons": null` in PUT request
2. Omit seasons field entirely to preserve existing seasons
3. If replacing seasons, include complete seasons array
4. Use PATCH endpoints for season updates instead

### Problem: Series not appearing in recommendations

**Symptoms**: Series exists but never recommended

**Solutions**:
1. Check watchStatus is UNWATCHED (at least one unwatched season)
2. Verify seasons array is not empty
3. Increase priority value for higher probability
4. Check if all seasons are marked WATCHED
5. Verify recommendation algorithm includes series

---

## Best Practices

### When to Use Each Endpoint
- **POST**: Initial series creation
- **PUT**: Major updates (title, genres, metadata), bulk season replacement
- **PATCH season**: Individual season tracking (preferred for season updates)
- **PATCH series**: Bulk mark all seasons (watched/unwatched)
- **PATCH priority**: Adjust recommendation weight

### Recommended Patterns
1. **New Series**: POST with seasons array if known, or POST empty + PATCH seasons progressively
2. **Season Tracking**: PATCH individual seasons as you watch (most granular control)
3. **Binge Complete**: PATCH series watch-status to mark all at once
4. **Metadata Updates**: PUT without seasons field (preserves existing seasons)
5. **Restructure**: PUT with complete seasons array (replaces all)

### Performance Tips
1. Use PUT sparingly - PATCH is more efficient for season updates
2. Batch season updates in UI, send individual PATCH requests
3. Cache series responses in frontend to minimize GET requests
4. Use GET /api/catalog for combined movie+series lists

### Data Integrity
1. Always include link for external integration
2. Use consistent genre naming across series
3. Set priority for actively watching series
4. Trigger manual refresh periodically for ongoing series
5. Verify seasons array after creation (GET request)

---

## Integration with Frontend

### Display Logic
```javascript
// Check if series has unwatched content
const hasUnwatchedContent = series.watchStatus === 'UNWATCHED';

// Calculate watch progress
const watchedSeasons = series.seasons.filter(s => s.watchStatus === 'WATCHED').length;
const totalSeasons = series.seasons.length;
const progress = totalSeasons > 0 ? (watchedSeasons / totalSeasons) * 100 : 0;

// Show new season badge
const showNewSeasonBadge = series.hasNewSeasons === true;

// Next season to watch
const nextSeason = series.seasons.find(s => s.watchStatus === 'UNWATCHED');
```

### Common UI Actions
```javascript
// Mark season as watched
async function markSeasonWatched(seriesId, seasonNumber) {
  await fetch(`/api/series/${seriesId}/seasons/${seasonNumber}/watch-status`, {
    method: 'PATCH',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({watchStatus: 'WATCHED'})
  });
}

// Mark all as watched
async function markSeriesWatched(seriesId) {
  await fetch(`/api/series/${seriesId}/watch-status`, {
    method: 'PATCH',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({watchStatus: 'WATCHED'})
  });
}

// Add new season
async function addSeason(seriesId, seasonNumber) {
  await fetch(`/api/series/${seriesId}/seasons/${seasonNumber}/watch-status`, {
    method: 'PATCH',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({watchStatus: 'UNWATCHED'})
  });
}
```

---

## Testing

### Manual Testing Checklist
- [ ] Create series with seasons
- [ ] Create series without providing seasons (verify default season 1 is inserted)
- [ ] Add season to existing series
- [ ] Mark season as watched
- [ ] Mark season as unwatched
- [ ] Mark entire series as watched
- [ ] Mark entire series as unwatched
- [ ] Update series metadata (preserve seasons)
- [ ] Replace all seasons via PUT
- [ ] Update priority
- [ ] Delete series
- [ ] Verify watchStatus calculation
- [ ] Test skip seasons (non-sequential)
- [ ] Test manual refresh

### Example Test Data
```json
{
  "title": "Test Series",
  "seasons": [
    {"seasonNumber": 1, "watchStatus": "WATCHED"},
    {"seasonNumber": 2, "watchStatus": "WATCHED"},
    {"seasonNumber": 3, "watchStatus": "UNWATCHED"}
  ]
}
// Expected: watchStatus = UNWATCHED

{
  "title": "Test Series Fully Watched",
  "seasons": [
    {"seasonNumber": 1, "watchStatus": "WATCHED"},
    {"seasonNumber": 2, "watchStatus": "WATCHED"}
  ]
}
// Expected: watchStatus = WATCHED
```
