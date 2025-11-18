package com.moviecat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Response containing enriched metadata from TMDB.
 * Used for both movie and series enrichment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmdbEnrichmentResponse {
    
    /**
     * TMDB ID for this content.
     */
    private Integer tmdbId;
    
    /**
     * Title/Name from TMDB.
     */
    private String title;
    
    /**
     * Runtime/length in minutes (for movies) or average episode length (for series).
     */
    private Integer length;
    
    /**
     * List of genre names.
     */
    @Builder.Default
    private List<String> genres = new ArrayList<>();
    
    /**
     * Full URL to poster image from TMDB.
     */
    private String posterUrl;
    
    /**
     * Image ID if downloaded and saved locally (null if not downloaded).
     */
    private String savedImageId;
    
    /**
     * Total number of seasons (series only).
     */
    private Integer totalSeasons;
    
    /**
     * Series status: "Returning Series", "Ended", "Canceled" (series only).
     */
    private String status;
    
    /**
     * Overview/description from TMDB.
     */
    private String overview;
    
    /**
     * IMDB ID if available (movies only).
     */
    private String imdbId;
}
