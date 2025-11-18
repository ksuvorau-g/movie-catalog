package com.moviecat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to enrich movie/series metadata from TMDB.
 * Can search by title or use a specific TMDB ID.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmdbEnrichmentRequest {
    
    /**
     * Title to search for in TMDB (optional if tmdbId is provided).
     */
    private String title;
    
    /**
     * Specific TMDB ID to fetch (optional if title is provided).
     */
    private Integer tmdbId;
    
    /**
     * Whether to download and save the cover image locally.
     * Default: true
     */
    @Builder.Default
    private Boolean downloadImage = true;
}
