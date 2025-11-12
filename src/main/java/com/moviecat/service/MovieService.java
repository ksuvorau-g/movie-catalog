package com.moviecat.service;

import com.moviecat.dto.MovieRequest;
import com.moviecat.dto.MovieResponse;
import com.moviecat.model.Movie;
import com.moviecat.model.WatchStatus;
import com.moviecat.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for movie-related operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MovieService {
    
    private final MovieRepository movieRepository;
    
    /**
     * Add a new movie to the catalog.
     * Checks for duplicates and logs a warning if found.
     * 
     * @param request movie details
     * @return created movie
     */
    public MovieResponse addMovie(MovieRequest request) {
        log.info("Adding new movie: {}", request.getTitle());
        
        // Check for duplicates
        List<Movie> existingMovies = movieRepository.findByTitleIgnoreCase(request.getTitle());
        if (!existingMovies.isEmpty()) {
            log.warn("Movie with title '{}' already exists in the catalog", request.getTitle());
        }
        
        Movie movie = Movie.builder()
                .title(request.getTitle())
                .link(request.getLink())
                .linkDescription(request.getLinkDescription())
                .comment(request.getComment())
                .coverImage(request.getCoverImage())
                .length(request.getLength())
                .genres(request.getGenres())
                .watchStatus(WatchStatus.UNWATCHED)
                .addedBy(request.getAddedBy())
                .dateAdded(LocalDateTime.now())
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .build();
        
        Movie savedMovie = movieRepository.save(movie);
        log.info("Movie added successfully with id: {}", savedMovie.getId());
        
        return toResponse(savedMovie);
    }
    
    /**
     * Get movie by ID.
     * 
     * @param id movie ID
     * @return movie details
     * @throws RuntimeException if movie not found
     */
    public MovieResponse getMovieById(String id) {
        log.info("Getting movie by id: {}", id);
        
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
        
        return toResponse(movie);
    }
    
    /**
     * Get all movies.
     * 
     * @return list of all movies
     */
    public List<MovieResponse> getAllMovies() {
        log.info("Getting all movies");
        
        List<Movie> movies = movieRepository.findAll();
        log.info("Found {} movies", movies.size());
        
        return movies.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Update movie information.
     * 
     * @param id movie ID
     * @param request updated movie details
     * @return updated movie
     * @throws RuntimeException if movie not found
     */
    public MovieResponse updateMovie(String id, MovieRequest request) {
        log.info("Updating movie: {}", id);
        
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
        
        // Update fields
        movie.setTitle(request.getTitle());
        movie.setLink(request.getLink());
        movie.setLinkDescription(request.getLinkDescription());
        movie.setComment(request.getComment());
        movie.setCoverImage(request.getCoverImage());
        movie.setLength(request.getLength());
        movie.setGenres(request.getGenres());
        movie.setAddedBy(request.getAddedBy());
        if (request.getPriority() != null) {
            movie.setPriority(request.getPriority());
        }
        
        Movie updatedMovie = movieRepository.save(movie);
        log.info("Movie updated successfully: {}", id);
        
        return toResponse(updatedMovie);
    }
    
    /**
     * Delete movie from catalog.
     * 
     * @param id movie ID
     * @throws RuntimeException if movie not found
     */
    public void deleteMovie(String id) {
        log.info("Deleting movie: {}", id);
        
        if (!movieRepository.existsById(id)) {
            throw new RuntimeException("Movie not found with id: " + id);
        }
        
        movieRepository.deleteById(id);
        log.info("Movie deleted successfully: {}", id);
    }
    
    /**
     * Update movie watch status.
     * 
     * @param id movie ID
     * @param watchStatus new watch status
     * @return updated movie
     * @throws RuntimeException if movie not found
     */
    public MovieResponse updateWatchStatus(String id, WatchStatus watchStatus) {
        log.info("Updating watch status for movie {}: {}", id, watchStatus);
        
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
        
        movie.setWatchStatus(watchStatus);
        Movie updatedMovie = movieRepository.save(movie);
        
        log.info("Watch status updated successfully for movie: {}", id);
        return toResponse(updatedMovie);
    }
    
    /**
     * Update movie priority.
     * 
     * @param id movie ID
     * @param priority new priority value
     * @return updated movie
     * @throws RuntimeException if movie not found
     */
    public MovieResponse updatePriority(String id, Integer priority) {
        log.info("Updating priority for movie {}: {}", id, priority);
        
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
        
        movie.setPriority(priority);
        Movie updatedMovie = movieRepository.save(movie);
        
        log.info("Priority updated successfully for movie: {}", id);
        return toResponse(updatedMovie);
    }
    
    /**
     * Convert Movie entity to MovieResponse DTO.
     * 
     * @param movie movie entity
     * @return movie response DTO
     */
    private MovieResponse toResponse(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .link(movie.getLink())
                .linkDescription(movie.getLinkDescription())
                .comment(movie.getComment())
                .coverImage(movie.getCoverImage())
                .length(movie.getLength())
                .genres(movie.getGenres())
                .watchStatus(movie.getWatchStatus())
                .addedBy(movie.getAddedBy())
                .dateAdded(movie.getDateAdded())
                .priority(movie.getPriority())
                .build();
    }
}