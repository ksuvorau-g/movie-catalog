#!/bin/bash

API_URL="http://localhost:8080/api/movies"

echo "Adding 15 movies to the catalog..."

# Movie 1: The Shawshank Redemption
curl -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "The Shawshank Redemption",
    "coverImage": "https://image.tmdb.org/t/p/w500/q6y0Go1tsGEsmtFryDOJo3dEmqu.jpg",
    "genres": ["Drama"],
    "length": 142,
    "watchStatus": "WATCHED",
    "addedBy": "admin",
    "priority": 5
  }'
echo ""

# Movie 2: The Godfather
curl -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "The Godfather",
    "coverImage": "https://image.tmdb.org/t/p/w500/3bhkrj58Vtu7enYsRolD1fZdja1.jpg",
    "genres": ["Crime", "Drama"],
    "length": 175,
    "watchStatus": "WATCHED",
    "addedBy": "admin",
    "priority": 5
  }'
echo ""

# Movie 3: The Dark Knight
curl -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "The Dark Knight",
    "coverImage": "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
    "genres": ["Action", "Crime", "Drama"],
    "length": 152,
    "watchStatus": "WATCHED",
    "addedBy": "admin",
    "priority": 5
  }'
echo ""

# Movie 4: Inception
curl -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Inception",
    "coverImage": "https://image.tmdb.org/t/p/w500/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg",
    "genres": ["Action", "Science Fiction", "Adventure"],
    "length": 148,
    "watchStatus": "WATCHING",
    "addedBy": "admin",
    "priority": 4
  }'
echo ""

# Movie 5: Pulp Fiction
curl -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Pulp Fiction",
    "coverImage": "https://image.tmdb.org/t/p/w500/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg",
    "genres": ["Thriller", "Crime"],
    "length": 154,
    "watchStatus": "WATCHED",
    "addedBy": "admin",
    "priority": 5
  }'
echo ""

# Movie 6: Forrest Gump
curl -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Forrest Gump",
    "coverImage": "https://image.tmdb.org/t/p/w500/arw2vcBveWOVZr6pxd9XTd1TdQa.jpg",
    "genres": ["Comedy", "Drama", "Romance"],
    "length": 142,
    "watchStatus": "WATCHED",
    "addedBy": "admin",
    "priority": 4
  }'
echo ""

# Movie 7: Interstellar
curl -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Interstellar",
    "coverImage": "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
    "genres": ["Adventure", "Drama", "Science Fiction"],
    "length": 169,
    "watchStatus": "TO_WATCH",
    "addedBy": "john",
    "priority": 5
  }'
echo ""

# Movie 8: The Matrix
curl -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "The Matrix",
    "coverImage": "https://image.tmdb.org/t/p/w500/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg",
    "genres": ["Action", "Science Fiction"],
    "length": 136,
    "watchStatus": "WATCHED",
    "addedBy": "admin",
    "priority": 5
  }'
echo ""

# Movie 9: Gladiator
curl -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Gladiator",
    "coverImage": "https://image.tmdb.org/t/p/w500/ty8TGRuvJLPUmAR1H1nRIsgwvim.jpg",
    "genres": ["Action", "Drama", "Adventure"],
    "length": 155,
    "watchStatus": "TO_WATCH",
    "addedBy": "sarah",
    "priority": 3
  }'
echo ""

# Movie 10: The Prestige
curl -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "The Prestige",
    "coverImage": "https://image.tmdb.org/t/p/w500/tRNlZbgNCNOpLpbPEz5L8G8A0JN.jpg",
    "genres": ["Drama", "Mystery", "Thriller"],
    "length": 130,
    "watchStatus": "WATCHING",
    "addedBy": "admin",
    "priority": 4
  }'
echo ""

# Movie 11: Fight Club
curl -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Fight Club",
    "coverImage": "https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
    "genres": ["Drama"],
    "length": 139,
    "watchStatus": "WATCHED",
    "addedBy": "admin",
    "priority": 4
  }'
echo ""

# Movie 12: The Lord of the Rings: The Return of the King
curl -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "The Lord of the Rings: The Return of the King",
    "coverImage": "https://image.tmdb.org/t/p/w500/rCzpDGLbOoPwLjy3OAm5NUPOTrC.jpg",
    "genres": ["Adventure", "Fantasy", "Action"],
    "length": 201,
    "watchStatus": "TO_WATCH",
    "addedBy": "john",
    "priority": 5
  }'
echo ""

# Movie 13: Parasite
curl -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Parasite",
    "coverImage": "https://image.tmdb.org/t/p/w500/7IiTTgloJzvGI1TAYymCfbfl3vT.jpg",
    "genres": ["Comedy", "Thriller", "Drama"],
    "length": 132,
    "watchStatus": "TO_WATCH",
    "addedBy": "sarah",
    "priority": 5
  }'
echo ""

# Movie 14: Whiplash
curl -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Whiplash",
    "coverImage": "https://image.tmdb.org/t/p/w500/7fn624j5lj3xTme2SgiLCeuedmO.jpg",
    "genres": ["Drama", "Music"],
    "length": 107,
    "watchStatus": "WATCHING",
    "addedBy": "admin",
    "priority": 3
  }'
echo ""

# Movie 15: Goodfellas
curl -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Goodfellas",
    "coverImage": "https://image.tmdb.org/t/p/w500/aKuFiU82s5ISJpGZp7YkIr3kCUd.jpg",
    "genres": ["Drama", "Crime"],
    "length": 145,
    "watchStatus": "TO_WATCH",
    "addedBy": "john",
    "priority": 4
  }'
echo ""

echo ""
echo "✅ Successfully added 15 movies to the catalog!"
echo "Visit http://localhost:3000 to see them in the frontend"
