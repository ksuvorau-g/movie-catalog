package com.moviecat.controller;

import com.moviecat.dto.MovieRequest;
import com.moviecat.dto.MovieResponse;
import com.moviecat.dto.PriorityRequest;
import com.moviecat.dto.WatchStatusRequest;
import com.moviecat.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for movie operations.
 */
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Tag(name = "Movies", description = "Movie management endpoints")
public class MovieController {
    
    private final MovieService movieService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add new movie", description = "Add a new movie to the catalog")
    public MovieResponse addMovie(@RequestBody MovieRequest request) {
        return movieService.addMovie(request);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get movie by ID", description = "Retrieve movie details by ID")
    public MovieResponse getMovie(@PathVariable String id) {
        return movieService.getMovieById(id);
    }
    
    @GetMapping
    @Operation(summary = "Get all movies", description = "Retrieve all movies in the catalog")
    public List<MovieResponse> getAllMovies() {
        return movieService.getAllMovies();
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update movie", description = "Update movie information")
    public MovieResponse updateMovie(@PathVariable String id, @RequestBody MovieRequest request) {
        return movieService.updateMovie(id, request);
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete movie", description = "Delete movie from catalog")
    public void deleteMovie(@PathVariable String id) {
        movieService.deleteMovie(id);
    }
    
    @PatchMapping("/{id}/watch-status")
    @Operation(summary = "Update watch status", description = "Mark movie as watched or unwatched")
    public MovieResponse updateWatchStatus(@PathVariable String id, @RequestBody WatchStatusRequest request) {
        return movieService.updateWatchStatus(id, request.getWatchStatus());
    }
    
    @PatchMapping("/{id}/priority")
    @Operation(summary = "Update priority", description = "Update movie recommendation priority")
    public MovieResponse updatePriority(@PathVariable String id, @RequestBody PriorityRequest request) {
        return movieService.updatePriority(id, request.getPriority());
    }
}
