#!/bin/bash

API_URL="http://localhost:8080/api/movies"

echo "Updating watch statuses for movies..."

# Get all movies and update their watch statuses
curl -s "$API_URL" | grep -o '"id":"[^"]*"' | sed 's/"id":"//;s/"//' | while read -r id; do
    title=$(curl -s "$API_URL/$id" | grep -o '"title":"[^"]*"' | sed 's/"title":"//;s/"//')
    
    case "$title" in
        "The Shawshank Redemption"|"The Godfather"|"The Dark Knight"|"Pulp Fiction"|"The Matrix"|"Fight Club")
            status="WATCHED"
            ;;
        "Inception"|"The Prestige"|"Whiplash")
            status="WATCHING"
            ;;
        *)
            status="TO_WATCH"
            ;;
    esac
    
    if [ "$title" != "Example movie" ]; then
        echo "Updating $title to $status..."
        curl -s -X PATCH "$API_URL/$id/watch-status" \
          -H "Content-Type: application/json" \
          -d "{\"watchStatus\": \"$status\"}" > /dev/null
    fi
done

echo ""
echo "✅ Watch statuses updated successfully!"
