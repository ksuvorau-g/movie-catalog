package com.moviecat.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Response from TMDB search API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmdbSearchResponse {
    
    @JsonProperty("page")
    private Integer page;
    
    @JsonProperty("results")
    @Builder.Default
    private List<TmdbSearchResult> results = new ArrayList<>();
    
    @JsonProperty("total_results")
    private Integer totalResults;
    
    @JsonProperty("total_pages")
    private Integer totalPages;
}
