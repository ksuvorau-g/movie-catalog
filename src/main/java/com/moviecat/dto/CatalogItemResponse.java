package com.moviecat.dto;

import com.moviecat.model.ContentType;
import com.moviecat.model.Season;
import com.moviecat.model.WatchStatus;
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
    private String title;
    private String link;
    private String comment;
    private String coverImage;
    private List<String> genres;
    private WatchStatus watchStatus;
    private String addedBy;
    private LocalDateTime dateAdded;
    private Integer priority;
    private Integer tmdbId;

    private ContentType contentType;

    // Movie-specific
    private Integer length;
    
    // Series-specific
    private List<Season> seasons;
    private Boolean hasNewSeasons;
    private String seriesStatus;
    private Integer totalAvailableSeasons;
}
