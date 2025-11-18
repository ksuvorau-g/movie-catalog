# TMDB Integration Guide

## Overview
The Movie Catalog app now integrates with [The Movie Database (TMDB)](https://www.themoviedb.org/) API to automatically fetch metadata for movies and TV series.

## Features
- **Search** movies and series by title
- **Auto-fetch** metadata: genres, length/runtime, season count, cover images
- **Download** and save cover images locally
- **Store** TMDB ID for future reference

## Setup

### 1. Get TMDB Access Token
1. Create a free account at https://www.themoviedb.org/
2. Go to Settings → API (https://www.themoviedb.org/settings/api)
3. Copy your **API Read Access Token** (this is the Bearer token)
   - Note: This is different from the API Key (v3 auth)
   - The Access Token works with both v3 and v4 APIs

### 2. Configure Access Token

**Option A: Environment Variable (Recommended)**
```bash
export TMDB_ACCESS_TOKEN=your_actual_access_token_here
```

**Option B: application.properties**
Edit `src/main/resources/application.properties`:
```properties
tmdb.access-token=your_actual_access_token_here
```

**Docker Compose:**
Add to `docker-compose.yml` under `app` service:
```yaml
environment:
  - TMDB_ACCESS_TOKEN=${TMDB_ACCESS_TOKEN}
```

Then run:
```bash
TMDB_ACCESS_TOKEN=your_token make run
```

## API Endpoints

### Search for Movies
```bash
GET /api/tmdb/search/movies?title=Inception
```

**Response:**
```json
[
  {
    "id": 27205,
    "title": "Inception",
    "overview": "Cobb steals secrets from dreams...",
    "posterPath": "/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg",
    "releaseDate": "2010-07-16"
  }
]
```

### Search for TV Series
```bash
GET /api/tmdb/search/series?title=Breaking Bad
```

### Enrich Movie Metadata
```bash
POST /api/tmdb/enrich/movie
Content-Type: application/json

{
  "title": "Inception",
  "downloadImage": true
}
```

**Or use specific TMDB ID:**
```bash
POST /api/tmdb/enrich/movie
Content-Type: application/json

{
  "tmdbId": 27205,
  "downloadImage": true
}
```

**Response:**
```json
{
  "tmdbId": 27205,
  "title": "Inception",
  "length": 148,
  "genres": ["Action", "Science Fiction", "Thriller"],
  "posterUrl": "https://image.tmdb.org/t/p/w500/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg",
  "savedImageId": "673abc123...",
  "overview": "Cobb steals secrets from dreams...",
  "imdbId": "tt1375666"
}
```

### Enrich Series Metadata
```bash
POST /api/tmdb/enrich/series
Content-Type: application/json

{
  "title": "Breaking Bad",
  "downloadImage": true
}
```

**Response:**
```json
{
  "tmdbId": 1396,
  "title": "Breaking Bad",
  "genres": ["Crime", "Drama", "Thriller"],
  "posterUrl": "https://image.tmdb.org/t/p/w500/...",
  "savedImageId": "673def456...",
  "totalSeasons": 5,
  "status": "Ended",
  "overview": "A chemistry teacher diagnosed..."
}
```

## Usage Workflow

### Adding a New Movie with Enrichment
```bash
# 1. Search for the movie
curl "http://localhost:8080/api/tmdb/search/movies?title=Inception"

# 2. Enrich metadata (auto-downloads cover image)
curl -X POST http://localhost:8080/api/tmdb/enrich/movie \
  -H "Content-Type: application/json" \
  -d '{
    "tmdbId": 27205,
    "downloadImage": true
  }'

# 3. Create movie with enriched data
curl -X POST http://localhost:8080/api/movies \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Inception",
    "genres": ["Action", "Science Fiction", "Thriller"],
    "length": 148,
    "coverImage": "/api/images/673abc123...",
    "tmdbId": 27205,
    "addedBy": "admin"
  }'
```

### Adding a New Series with Enrichment
```bash
# 1. Enrich series metadata
curl -X POST http://localhost:8080/api/tmdb/enrich/series \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Breaking Bad",
    "downloadImage": true
  }'

# 2. Create series with enriched data
curl -X POST http://localhost:8080/api/series \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Breaking Bad",
    "genres": ["Crime", "Drama", "Thriller"],
    "coverImage": "/api/images/673def456...",
    "totalAvailableSeasons": 5,
    "seriesStatus": "COMPLETE",
    "tmdbId": 1396,
    "addedBy": "admin"
  }'
```

## Field Mapping

### Movie Fields
| Movie Catalog Field | TMDB Field | Notes |
|---------------------|------------|-------|
| `title` | `title` | Movie title |
| `length` | `runtime` | Duration in minutes |
| `genres` | `genres[].name` | Array of genre names |
| `coverImage` | `poster_path` | Downloaded and saved locally |
| `tmdbId` | `id` | TMDB movie ID |

### Series Fields
| Movie Catalog Field | TMDB Field | Notes |
|---------------------|------------|-------|
| `title` | `name` | Series name |
| `genres` | `genres[].name` | Array of genre names |
| `totalAvailableSeasons` | `number_of_seasons` | Total season count |
| `seriesStatus` | `status` | "Ended"→COMPLETE, "Returning Series"→ONGOING |
| `coverImage` | `poster_path` | Downloaded and saved locally |
| `tmdbId` | `id` | TMDB series ID |

## Configuration

All TMDB settings are in `application.properties`:

```properties
# TMDB API Configuration
# Use API Read Access Token (Bearer token) from https://www.themoviedb.org/settings/api
tmdb.access-token=${TMDB_ACCESS_TOKEN:your_access_token_here}
tmdb.api.base-url=https://api.themoviedb.org/3
tmdb.image.base-url=https://image.tmdb.org/t/p/w500
```

**Authentication Method:**
- Uses **Bearer token** authentication (recommended by TMDB)
- Token is sent in the `Authorization: Bearer <token>` header
- Works with both v3 and v4 TMDB APIs
- More secure than query parameter API key

## Rate Limits
- **Free tier**: 1,000 requests per day
- **Rate limit**: 40 requests per 10 seconds
- Sufficient for personal use

## Timeouts
- **Connection timeout**: 5 seconds
- **Read timeout**: 10 seconds
- **Write timeout**: 10 seconds

## Error Handling
- API errors are logged but don't crash the application
- Image download failures are logged and skipped
- Search returns empty array if no results
- Enrichment throws RuntimeException if content not found

## Testing with Swagger
Access the interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

Look for the "TMDB Integration" section.

## Examples

### Search Multiple Results
```bash
curl "http://localhost:8080/api/tmdb/search/movies?title=Matrix"
```
Returns all Matrix movies for user to select the correct one.

### Enrich Without Image Download
```bash
curl -X POST http://localhost:8080/api/tmdb/enrich/movie \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Inception",
    "downloadImage": false
  }'
```
Only fetches metadata, doesn't download poster image.

## Future Enhancements
- Bulk enrichment endpoint
- Auto-enrich during movie/series creation
- Update existing entries with TMDB data
- Fetch cast and crew information
- Get season-level details for series
