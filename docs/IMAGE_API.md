# Image Management API

This document describes the Image Management API for the Movie Catalog application.

## Overview

The Image Management system allows you to:
- Download images from external URLs
- Store images in the local file system
- Access images by unique ID
- Associate images with movies
- Manage image metadata in MongoDB

## Storage

- **File System**: Images are stored in the `images/` folder at the root of the project
- **Database**: Image metadata is stored in MongoDB's `images` collection

## API Endpoints

### 1. Download and Save Image

Downloads an image from a URL and stores it with a unique ID.

**Endpoint**: `POST /api/images/download`

**Request Body**:
```json
{
  "imageUrl": "https://example.com/movie-poster.jpg",
  "movieId": "optional-movie-id"
}
```

**Response** (201 Created):
```json
{
  "id": "67890abcdef1234567890abc",
  "originalUrl": "https://example.com/movie-poster.jpg",
  "filename": "550e8400-e29b-41d4-a716-446655440000.jpg",
  "contentType": "image/jpeg",
  "fileSize": 245678,
  "movieId": "optional-movie-id",
  "uploadedAt": "2025-11-12T10:30:00",
  "imageUrl": "/api/images/67890abcdef1234567890abc"
}
```

### 2. Get Image by ID

Retrieves the actual image file.

**Endpoint**: `GET /api/images/{id}`

**Response**: Binary image data with appropriate Content-Type header

**Example**:
```
GET /api/images/67890abcdef1234567890abc
```

### 3. Get Image Metadata

Retrieves only the metadata for an image.

**Endpoint**: `GET /api/images/{id}/metadata`

**Response** (200 OK):
```json
{
  "id": "67890abcdef1234567890abc",
  "originalUrl": "https://example.com/movie-poster.jpg",
  "filename": "550e8400-e29b-41d4-a716-446655440000.jpg",
  "contentType": "image/jpeg",
  "fileSize": 245678,
  "movieId": "optional-movie-id",
  "uploadedAt": "2025-11-12T10:30:00",
  "imageUrl": "/api/images/67890abcdef1234567890abc"
}
```

### 4. Get Images by Movie ID

Retrieves all images associated with a specific movie.

**Endpoint**: `GET /api/images/movie/{movieId}`

**Response** (200 OK):
```json
[
  {
    "id": "67890abcdef1234567890abc",
    "originalUrl": "https://example.com/poster1.jpg",
    "filename": "550e8400-e29b-41d4-a716-446655440000.jpg",
    "contentType": "image/jpeg",
    "fileSize": 245678,
    "movieId": "movie-id-123",
    "uploadedAt": "2025-11-12T10:30:00",
    "imageUrl": "/api/images/67890abcdef1234567890abc"
  }
]
```

### 5. Delete Image

Deletes both the image file and its metadata.

**Endpoint**: `DELETE /api/images/{id}`

**Response**: 204 No Content

## Configuration

The image storage path can be configured in `application.properties`:

```properties
# Image Storage Configuration
image.storage.path=images
```

## MongoDB Schema

### images Collection

```json
{
  "_id": ObjectId,
  "originalUrl": String,
  "filename": String,
  "contentType": String,
  "fileSize": Long,
  "movieId": String (indexed),
  "uploadedAt": DateTime
}
```

## Supported Image Formats

- JPEG (.jpg, .jpeg)
- PNG (.png)
- GIF (.gif)
- WebP (.webp)

## Usage Examples

### cURL Examples

**Download an image:**
```bash
curl -X POST http://localhost:8080/api/images/download \
  -H "Content-Type: application/json" \
  -d '{
    "imageUrl": "https://image.tmdb.org/t/p/w500/example.jpg",
    "movieId": "12345"
  }'
```

**Get an image:**
```bash
curl http://localhost:8080/api/images/67890abcdef1234567890abc -o image.jpg
```

**Get image metadata:**
```bash
curl http://localhost:8080/api/images/67890abcdef1234567890abc/metadata
```

**Delete an image:**
```bash
curl -X DELETE http://localhost:8080/api/images/67890abcdef1234567890abc
```

## Error Handling

- **400 Bad Request**: Invalid URL or download failed
- **404 Not Found**: Image not found
- **500 Internal Server Error**: Server error during processing

## Notes

- Images are stored with UUID-based filenames to avoid conflicts
- The system automatically detects content type from the downloaded image
- File extensions are preserved based on content type or URL
- All operations are logged for debugging purposes
