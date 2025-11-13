# Integration Example: Using Images with Movies

This document shows how to use the Image Management API together with the Movie API.

## Scenario: Adding a Movie with Downloaded Poster

### Step 1: Download the Movie Poster

First, download and store the poster image:

```bash
curl -X POST http://localhost:8080/api/images/download \
  -H "Content-Type: application/json" \
  -d '{
    "imageUrl": "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg"
  }' | jq
```

**Response:**
```json
{
  "id": "67890abcdef1234567890abc",
  "originalUrl": "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
  "filename": "550e8400-e29b-41d4-a716-446655440000.jpg",
  "contentType": "image/jpeg",
  "fileSize": 245678,
  "movieId": null,
  "uploadedAt": "2025-11-12T10:30:00",
  "imageUrl": "/api/images/67890abcdef1234567890abc"
}
```

Save the `id` value: `67890abcdef1234567890abc`

### Step 2: Create the Movie with Image Reference

Now create a movie entry using the image URL:

```bash
curl -X POST http://localhost:8080/api/movies \
  -H "Content-Type: application/json" \
  -d '{
    "title": "The Matrix",
    "link": "https://netflix.com/matrix",
    "linkDescription": "https://www.imdb.com/title/tt0133093/",
    "comment": "Mind-bending sci-fi classic",
    "coverImage": "/api/images/67890abcdef1234567890abc",
    "length": 136,
    "genres": ["Sci-Fi", "Action"],
    "addedBy": "Alice",
    "priority": 5
  }' | jq
```

**Note**: The `coverImage` field now contains the path to access the locally stored image.

### Step 3: Associate Image with Movie (Optional)

If you want to update the image record to link it to the movie:

```bash
# First get the movie ID from the response above (e.g., "movie123")

# Then download a new image directly associated with that movie:
curl -X POST http://localhost:8080/api/images/download \
  -H "Content-Type: application/json" \
  -d '{
    "imageUrl": "https://example.com/alternate-poster.jpg",
    "movieId": "movie123"
  }'
```

### Step 4: Retrieve All Images for a Movie

```bash
curl http://localhost:8080/api/images/movie/movie123 | jq
```

This returns all images associated with that movie.

## Alternative Workflow: Store Image Reference in Movie

You can also store the image ID directly in the movie and retrieve it:

### Option 1: Store Full URL in Movie

```json
{
  "title": "Inception",
  "coverImage": "/api/images/67890abcdef1234567890abc"
}
```

Then in your frontend:
```javascript
// Direct image access
<img src={`http://localhost:8080${movie.coverImage}`} alt={movie.title} />
```

### Option 2: Store Image ID and Build URL

```json
{
  "title": "Inception",
  "coverImageId": "67890abcdef1234567890abc"
}
```

Then in your frontend:
```javascript
// Build URL from ID
<img src={`http://localhost:8080/api/images/${movie.coverImageId}`} alt={movie.title} />
```

## Benefits of This Approach

1. **Local Storage**: Images are stored locally, reducing dependency on external URLs
2. **Consistent Access**: Images remain accessible even if original URL changes or becomes unavailable
3. **Metadata Tracking**: Store additional information (file size, upload date, etc.)
4. **Flexible Association**: Images can be linked to movies or kept independent
5. **Easy Management**: Delete or update images independently of movies

## Frontend Integration Example (React)

```jsx
import React, { useState } from 'react';

function MoviePosterUpload({ movieId }) {
  const [imageUrl, setImageUrl] = useState('');
  const [uploadedImage, setUploadedImage] = useState(null);

  const handleDownload = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/images/download', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          imageUrl: imageUrl,
          movieId: movieId
        })
      });
      
      const data = await response.json();
      setUploadedImage(data);
      console.log('Image downloaded:', data);
    } catch (error) {
      console.error('Error downloading image:', error);
    }
  };

  return (
    <div>
      <input 
        type="text" 
        value={imageUrl} 
        onChange={(e) => setImageUrl(e.target.value)}
        placeholder="Enter image URL"
      />
      <button onClick={handleDownload}>Download & Save</button>
      
      {uploadedImage && (
        <div>
          <p>Image saved with ID: {uploadedImage.id}</p>
          <img 
            src={`http://localhost:8080${uploadedImage.imageUrl}`} 
            alt="Movie poster" 
            style={{ maxWidth: '200px' }}
          />
        </div>
      )}
    </div>
  );
}

export default MoviePosterUpload;
```

## Complete Workflow Example

```bash
# 1. Download poster from TMDB
IMAGE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/images/download \
  -H "Content-Type: application/json" \
  -d '{
    "imageUrl": "https://image.tmdb.org/t/p/w500/poster.jpg"
  }')

IMAGE_ID=$(echo $IMAGE_RESPONSE | jq -r '.id')
IMAGE_URL=$(echo $IMAGE_RESPONSE | jq -r '.imageUrl')

echo "Image ID: $IMAGE_ID"
echo "Image URL: $IMAGE_URL"

# 2. Create movie with the image
MOVIE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/movies \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"The Matrix\",
    \"coverImage\": \"$IMAGE_URL\",
    \"genres\": [\"Sci-Fi\", \"Action\"],
    \"addedBy\": \"Script\"
  }")

MOVIE_ID=$(echo $MOVIE_RESPONSE | jq -r '.id')

echo "Movie ID: $MOVIE_ID"

# 3. View the movie with its image
curl http://localhost:8080/api/movies/$MOVIE_ID | jq

# 4. Access the image directly
curl http://localhost:8080$IMAGE_URL -o downloaded_poster.jpg

echo "Poster saved as downloaded_poster.jpg"
```
