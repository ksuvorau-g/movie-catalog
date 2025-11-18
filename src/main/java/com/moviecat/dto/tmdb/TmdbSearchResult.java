package com.moviecat.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Single search result item from TMDB API.
 * Used for both movie and TV series search results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmdbSearchResult {
    
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("title")
    private String title; // For movies
    
    @JsonProperty("name")
    private String name; // For TV series
    
    @JsonProperty("original_title")
    private String originalTitle; // For movies
    
    @JsonProperty("original_name")
    private String originalName; // For TV series
    
    @JsonProperty("overview")
    private String overview;
    
    @JsonProperty("poster_path")
    private String posterPath;
    
    @JsonProperty("release_date")
    private String releaseDate; // For movies
    
    @JsonProperty("first_air_date")
    private String firstAirDate; // For TV series
    
    @JsonProperty("media_type")
    private String mediaType; // "movie" or "tv"
    
    /**
     * Get display title (handles both movies and series).
     */
    public String getDisplayTitle() {
        return title != null ? title : name;
    }
}
