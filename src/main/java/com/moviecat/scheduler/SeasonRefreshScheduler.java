package com.moviecat.scheduler;

import com.moviecat.dto.BulkRefreshResponse;
import com.moviecat.service.SeriesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for automatic weekly refresh of TV series season data.
 * 
 * This scheduler runs every Monday at midnight (configurable via scheduler.cron.season-check property)
 * and refreshes season information for all series that have a TMDB ID configured.
 * 
 * The refresh process:
 * - Fetches latest season data from TMDB API
 * - Updates season lists and episode counts
 * - Sets hasNewSeasons flag if new seasons detected
 * - Generates notifications for series with watched content and new seasons
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeasonRefreshScheduler {

    private final SeriesService seriesService;

    /**
     * Scheduled task to refresh all series with TMDB ID.
     * Runs based on cron expression defined in application.properties (scheduler.cron.season-check).
     * Default: Every Monday at 00:00:00 (0 0 0 * * MON)
     */
    @Scheduled(cron = "${scheduler.cron.season-check}")
    public void refreshAllSeriesSeasons() {
        log.info("Starting scheduled weekly series refresh");
        
        try {
            BulkRefreshResponse response = seriesService.refreshAllSeriesWithTmdbId();
            
            log.info("Scheduled series refresh completed successfully. " +
                    "Total: {}, Success: {}, Failed: {}, Updated: {}",
                    response.getTotalProcessed(),
                    response.getSuccessCount(),
                    response.getFailureCount(),
                    response.getUpdatedCount());
            
        } catch (Exception e) {
            log.error("Scheduled series refresh failed with error: {}", e.getMessage(), e);
        }
    }
}
