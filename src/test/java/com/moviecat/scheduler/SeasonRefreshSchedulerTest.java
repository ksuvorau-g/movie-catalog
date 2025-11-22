package com.moviecat.scheduler;

import com.moviecat.dto.BulkRefreshResponse;
import com.moviecat.service.SeriesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Unit tests for SeasonRefreshScheduler.
 * 
 * Tests the scheduled task that automatically refreshes TV series season data.
 */
@ExtendWith(MockitoExtension.class)
class SeasonRefreshSchedulerTest {

    @Mock
    private SeriesService seriesService;

    @InjectMocks
    private SeasonRefreshScheduler scheduler;

    private BulkRefreshResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = BulkRefreshResponse.builder()
                .totalProcessed(10)
                .successCount(8)
                .failureCount(2)
                .updatedCount(5)
                .build();
    }

    @Test
    void refreshAllSeriesSeasons_shouldCallServiceAndLogSuccess() {
        // Given
        when(seriesService.refreshAllSeriesWithTmdbId()).thenReturn(mockResponse);

        // When
        scheduler.refreshAllSeriesSeasons();

        // Then
        verify(seriesService, times(1)).refreshAllSeriesWithTmdbId();
    }

    @Test
    void refreshAllSeriesSeasons_shouldHandleExceptionGracefully() {
        // Given
        when(seriesService.refreshAllSeriesWithTmdbId())
                .thenThrow(new RuntimeException("TMDB API unavailable"));

        // When
        scheduler.refreshAllSeriesSeasons();

        // Then
        verify(seriesService, times(1)).refreshAllSeriesWithTmdbId();
        // Exception should be caught and logged, not propagated
    }

    @Test
    void refreshAllSeriesSeasons_shouldHandleEmptyResponse() {
        // Given
        BulkRefreshResponse emptyResponse = BulkRefreshResponse.builder()
                .totalProcessed(0)
                .successCount(0)
                .failureCount(0)
                .updatedCount(0)
                .build();
        when(seriesService.refreshAllSeriesWithTmdbId()).thenReturn(emptyResponse);

        // When
        scheduler.refreshAllSeriesSeasons();

        // Then
        verify(seriesService, times(1)).refreshAllSeriesWithTmdbId();
    }
}
