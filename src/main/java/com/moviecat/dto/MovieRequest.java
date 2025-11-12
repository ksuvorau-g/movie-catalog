package com.moviecat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating or updating a movie.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieRequest {
    
    private String title;
    private String link;
    private String linkDescription;
    private String comment;
    private String coverImage;
    private Integer length;
    private List<String> genres;
    private String addedBy;
    private Integer priority;
}
