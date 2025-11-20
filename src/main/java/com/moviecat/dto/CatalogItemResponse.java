package com.moviecat.dto;

import com.moviecat.model.ContentType;
import com.moviecat.model.Season;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Unified response DTO for catalog items (both movies and series).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogItemResponse {
    
    private String id;
    private ContentType contentType;
    private String title;
    private String link;
    private String coverImage;
    private String comment;
    private List<String> genres;
    private String watchStatus;
    private String addedBy;
    private LocalDateTime dateAdded;
    private Integer priority;
    private Integer tmdbId;
    
    // Movie-specific
    private Integer length;
    
    // Series-specific
    private List<Season> seasons;
    private Boolean hasNewSeasons;
    private String seriesStatus;
    private Integer totalAvailableSeasons;
}
