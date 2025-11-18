package com.moviecat.service;

import com.moviecat.dto.tmdb.TmdbMovieDetails;
import com.moviecat.dto.tmdb.TmdbSearchResponse;
import com.moviecat.dto.tmdb.TmdbSearchResult;
import com.moviecat.dto.tmdb.TmdbSeriesDetails;
import com.moviecat.model.ContentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service for interacting with TMDB (The Movie Database) API.
 * Provides methods for searching and retrieving detailed information about movies and TV series.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TmdbApiService {
    
    private final WebClient tmdbWebClient;
    
    @Value("${tmdb.image.base-url}")
    private String tmdbImageBaseUrl;
    
    /**
     * Search for movies or TV series by title.
     * 
     * @param title search query
     * @param contentType MOVIE or SERIES
     * @return list of search results
     */
    public List<TmdbSearchResult> searchByTitle(String title, ContentType contentType) {
        log.info("Searching TMDB for {} with title: {}", contentType, title);
        
        String endpoint = contentType == ContentType.MOVIE ? "/search/movie" : "/search/tv";
        
        try {
            TmdbSearchResponse response = tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(endpoint)
                            .queryParam("query", title)
                            .queryParam("language", "en-US")
                            .queryParam("page", 1)
                            .build())
                    .retrieve()
                    .bodyToMono(TmdbSearchResponse.class)
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        log.error("TMDB API error during search: {} - {}", ex.getStatusCode(), ex.getMessage());
                        return Mono.empty();
                    })
                    .block();
            
            if (response == null || response.getResults() == null) {
                log.warn("No results found for title: {}", title);
                return List.of();
            }
            
            log.info("Found {} results for title: {}", response.getResults().size(), title);
            return response.getResults();
            
        } catch (Exception ex) {
            log.error("Error searching TMDB for title: {}", title, ex);
            throw new RuntimeException("Failed to search TMDB: " + ex.getMessage(), ex);
        }
    }
    
    /**
     * Get detailed movie information by TMDB ID.
     * 
     * @param tmdbId TMDB movie ID
     * @return movie details
     */
    public TmdbMovieDetails getMovieDetails(Integer tmdbId) {
        log.info("Fetching movie details from TMDB for ID: {}", tmdbId);
        
        try {
            TmdbMovieDetails details = tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/movie/{id}")
                            .queryParam("language", "en-US")
                            .build(tmdbId))
                    .retrieve()
                    .bodyToMono(TmdbMovieDetails.class)
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        log.error("TMDB API error fetching movie {}: {} - {}", 
                            tmdbId, ex.getStatusCode(), ex.getMessage());
                        return Mono.empty();
                    })
                    .block();
            
            if (details == null) {
                throw new RuntimeException("Movie not found with TMDB ID: " + tmdbId);
            }
            
            log.info("Successfully fetched movie details: {}", details.getTitle());
            return details;
            
        } catch (Exception ex) {
            log.error("Error fetching movie details for TMDB ID: {}", tmdbId, ex);
            throw new RuntimeException("Failed to fetch movie details: " + ex.getMessage(), ex);
        }
    }
    
    /**
     * Get detailed TV series information by TMDB ID.
     * 
     * @param tmdbId TMDB series ID
     * @return series details
     */
    public TmdbSeriesDetails getSeriesDetails(Integer tmdbId) {
        log.info("Fetching series details from TMDB for ID: {}", tmdbId);
        
        try {
            TmdbSeriesDetails details = tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/tv/{id}")
                            .queryParam("language", "en-US")
                            .build(tmdbId))
                    .retrieve()
                    .bodyToMono(TmdbSeriesDetails.class)
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        log.error("TMDB API error fetching series {}: {} - {}", 
                            tmdbId, ex.getStatusCode(), ex.getMessage());
                        return Mono.empty();
                    })
                    .block();
            
            if (details == null) {
                throw new RuntimeException("Series not found with TMDB ID: " + tmdbId);
            }
            
            log.info("Successfully fetched series details: {}", details.getName());
            return details;
            
        } catch (Exception ex) {
            log.error("Error fetching series details for TMDB ID: {}", tmdbId, ex);
            throw new RuntimeException("Failed to fetch series details: " + ex.getMessage(), ex);
        }
    }
    
    /**
     * Build full image URL from TMDB poster path.
     * 
     * @param posterPath poster path from TMDB (e.g., "/abc123.jpg")
     * @return full image URL or null if posterPath is null
     */
    public String buildImageUrl(String posterPath) {
        if (posterPath == null || posterPath.isEmpty()) {
            return null;
        }
        return tmdbImageBaseUrl + posterPath;
    }
    
    /**
     * Search for content by title and return the best match (first result).
     * Useful for auto-enrichment when you want a single result.
     * 
     * @param title search query
     * @param contentType MOVIE or SERIES
     * @return first search result or null if no results
     */
    public TmdbSearchResult searchBestMatch(String title, ContentType contentType) {
        List<TmdbSearchResult> results = searchByTitle(title, contentType);
        
        if (results.isEmpty()) {
            log.warn("No TMDB results found for: {}", title);
            return null;
        }
        
        TmdbSearchResult bestMatch = results.get(0);
        log.info("Best match for '{}': {}", title, bestMatch.getDisplayTitle());
        return bestMatch;
    }
}
