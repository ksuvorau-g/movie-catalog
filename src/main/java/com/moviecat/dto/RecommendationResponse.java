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
    private ContentType contentType;
    private String title;
    private String link;
    private String coverImage;
    private String comment;
    private Integer priority;
    private String addedBy;
    
    // Movie-specific
    private Integer length;
    
    // Series-specific
    private Boolean hasNewSeasons;
    private Integer totalAvailableSeasons;
}
