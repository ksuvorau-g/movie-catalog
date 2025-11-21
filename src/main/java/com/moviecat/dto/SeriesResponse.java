package com.moviecat.dto;

import com.moviecat.model.Season;
import com.moviecat.model.SeriesStatus;
import com.moviecat.model.WatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for TV series details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeriesResponse {
    
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

    private List<Season> seasons;
    private Boolean hasNewSeasons;
    private SeriesStatus seriesStatus;
    private Integer totalAvailableSeasons;
    private LocalDateTime lastSeasonCheck;
}
