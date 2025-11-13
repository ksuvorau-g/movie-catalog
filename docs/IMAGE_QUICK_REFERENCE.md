# Image Management API - Quick Reference

## 🚀 Quick Start

### Download an Image
```bash
POST /api/images/download
{
  "imageUrl": "https://example.com/image.jpg",
  "movieId": "optional-movie-id"
}
```

### Get Image
```bash
GET /api/images/{id}
```

### Get Image Metadata
```bash
GET /api/images/{id}/metadata
```

### Delete Image
```bash
DELETE /api/images/{id}
```

## 📁 Storage

- **Location**: `images/` folder
- **Database**: MongoDB `images` collection
- **Naming**: UUID-based filenames (e.g., `550e8400-e29b-41d4-a716-446655440000.jpg`)

## 🔗 Movie Integration

### Option 1: Store URL in Movie
```json
{
  "title": "Movie Title",
  "coverImage": "/api/images/67890abcdef1234567890abc"
}
```

### Option 2: Associate During Download
```json
{
  "imageUrl": "https://...",
  "movieId": "movie-id-123"
}
```

## 📋 Response Format

```json
{
  "id": "67890abcdef1234567890abc",
  "originalUrl": "https://...",
  "filename": "550e8400-e29b-41d4-a716-446655440000.jpg",
  "contentType": "image/jpeg",
  "fileSize": 245678,
  "movieId": "movie-id-123",
  "uploadedAt": "2025-11-12T10:30:00",
  "imageUrl": "/api/images/67890abcdef1234567890abc"
}
```

## 🎨 Supported Formats

- JPEG (.jpg, .jpeg)
- PNG (.png)
- GIF (.gif)
- WebP (.webp)

## ⚙️ Configuration

```properties
# application.properties
image.storage.path=images
```

## 🧪 Test with cURL

```bash
# Download
curl -X POST http://localhost:8080/api/images/download \
  -H "Content-Type: application/json" \
  -d '{"imageUrl":"https://picsum.photos/200"}'

# View (replace {id} with actual ID)
curl http://localhost:8080/api/images/{id} -o test.jpg

# Delete
curl -X DELETE http://localhost:8080/api/images/{id}
```

## 📚 Full Documentation

- **API Reference**: `docs/IMAGE_API.md`
- **Integration Guide**: `docs/IMAGE_INTEGRATION_EXAMPLE.md`
- **Implementation Summary**: `docs/IMAGE_SYSTEM_SUMMARY.md`
