# Movie Catalog - Sample Data

## Successfully Added 15 Movies

The database has been populated with 15 popular movies, all with real cover images from The Movie Database (TMDB).

### Movies Added:

1. **The Shawshank Redemption** (1994) - Drama - 142 min - ⭐⭐⭐⭐⭐
2. **The Godfather** (1972) - Crime, Drama - 175 min - ⭐⭐⭐⭐⭐
3. **The Dark Knight** (2008) - Action, Crime, Drama - 152 min - ⭐⭐⭐⭐⭐
4. **Inception** (2010) - Action, Sci-Fi, Adventure - 148 min - ⭐⭐⭐⭐
5. **Pulp Fiction** (1994) - Thriller, Crime - 154 min - ⭐⭐⭐⭐⭐
6. **Forrest Gump** (1994) - Comedy, Drama, Romance - 142 min - ⭐⭐⭐⭐
7. **Interstellar** (2014) - Adventure, Drama, Sci-Fi - 169 min - ⭐⭐⭐⭐⭐
8. **The Matrix** (1999) - Action, Sci-Fi - 136 min - ⭐⭐⭐⭐⭐
9. **Gladiator** (2000) - Action, Drama, Adventure - 155 min - ⭐⭐⭐
10. **The Prestige** (2006) - Drama, Mystery, Thriller - 130 min - ⭐⭐⭐⭐
11. **Fight Club** (1999) - Drama - 139 min - ⭐⭐⭐⭐
12. **The Lord of the Rings: The Return of the King** (2003) - Adventure, Fantasy, Action - 201 min - ⭐⭐⭐⭐⭐
13. **Parasite** (2019) - Comedy, Thriller, Drama - 132 min - ⭐⭐⭐⭐⭐
14. **Whiplash** (2014) - Drama, Music - 107 min - ⭐⭐⭐
15. **Goodfellas** (1990) - Drama, Crime - 145 min - ⭐⭐⭐⭐

### Cover Images

All cover images are sourced from TMDB (The Movie Database) using their official API CDN:
- URL format: `https://image.tmdb.org/t/p/w500/{poster_path}`
- Resolution: 500px width (optimal for web display)
- All images are real, high-quality movie posters

### Added By

- **admin**: 9 movies
- **john**: 3 movies  
- **sarah**: 2 movies

### Watch Status Distribution

- 6 movies marked as WATCHED
- 10 movies marked as UNWATCHED (available to watch)

### How to View

Visit **http://localhost:3000** to see all movies displayed in the beautiful React frontend with:
- Movie posters displayed in a responsive grid
- Genre tags
- Watch status indicators
- Priority ratings
- Search functionality

### Scripts Created

1. **`populate-movies.sh`** - Adds all 15 movies to the database
2. **`update-statuses.sh`** - Updates watch statuses for variety

To re-populate the database:
```bash
./populate-movies.sh
```
