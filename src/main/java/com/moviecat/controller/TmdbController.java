package com.moviecat.controller;

import com.moviecat.dto.TmdbEnrichmentRequest;
import com.moviecat.dto.TmdbEnrichmentResponse;
import com.moviecat.dto.tmdb.TmdbSearchResult;
import com.moviecat.service.TmdbEnrichmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for TMDB integration endpoints.
 * Provides metadata enrichment from The Movie Database API.
 */
@RestController
@RequestMapping("/api/tmdb")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "TMDB Integration", description = "Endpoints for enriching content metadata from TMDB")
public class TmdbController {
    
    private final TmdbEnrichmentService tmdbEnrichmentService;
    
    /**
     * Search for movies in TMDB by title.
     * Returns list of matching results for user to select from.
     */
    @GetMapping("/search/movies")
    @Operation(summary = "Search movies in TMDB", 
               description = "Search for movies by title in The Movie Database")
    public ResponseEntity<List<TmdbSearchResult>> searchMovies(
            @Parameter(description = "Movie title to search for")
            @RequestParam String title) {
        log.info("Searching TMDB for movies with title: {}", title);
        List<TmdbSearchResult> results = tmdbEnrichmentService.searchMovies(title);
        return ResponseEntity.ok(results);
    }
    
    /**
     * Search for TV series in TMDB by title.
     * Returns list of matching results for user to select from.
     */
    @GetMapping("/search/series")
    @Operation(summary = "Search TV series in TMDB", 
               description = "Search for TV series by title in The Movie Database")
    public ResponseEntity<List<TmdbSearchResult>> searchSeries(
            @Parameter(description = "Series title to search for")
            @RequestParam String title) {
        log.info("Searching TMDB for series with title: {}", title);
        List<TmdbSearchResult> results = tmdbEnrichmentService.searchSeries(title);
        return ResponseEntity.ok(results);
    }
    
    /**
     * Get enriched movie metadata from TMDB.
     * Can search by title or use specific TMDB ID.
     * Optionally downloads and saves cover image.
     */
    @PostMapping("/enrich/movie")
    @Operation(summary = "Enrich movie metadata from TMDB",
               description = "Fetch detailed movie information from TMDB including genres, length, and cover image")
    public ResponseEntity<TmdbEnrichmentResponse> enrichMovie(
            @RequestBody TmdbEnrichmentRequest request) {
        log.info("Enriching movie metadata: {}", request);
        
        if (request.getTitle() == null && request.getTmdbId() == null) {
            return ResponseEntity.badRequest().build();
        }
        
        TmdbEnrichmentResponse response = tmdbEnrichmentService.enrichMovie(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get enriched TV series metadata from TMDB.
     * Can search by title or use specific TMDB ID.
     * Optionally downloads and saves cover image.
     */
    @PostMapping("/enrich/series")
    @Operation(summary = "Enrich series metadata from TMDB",
               description = "Fetch detailed TV series information from TMDB including genres, season count, and cover image")
    public ResponseEntity<TmdbEnrichmentResponse> enrichSeries(
            @RequestBody TmdbEnrichmentRequest request) {
        log.info("Enriching series metadata: {}", request);
        
        if (request.getTitle() == null && request.getTmdbId() == null) {
            return ResponseEntity.badRequest().build();
        }
        
        TmdbEnrichmentResponse response = tmdbEnrichmentService.enrichSeries(request);
        return ResponseEntity.ok(response);
    }
}
