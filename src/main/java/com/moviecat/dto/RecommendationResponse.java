package com.moviecat.dto;

import com.moviecat.model.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for recommendations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    
    private String id;
    private String title;
    private String link;
    private String comment;
    private String coverImage;
    private Integer priority;
    private String addedBy;
    private ContentType contentType;

    // Movie-specific
    private Integer length;
    
    // Series-specific
    private Boolean hasNewSeasons;
    private Integer totalAvailableSeasons;
}
