package com.moviecat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a TV series in the catalog.
 * Stored as a document in MongoDB 'series' collection.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "series")
public class Series {
    
    /**
     * Unique identifier (MongoDB ObjectId).
     */
    @Id
    private String id;
    
    /**
     * Series title (required field).
     */
    @Indexed
    private String title;
    
    /**
     * URL where the series can be accessed/watched (optional).
     */
    private String link;
    
    /**
     * User's personal notes, thoughts, or review (optional).
     */
    private String comment;
    
    /**
     * URL to series poster or thumbnail image (optional).
     */
    private String coverImage;
    
    /**
     * Series genres - can have multiple genres, free text (optional).
     */
    @Builder.Default
    private List<String> genres = new ArrayList<>();
    
    /**
     * Collection of seasons with their watch status.
     * Embedded documents.
     */
    @Builder.Default
    private List<Season> seasons = new ArrayList<>();
    
    /**
     * Overall series watch status (calculated automatically).
     * Watched if all seasons are watched, unwatched otherwise.
     */
    @Indexed
    @Builder.Default
    private WatchStatus seriesWatchStatus = WatchStatus.UNWATCHED;
    
    /**
     * Total number of seasons available (fetched from external source).
     */
    private Integer totalAvailableSeasons;
    
    /**
     * Flag indicating if new unwatched seasons are available.
     */
    @Indexed
    @Builder.Default
    private Boolean hasNewSeasons = false;
    
    /**
     * Series status: complete or ongoing (fetched from external source).
     */
    @Indexed
    private SeriesStatus seriesStatus;
    
    /**
     * Name of the person who added this series (optional).
     */
    @Indexed
    private String addedBy;
    
    /**
     * Date when the series was added to the catalog (automatic).
     */
    @Indexed
    @Builder.Default
    private LocalDateTime dateAdded = LocalDateTime.now();
    
    /**
     * Date when system last checked for new seasons (automatic).
     */
    private LocalDateTime lastSeasonCheck;
    
    /**
     * Manual priority for recommendations (optional).
     * Higher values indicate higher priority.
     * Can be increased/decreased manually through API.
     * Default is 0 (normal priority).
     */
    @Indexed
    @Builder.Default
    private Integer priority = 0;
    
    /**
     * TMDB (The Movie Database) ID for this series (optional).
     * Used for fetching additional metadata and updates.
     */
    private Integer tmdbId;
    
    /**
     * Calculate and update series watch status based on seasons.
     * Series is watched only if all seasons are watched.
     * Also resets hasNewSeasons flag to false when all seasons are watched.
     */
    public void updateSeriesWatchStatus() {
        if (seasons == null || seasons.isEmpty()) {
            this.seriesWatchStatus = WatchStatus.UNWATCHED;
            return;
        }
        
        boolean allWatched = seasons.stream()
                .allMatch(season -> season.getWatchStatus() == WatchStatus.WATCHED);
        
        this.seriesWatchStatus = allWatched ? WatchStatus.WATCHED : WatchStatus.UNWATCHED;
        
        // Reset hasNewSeasons flag when all seasons are watched
        if (allWatched) {
            this.hasNewSeasons = false;
        }
    }
}
