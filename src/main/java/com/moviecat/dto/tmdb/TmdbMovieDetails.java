package com.moviecat.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Detailed movie information from TMDB API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmdbMovieDetails {
    
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("original_title")
    private String originalTitle;
    
    @JsonProperty("overview")
    private String overview;
    
    @JsonProperty("poster_path")
    private String posterPath;
    
    @JsonProperty("backdrop_path")
    private String backdropPath;
    
    @JsonProperty("runtime")
    private Integer runtime; // In minutes
    
    @JsonProperty("genres")
    @Builder.Default
    private List<TmdbGenre> genres = new ArrayList<>();
    
    @JsonProperty("release_date")
    private String releaseDate;
    
    @JsonProperty("vote_average")
    private Double voteAverage;
    
    @JsonProperty("vote_count")
    private Integer voteCount;
    
    @JsonProperty("imdb_id")
    private String imdbId;
    
    @JsonProperty("status")
    private String status;
}
