package com.moviecat.dto;

import com.moviecat.model.WatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for movie details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponse {
    
    private String id;
    private String title;
    private String link;
    private String linkDescription;
    private String comment;
    private String coverImage;
    private Integer length;
    private List<String> genres;
    private WatchStatus watchStatus;
    private String addedBy;
    private LocalDateTime dateAdded;
    private Integer priority;
}
