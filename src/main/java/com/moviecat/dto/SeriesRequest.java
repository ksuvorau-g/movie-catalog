package com.moviecat.dto;

import com.moviecat.model.Season;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating or updating a TV series.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeriesRequest {
    
    private String title;
    private String link;
    private String comment;
    private String coverImage;
    private List<String> genres;
    private List<Season> seasons;
    private String addedBy;
    private Integer priority;
}
