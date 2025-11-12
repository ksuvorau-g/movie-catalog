package com.moviecat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded document representing a season of a TV series.
 * Not a separate collection - embedded within Series documents.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Season {
    
    /**
     * Season number (e.g., 1, 2, 3).
     */
    private Integer seasonNumber;
    
    /**
     * Watch status of this season.
     */
    @Builder.Default
    private WatchStatus watchStatus = WatchStatus.UNWATCHED;
}
