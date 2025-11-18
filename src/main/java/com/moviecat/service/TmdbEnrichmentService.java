package com.moviecat.service;

import com.moviecat.dto.ImageDownloadRequest;
import com.moviecat.dto.ImageResponse;
import com.moviecat.dto.TmdbEnrichmentRequest;
import com.moviecat.dto.TmdbEnrichmentResponse;
import com.moviecat.dto.tmdb.TmdbGenre;
import com.moviecat.dto.tmdb.TmdbMovieDetails;
import com.moviecat.dto.tmdb.TmdbSearchResult;
import com.moviecat.dto.tmdb.TmdbSeriesDetails;
import com.moviecat.model.ContentType;
import com.moviecat.model.SeriesStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for enriching movie and series metadata using TMDB API.
 * Handles searching, fetching details, and downloading cover images.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TmdbEnrichmentService {
    
    private final TmdbApiService tmdbApiService;
    private final ImageService imageService;
    
    /**
     * Search for movies in TMDB.
     * Converts poster paths to full URLs.
     */
    public List<TmdbSearchResult> searchMovies(String title) {
        List<TmdbSearchResult> results = tmdbApiService.searchByTitle(title, ContentType.MOVIE);
        // Convert poster paths to full URLs
        results.forEach(result -> {
            if (result.getPosterPath() != null) {
                result.setPosterPath(tmdbApiService.buildImageUrl(result.getPosterPath()));
            }
        });
        return results;
    }
    
    /**
     * Search for TV series in TMDB.
     * Converts poster paths to full URLs.
     */
    public List<TmdbSearchResult> searchSeries(String title) {
        List<TmdbSearchResult> results = tmdbApiService.searchByTitle(title, ContentType.SERIES);
        // Convert poster paths to full URLs
        results.forEach(result -> {
            if (result.getPosterPath() != null) {
                result.setPosterPath(tmdbApiService.buildImageUrl(result.getPosterPath()));
            }
        });
        return results;
    }
    
    /**
     * Enrich movie metadata from TMDB.
     * If tmdbId is provided, fetches details directly.
     * If only title is provided, searches and uses best match.
     */
    public TmdbEnrichmentResponse enrichMovie(TmdbEnrichmentRequest request) {
        log.info("Enriching movie: tmdbId={}, title={}", request.getTmdbId(), request.getTitle());
        
        Integer tmdbId = request.getTmdbId();
        
        // If no tmdbId, search by title to get it
        if (tmdbId == null) {
            TmdbSearchResult searchResult = tmdbApiService.searchBestMatch(
                request.getTitle(), 
                ContentType.MOVIE
            );
            
            if (searchResult == null) {
                throw new RuntimeException("No TMDB results found for movie: " + request.getTitle());
            }
            
            tmdbId = searchResult.getId();
            log.info("Found TMDB ID {} for movie: {}", tmdbId, request.getTitle());
        }
        
        // Fetch detailed movie information
        TmdbMovieDetails details = tmdbApiService.getMovieDetails(tmdbId);
        
        // Build enrichment response
        TmdbEnrichmentResponse response = TmdbEnrichmentResponse.builder()
                .tmdbId(details.getId())
                .title(details.getTitle())
                .length(details.getRuntime())
                .genres(extractGenreNames(details.getGenres()))
                .posterUrl(tmdbApiService.buildImageUrl(details.getPosterPath()))
                .overview(details.getOverview())
                .imdbId(details.getImdbId())
                .build();
        
        // Download and save image if requested
        if (request.getDownloadImage() && details.getPosterPath() != null) {
            String imageUrl = tmdbApiService.buildImageUrl(details.getPosterPath());
            try {
                ImageDownloadRequest imageRequest = ImageDownloadRequest.builder()
                        .imageUrl(imageUrl)
                        .build();
                ImageResponse imageResponse = imageService.downloadAndSaveImage(imageRequest);
                response.setSavedImageId(imageResponse.getId());
                log.info("Downloaded and saved cover image: {}", imageResponse.getId());
            } catch (Exception ex) {
                log.error("Failed to download cover image from TMDB", ex);
                // Continue without image - not critical
            }
        }
        
        log.info("Successfully enriched movie: {}", details.getTitle());
        return response;
    }
    
    /**
     * Enrich TV series metadata from TMDB.
     * If tmdbId is provided, fetches details directly.
     * If only title is provided, searches and uses best match.
     */
    public TmdbEnrichmentResponse enrichSeries(TmdbEnrichmentRequest request) {
        log.info("Enriching series: tmdbId={}, title={}", request.getTmdbId(), request.getTitle());
        
        Integer tmdbId = request.getTmdbId();
        
        // If no tmdbId, search by title to get it
        if (tmdbId == null) {
            TmdbSearchResult searchResult = tmdbApiService.searchBestMatch(
                request.getTitle(), 
                ContentType.SERIES
            );
            
            if (searchResult == null) {
                throw new RuntimeException("No TMDB results found for series: " + request.getTitle());
            }
            
            tmdbId = searchResult.getId();
            log.info("Found TMDB ID {} for series: {}", tmdbId, request.getTitle());
        }
        
        // Fetch detailed series information
        TmdbSeriesDetails details = tmdbApiService.getSeriesDetails(tmdbId);
        
        // Calculate average episode length if available
        Integer avgEpisodeLength = null;
        if (details.getEpisodeRunTime() != null && !details.getEpisodeRunTime().isEmpty()) {
            avgEpisodeLength = (int) details.getEpisodeRunTime().stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0);
        }
        
        // Build enrichment response
        TmdbEnrichmentResponse response = TmdbEnrichmentResponse.builder()
                .tmdbId(details.getId())
                .title(details.getName())
                .length(avgEpisodeLength)
                .genres(extractGenreNames(details.getGenres()))
                .posterUrl(tmdbApiService.buildImageUrl(details.getPosterPath()))
                .totalSeasons(details.getNumberOfSeasons())
                .status(details.getStatus())
                .overview(details.getOverview())
                .build();
        
        // Download and save image if requested
        if (request.getDownloadImage() && details.getPosterPath() != null) {
            String imageUrl = tmdbApiService.buildImageUrl(details.getPosterPath());
            try {
                ImageDownloadRequest imageRequest = ImageDownloadRequest.builder()
                        .imageUrl(imageUrl)
                        .build();
                ImageResponse imageResponse = imageService.downloadAndSaveImage(imageRequest);
                response.setSavedImageId(imageResponse.getId());
                log.info("Downloaded and saved cover image: {}", imageResponse.getId());
            } catch (Exception ex) {
                log.error("Failed to download cover image from TMDB", ex);
                // Continue without image - not critical
            }
        }
        
        log.info("Successfully enriched series: {}", details.getName());
        return response;
    }
    
    /**
     * Extract genre names from TMDB genre objects.
     */
    private List<String> extractGenreNames(List<TmdbGenre> genres) {
        if (genres == null) {
            return List.of();
        }
        return genres.stream()
                .map(TmdbGenre::getName)
                .collect(Collectors.toList());
    }
    
    /**
     * Map TMDB series status to our SeriesStatus enum.
     */
    public SeriesStatus mapSeriesStatus(String tmdbStatus) {
        if (tmdbStatus == null) {
            return null;
        }
        
        return switch (tmdbStatus.toLowerCase()) {
            case "ended", "canceled", "cancelled" -> SeriesStatus.COMPLETE;
            case "returning series", "in production", "planned" -> SeriesStatus.ONGOING;
            default -> null;
        };
    }
}
