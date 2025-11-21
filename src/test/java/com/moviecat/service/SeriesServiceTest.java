package com.moviecat.service;

import com.moviecat.dto.SeriesResponse;
import com.moviecat.dto.tmdb.TmdbSeriesDetails;
import com.moviecat.model.Season;
import com.moviecat.model.Series;
import com.moviecat.model.SeriesStatus;
import com.moviecat.model.WatchStatus;
import com.moviecat.repository.SeriesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeriesServiceTest {

    @Mock
    private SeriesRepository seriesRepository;

    @Mock
    private TmdbApiService tmdbApiService;

    private SeriesService seriesService;

    @BeforeEach
    void setUp() {
        seriesService = new SeriesService(seriesRepository, tmdbApiService);
    }

    @Test
    void refreshSeasonsAddsMissingSeasonsAndFlagsNewContent() {
        Series series = Series.builder()
                .id("series-1")
                .tmdbId(12345)
                .seasons(new ArrayList<>(List.of(
                        Season.builder().seasonNumber(1).watchStatus(WatchStatus.WATCHED).build(),
                        Season.builder().seasonNumber(2).watchStatus(WatchStatus.UNWATCHED).build()
                )))
                .lastSeasonCheck(LocalDateTime.of(2024, 1, 1, 0, 0))
                .build();

        when(seriesRepository.findById("series-1")).thenReturn(Optional.of(series));
        when(tmdbApiService.getSeriesDetails(12345))
                .thenReturn(TmdbSeriesDetails.builder().numberOfSeasons(4).status("Returning Series").build());
        when(seriesRepository.save(any(Series.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SeriesResponse response = seriesService.refreshSeasons("series-1");

        assertEquals(4, response.getSeasons().size(), "Should align seasons with TMDB count");
        assertTrue(response.getSeasons().stream().anyMatch(season -> season.getSeasonNumber() == 3),
                "Missing seasons must be added");
        assertTrue(response.getSeasons().stream().anyMatch(season -> season.getSeasonNumber() == 4),
                "All TMDB seasons must be represented");
        assertTrue(response.getHasNewSeasons(), "New seasons flag should be raised when TMDB has more seasons");
        assertEquals(Integer.valueOf(4), response.getTotalAvailableSeasons());
        assertEquals(SeriesStatus.ONGOING, response.getSeriesStatus());
    }

    @Test
    void refreshSeasonsRemovesExtraTrackedSeasons() {
        Series series = Series.builder()
                .id("series-3")
                .tmdbId(9876)
                .seasons(new ArrayList<>(List.of(
                        Season.builder().seasonNumber(1).watchStatus(WatchStatus.WATCHED).build(),
                        Season.builder().seasonNumber(2).watchStatus(WatchStatus.WATCHED).build(),
                        Season.builder().seasonNumber(3).watchStatus(WatchStatus.UNWATCHED).build()
                )))
                .build();

        when(seriesRepository.findById("series-3")).thenReturn(Optional.of(series));
        when(tmdbApiService.getSeriesDetails(9876))
                .thenReturn(TmdbSeriesDetails.builder().numberOfSeasons(2).status("Ended").build());
        when(seriesRepository.save(any(Series.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SeriesResponse response = seriesService.refreshSeasons("series-3");

        assertEquals(2, response.getSeasons().size(), "Tracked seasons beyond TMDB count should be removed");
        assertFalse(response.getSeasons().stream().anyMatch(season -> season.getSeasonNumber() == 3));
        assertEquals(SeriesStatus.COMPLETE, response.getSeriesStatus());
        assertFalse(response.getHasNewSeasons());
    }

    @Test
    void refreshSeasonsThrowsWhenTmdbReferenceMissing() {
        Series series = Series.builder()
                .id("series-2")
                .seasons(new ArrayList<>(List.of(
                        Season.builder().seasonNumber(1).watchStatus(WatchStatus.UNWATCHED).build()
                )))
                .build();

        when(seriesRepository.findById("series-2")).thenReturn(Optional.of(series));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> seriesService.refreshSeasons("series-2"));

        assertTrue(exception.getMessage().contains("TMDB"));
    }
}
