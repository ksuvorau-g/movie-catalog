# Movie Catalog

A personal media tracking system with Spring Boot REST API backend and React frontend. Track movies and TV series with intelligent recommendations, automated season tracking, and notifications for new seasons.

## Features

- 🎬 **Movie Management**: Add, track, and mark movies as watched/unwatched
- 📺 **TV Series Tracking**: Season-by-season progress tracking with automatic status calculation
- 🎯 **Smart Recommendations**: Weighted algorithm prioritizing older unwatched content and series with new seasons
- 🔔 **Season Notifications**: Automatic detection of new seasons via IMDB/Kinopoisk scraping
- 🖼️ **Image Management**: Download and store images locally with metadata tracking
- 🔍 **Advanced Filtering**: Filter by genre, watch status, content type, series status, and more
- 📊 **Priority System**: Manual priority adjustment for recommendation weighting

## Tech Stack

- **Backend**: Java 21, Spring Boot 3.5.7, MongoDB
- **Frontend**: React 18, Webpack 5
- **Containerization**: Docker, Docker Compose
- **API Documentation**: Swagger/OpenAPI 3

## Quick Start

### Prerequisites
- Docker and Docker Compose
- Make (optional, for convenience commands)

### Run Full Stack
```bash
# Start all services (backend, frontend, MongoDB)
make run

# Or with docker-compose directly
docker-compose up --build
```

Access the application:
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/swagger-ui.html

### Development Mode (Hot Reload)
```bash
# Backend in Docker, frontend with hot reload
make serve
```

This runs backend containers and starts frontend dev server with Webpack hot reload on port 3000.

### Run Tests
```bash
make test

# Or with Maven directly
mvn test
```

## Documentation

### Core Documentation
- **[Architecture Guide](docs/architecture.md)** - Complete system architecture, layers, and component interactions
- **[Series Management Guide](docs/SERIES_MANAGEMENT_GUIDE.md)** - Comprehensive guide for TV series and season management
- **[Image System Summary](docs/IMAGE_SYSTEM_SUMMARY.md)** - Image download, storage, and retrieval documentation
- **[Requirements](docs/raw_requirements.txt)** - Original requirements and feature specifications

### API Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui.html (when running)
- **Image API Guide**: [docs/IMAGE_API.md](docs/IMAGE_API.md)

### Quick References
- **Image Integration**: [docs/IMAGE_QUICK_REFERENCE.md](docs/IMAGE_QUICK_REFERENCE.md)
- **Sample Data**: [SAMPLE_DATA.md](SAMPLE_DATA.md)

## API Overview

### Movies
```bash
POST   /api/movies                    # Create movie
GET    /api/movies                    # List all movies
GET    /api/movies/{id}               # Get movie details
PUT    /api/movies/{id}               # Update movie
DELETE /api/movies/{id}               # Delete movie
PATCH  /api/movies/{id}/watch-status  # Mark watched/unwatched
PATCH  /api/movies/{id}/priority      # Update priority
```

### TV Series
```bash
POST   /api/series                                          # Create series
GET    /api/series                                          # List all series
GET    /api/series/{id}                                     # Get series details
PUT    /api/series/{id}                                     # Update series
DELETE /api/series/{id}                                     # Delete series
PATCH  /api/series/{id}/seasons/{seasonNumber}/watch-status # Mark season watched/unwatched
PATCH  /api/series/{id}/watch-status                        # Mark all seasons
PATCH  /api/series/{id}/priority                            # Update priority
POST   /api/series/{id}/refresh                             # Refresh seasons from external source
```

### Catalog & Recommendations
```bash
GET /api/catalog              # Combined movies + series list (with filtering)
GET /api/recommendations      # Get next recommendation
GET /api/notifications        # List notifications
DELETE /api/notifications/{id} # Dismiss notification
```

### Images
```bash
POST   /api/images/download   # Download image from URL
GET    /api/images/{id}       # Get image file
DELETE /api/images/{id}       # Delete image
```

## Series Management

TV series have more complex management than movies due to season tracking. Here are the main patterns:

### Create Series with Seasons
```json
POST /api/series
{
  "title": "Breaking Bad",
  "link": "https://imdb.com/title/tt0903747",
  "seasons": [
    {"seasonNumber": 1, "watchStatus": "UNWATCHED"},
    {"seasonNumber": 2, "watchStatus": "UNWATCHED"}
  ],
  "priority": 5
}
```

### Mark Individual Season
```bash
PATCH /api/series/{id}/seasons/1/watch-status
{"watchStatus": "WATCHED"}
```

### Mark Entire Series
```bash
PATCH /api/series/{id}/watch-status
{"watchStatus": "WATCHED"}
```

**Important**: 
- Series watch status is auto-calculated (WATCHED if all seasons watched, otherwise UNWATCHED)
- Seasons can be added during creation or progressively via PATCH
- No season order enforcement - can skip seasons
- See [Series Management Guide](docs/SERIES_MANAGEMENT_GUIDE.md) for comprehensive documentation

## Key Concepts

### Watch Status
- **Movies**: Direct `watchStatus` field (WATCHED/UNWATCHED)
- **Series**: `seriesWatchStatus` auto-calculated from all seasons
  - WATCHED: All seasons are watched
  - UNWATCHED: Any season is unwatched or no seasons exist

### Recommendation Algorithm
Weighted random selection with priority tiers:
1. **Manual priority**: User-set priority values (higher = more likely)
2. **New seasons**: Series with `hasNewSeasons=true` get 10x weight
3. **Age-based**: Older unwatched content prioritized via logarithmic weighting

### Season Refresh
- **Automatic**: Weekly on Mondays at midnight
- **Manual**: POST `/api/series/{id}/refresh`
- **Requirements**: Series must have `link` field set (IMDB/Kinopoisk URL)
- **Updates**: `totalAvailableSeasons`, `seriesStatus`, `hasNewSeasons`

### Notifications
- Generated when new seasons detected
- Only for series with ≥1 watched season
- Persist until manually dismissed
- No auto-dismissal

## Project Structure

```
movie-catalog/
├── src/main/java/com/moviecat/
│   ├── controller/       # REST endpoints
│   ├── service/          # Business logic
│   ├── repository/       # MongoDB data access
│   ├── model/            # Domain entities
│   ├── dto/              # Request/response objects
│   └── config/           # Configuration
├── frontend/
│   └── src/
│       ├── components/   # React components
│       ├── App.jsx       # Main app
│       └── styles.css    # Styling
├── docs/                 # Documentation
├── images/               # Downloaded images storage
└── docker-compose.yml    # Service orchestration
```

## Configuration

### Backend (application.properties)
```properties
server.port=8080
spring.data.mongodb.uri=mongodb://localhost:27017/moviecat
scheduler.cron.season-check=0 0 0 * * MON  # Weekly on Mondays
image.storage.path=images
```

### Environment Variables
```bash
SPRING_DATA_MONGODB_URI=mongodb://mongodb:27017/moviecat  # Docker override
```

## Development

### Backend Development
```bash
# Run tests
mvn test

# Build
mvn clean package

# Run locally (requires MongoDB)
mvn spring-boot:run
```

### Frontend Development
```bash
cd frontend
npm install
npm start  # Starts dev server on port 3000
```

### Docker Commands
```bash
# Build images
docker-compose build

# Start services
docker-compose up

# Stop services
docker-compose down

# View logs
docker-compose logs -f app
```

## Database

### MongoDB Collections
- **movies**: Movie documents
- **series**: Series documents (with embedded seasons)
- **notifications**: New season notifications
- **images**: Image metadata

### Indexes
Created automatically on:
- `title`, `watchStatus`, `addedBy`, `dateAdded` (movies & series)
- `hasNewSeasons`, `seriesStatus` (series)
- `priority` (movies & series)

## Troubleshooting

### Seasons not saving
- Ensure `seasons` array included in POST request body
- Verify JSON structure: `[{"seasonNumber": 1, "watchStatus": "UNWATCHED"}]`
- Check backend logs for validation errors

### Series watch status not updating
- Verify ALL seasons are marked WATCHED for series to be WATCHED
- Check MongoDB document has seasons properly saved
- Ensure `updateSeriesWatchStatus()` called in service layer

### Images not loading
- Verify image was downloaded via POST `/api/images/download`
- Check `coverImage` field uses format `/api/images/{id}`
- Ensure `images/` directory exists and is writable

### Cannot connect to MongoDB
- Check MongoDB container is running: `docker ps`
- Verify connection string in `application.properties` or environment
- For Docker: Use `mongodb://mongodb:27017/moviecat`
- For local: Use `mongodb://localhost:27017/moviecat`

## Contributing

This is a personal project, but suggestions and improvements are welcome!

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is for personal use.

## Contact

For questions or issues, please create an issue in the repository.
