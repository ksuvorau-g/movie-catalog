package com.moviecat.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TMDB Genre data structure.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmdbGenre {
    
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("name")
    private String name;
}
