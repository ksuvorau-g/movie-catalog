package com.moviecat.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Detailed TV series information from TMDB API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmdbSeriesDetails {
    
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("original_name")
    private String originalName;
    
    @JsonProperty("overview")
    private String overview;
    
    @JsonProperty("poster_path")
    private String posterPath;
    
    @JsonProperty("backdrop_path")
    private String backdropPath;
    
    @JsonProperty("genres")
    @Builder.Default
    private List<TmdbGenre> genres = new ArrayList<>();
    
    @JsonProperty("first_air_date")
    private String firstAirDate;
    
    @JsonProperty("last_air_date")
    private String lastAirDate;
    
    @JsonProperty("number_of_seasons")
    private Integer numberOfSeasons;
    
    @JsonProperty("number_of_episodes")
    private Integer numberOfEpisodes;
    
    @JsonProperty("episode_run_time")
    @Builder.Default
    private List<Integer> episodeRunTime = new ArrayList<>();
    
    @JsonProperty("status")
    private String status; // "Returning Series", "Ended", "Canceled"
    
    @JsonProperty("vote_average")
    private Double voteAverage;
    
    @JsonProperty("vote_count")
    private Integer voteCount;
    
    @JsonProperty("in_production")
    private Boolean inProduction;
}
