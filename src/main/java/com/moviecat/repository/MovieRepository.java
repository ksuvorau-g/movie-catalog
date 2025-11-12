package com.moviecat.repository;

import com.moviecat.model.Movie;
import com.moviecat.model.WatchStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Movie entity.
 * Provides CRUD operations and custom query methods for movies.
 */
@Repository
public interface MovieRepository extends MongoRepository<Movie, String> {
    
    /**
     * Find movies by title (case-insensitive).
     * Useful for duplicate detection.
     * 
     * @param title the movie title
     * @return list of movies with matching title
     */
    List<Movie> findByTitleIgnoreCase(String title);
    
    /**
     * Find movies by watch status.
     * 
     * @param watchStatus the watch status (WATCHED or UNWATCHED)
     * @return list of movies with the specified watch status
     */
    List<Movie> findByWatchStatus(WatchStatus watchStatus);
    
    /**
     * Find movies by genre (case-insensitive).
     * 
     * @param genre the genre name
     * @return list of movies containing the specified genre
     */
    List<Movie> findByGenresContainingIgnoreCase(String genre);
    
    /**
     * Find movies added by a specific person.
     * 
     * @param addedBy the name of the person who added the movies
     * @return list of movies added by the specified person
     */
    List<Movie> findByAddedBy(String addedBy);
    
    /**
     * Find movies by watch status and added by.
     * 
     * @param watchStatus the watch status
     * @param addedBy the name of the person who added the movies
     * @return list of movies matching both criteria
     */
    List<Movie> findByWatchStatusAndAddedBy(WatchStatus watchStatus, String addedBy);
}
