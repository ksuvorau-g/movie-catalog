# Documentation Index

Complete documentation for the Movie Catalog project.

## Getting Started

**New to the project?** Start here:
1. [README.md](../README.md) - Project overview, quick start, API overview
2. [Architecture Guide](architecture.md) - System architecture and design patterns
3. [Series Management Guide](SERIES_MANAGEMENT_GUIDE.md) - TV series and season management

## Core Documentation

### Architecture & Design
- **[architecture.md](architecture.md)** - Complete system architecture
  - Layered architecture pattern (Controller → Service → Repository)
  - Component interaction flows
  - Domain model descriptions
  - External dependencies
  - Performance considerations
  - Series management API documentation (new section)

### Requirements
- **[raw_requirements.txt](raw_requirements.txt)** - Original requirements document
  - Feature specifications
  - Data attributes for movies and series
  - API request/response formats
  - Season data structures
  - Business rules and decisions

### Technical Specifications
- **[technical_stack.txt](technical_stack.txt)** - Technology choices and rationale

## Feature-Specific Guides

### TV Series Management
- **[SERIES_MANAGEMENT_GUIDE.md](SERIES_MANAGEMENT_GUIDE.md)** - **NEW** Comprehensive guide
  - API endpoints with examples
  - Season management patterns
  - Common use cases and workflows
  - Data structures (SeriesRequest, SeriesResponse)
  - Watch status calculation rules
  - Best practices
  - Troubleshooting guide
  - Frontend integration examples

### Image Management
- **[IMAGE_SYSTEM_SUMMARY.md](IMAGE_SYSTEM_SUMMARY.md)** - Complete image system documentation
  - Architecture and design
  - API endpoints
  - Storage strategy
  - Error handling
  
- **[IMAGE_API.md](IMAGE_API.md)** - Detailed API reference
  - Download endpoints
  - Retrieval endpoints
  - Delete endpoints
  - Request/response formats

- **[IMAGE_QUICK_REFERENCE.md](IMAGE_QUICK_REFERENCE.md)** - Quick reference guide
  - Common operations
  - Code examples
  - Best practices

- **[IMAGE_INTEGRATION_EXAMPLE.md](IMAGE_INTEGRATION_EXAMPLE.md)** - Integration examples
  - Frontend integration
  - Backend usage patterns
  - Error handling examples

## Development Resources

### Sample Data
- **[SAMPLE_DATA.md](../SAMPLE_DATA.md)** - Sample data for testing
  - Example movies
  - Example series with seasons
  - Test scenarios

### Scripts
- **[populate-movies.sh](../populate-movies.sh)** - Script to populate sample data
- **[update-statuses.sh](../update-statuses.sh)** - Script to update watch statuses

### Development Setup
- **[frontend/DEV_SETUP.md](../frontend/DEV_SETUP.md)** - Frontend development setup

## TODO & Planning
- **[TODO.md](TODO.md)** - Planned features and improvements

## Quick Reference by Topic

### Movies
- **Create/Update/Delete**: See [architecture.md](architecture.md) - MovieController section
- **Watch Status**: PATCH `/api/movies/{id}/watch-status`
- **Priority**: PATCH `/api/movies/{id}/priority`

### TV Series
- **Complete Guide**: [SERIES_MANAGEMENT_GUIDE.md](SERIES_MANAGEMENT_GUIDE.md)
- **API Reference**: See [architecture.md](architecture.md) - Series Management Guide section
- **Quick Examples**:
  - Create with seasons: POST `/api/series` with `seasons` array
  - Mark season: PATCH `/api/series/{id}/seasons/{seasonNumber}/watch-status`
  - Mark all: PATCH `/api/series/{id}/watch-status`
  - Update metadata only: PUT `/api/series/{id}` (omit seasons field)

### Seasons
- **Management Patterns**: [SERIES_MANAGEMENT_GUIDE.md](SERIES_MANAGEMENT_GUIDE.md) - Season Management Patterns
- **Data Structure**: `{"seasonNumber": integer, "watchStatus": "WATCHED"|"UNWATCHED"}`
- **Watch Status Rules**: [SERIES_MANAGEMENT_GUIDE.md](SERIES_MANAGEMENT_GUIDE.md) - Watch Status Calculation Rules
- **Common Operations**:
  - Add season: PATCH `/api/series/{id}/seasons/{seasonNumber}/watch-status` (auto-creates if not exists)
  - Mark watched: `{"watchStatus": "WATCHED"}`
  - Mark unwatched: `{"watchStatus": "UNWATCHED"}`

### Images
- **Complete Guide**: [IMAGE_SYSTEM_SUMMARY.md](IMAGE_SYSTEM_SUMMARY.md)
- **Quick Reference**: [IMAGE_QUICK_REFERENCE.md](IMAGE_QUICK_REFERENCE.md)
- **API Details**: [IMAGE_API.md](IMAGE_API.md)

### Recommendations
- **Algorithm**: See [architecture.md](architecture.md) - RecommendationService section
- **Endpoint**: GET `/api/recommendations`
- **Priority Factors**: Manual priority > New seasons > Age-based weighting

### Notifications
- **Creation**: Automatic when new seasons detected
- **Conditions**: Only for series with ≥1 watched season
- **Endpoints**: GET `/api/notifications`, DELETE `/api/notifications/{id}`

### Catalog
- **Combined List**: GET `/api/catalog` (movies + series)
- **Filtering**: By genre, watch status, content type, series status, etc.
- **Sorting**: Unwatched first → Priority → Date added

## API Documentation

### Interactive Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui.html (when running)

### REST Endpoints
All endpoints documented in:
- [architecture.md](architecture.md) - Architecture Layers → Presentation Layer
- [SERIES_MANAGEMENT_GUIDE.md](SERIES_MANAGEMENT_GUIDE.md) - API Endpoints Summary

## Troubleshooting Guides

### Series Issues
- [SERIES_MANAGEMENT_GUIDE.md](SERIES_MANAGEMENT_GUIDE.md) - Troubleshooting section
  - Seasons not saving
  - Watch status not updating
  - Cannot add seasons
  - PUT removes seasons
  - Series not in recommendations

### Image Issues
- [IMAGE_SYSTEM_SUMMARY.md](IMAGE_SYSTEM_SUMMARY.md) - Error Handling section
  - Download failures
  - Storage issues
  - Retrieval errors

### General Issues
- [README.md](../README.md) - Troubleshooting section
  - MongoDB connection
  - Docker issues
  - Development setup

## Documentation Updates

### Recent Additions
- ✅ **Series Management Guide** - Comprehensive guide for TV series and season management
- ✅ **Series API Documentation** - Added to architecture.md with complete endpoint reference
- ✅ **Season Data Structures** - Added to raw_requirements.txt
- ✅ **SeriesRequest seasons field** - Fixed and documented

### Document Status
| Document | Last Updated | Status |
|----------|-------------|--------|
| README.md | Nov 2025 | ✅ Complete |
| architecture.md | Nov 2025 | ✅ Complete (includes series guide) |
| SERIES_MANAGEMENT_GUIDE.md | Nov 2025 | ✅ Complete |
| raw_requirements.txt | Nov 2025 | ✅ Complete (includes series API) |
| IMAGE_SYSTEM_SUMMARY.md | Current | ✅ Complete |
| IMAGE_API.md | Current | ✅ Complete |
| IMAGE_QUICK_REFERENCE.md | Current | ✅ Complete |

## Contributing to Documentation

When adding new features:
1. Update [architecture.md](architecture.md) with architectural changes
2. Update [raw_requirements.txt](raw_requirements.txt) if requirements change
3. Create feature-specific guide if complex (like SERIES_MANAGEMENT_GUIDE.md)
4. Update [README.md](../README.md) with new API endpoints
5. Update this index with new documentation
6. Add examples to [SAMPLE_DATA.md](../SAMPLE_DATA.md) if applicable

## Questions?

- **Architecture Questions**: See [architecture.md](architecture.md)
- **Series/Season Questions**: See [SERIES_MANAGEMENT_GUIDE.md](SERIES_MANAGEMENT_GUIDE.md)
- **Image Questions**: See [IMAGE_SYSTEM_SUMMARY.md](IMAGE_SYSTEM_SUMMARY.md)
- **API Questions**: See Swagger UI or architecture.md
- **Setup Questions**: See [README.md](../README.md) or frontend/DEV_SETUP.md
