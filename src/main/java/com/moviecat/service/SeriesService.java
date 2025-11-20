package com.moviecat.service;

import com.moviecat.dto.SeriesRequest;
import com.moviecat.dto.SeriesResponse;
import com.moviecat.dto.tmdb.TmdbSeriesDetails;
import com.moviecat.model.Season;
import com.moviecat.model.Series;
import com.moviecat.model.SeriesStatus;
import com.moviecat.model.WatchStatus;
import com.moviecat.repository.SeriesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for TV series-related operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SeriesService {
    
    private static final Pattern TMDB_TV_ID_PATTERN = Pattern.compile("/tv/(\\d+)");

    private final SeriesRepository seriesRepository;
    private final TmdbApiService tmdbApiService;
    
    /**
     * Add a new TV series to the catalog.
     * Checks for duplicates and logs a warning if found.
     * 
     * @param request series details
     * @return created series
     */
    public SeriesResponse addSeries(SeriesRequest request) {
        log.info("Adding new series: {}", request.getTitle());
        
        // Check for duplicates
        List<Series> existingSeries = seriesRepository.findByTitleIgnoreCase(request.getTitle());
        if (!existingSeries.isEmpty()) {
            log.warn("Series with title '{}' already exists in the catalog", request.getTitle());
        }
        
        // Use provided seasons or create default season 1
        List<Season> seasons = request.getSeasons() != null ? request.getSeasons() : new ArrayList<>();
        
        // Business rule: Series must have at least one season
        if (seasons.isEmpty()) {
            log.info("No seasons provided for series '{}', creating default season 1 in UNWATCHED status", request.getTitle());
            Season defaultSeason = Season.builder()
                    .seasonNumber(1)
                    .watchStatus(WatchStatus.UNWATCHED)
                    .build();
            seasons.add(defaultSeason);
        }
        
        Series series = Series.builder()
                .title(request.getTitle())
                .link(request.getLink())
                .comment(request.getComment())
                .coverImage(request.getCoverImage())
                .genres(request.getGenres())
                .seasons(seasons)
                .seriesWatchStatus(WatchStatus.UNWATCHED)
                .hasNewSeasons(false)
                .addedBy(request.getAddedBy())
                .dateAdded(LocalDateTime.now())
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .build();
        
        // Calculate initial watch status based on seasons
        series.updateSeriesWatchStatus();
        
        Series savedSeries = seriesRepository.save(series);
        log.info("Series added successfully with id: {}", savedSeries.getId());
        
        return toResponse(savedSeries);
    }
    
    /**
     * Get series by ID.
     * 
     * @param id series ID
     * @return series details
     * @throws RuntimeException if series not found
     */
    public SeriesResponse getSeriesById(String id) {
        log.info("Getting series by id: {}", id);
        
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Series not found with id: " + id));
        
        return toResponse(series);
    }
    
    /**
     * Get all series.
     * 
     * @return list of all series
     */
    public List<SeriesResponse> getAllSeries() {
        log.info("Getting all series");
        
        List<Series> seriesList = seriesRepository.findAll();
        log.info("Found {} series", seriesList.size());
        
        return seriesList.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Update series information.
     * 
     * @param id series ID
     * @param request updated series details
     * @return updated series
     * @throws RuntimeException if series not found
     */
    public SeriesResponse updateSeries(String id, SeriesRequest request) {
        log.info("Updating series: {}", id);
        
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Series not found with id: " + id));
        
        // Update fields
        series.setTitle(request.getTitle());
        series.setLink(request.getLink());
        series.setComment(request.getComment());
        series.setCoverImage(request.getCoverImage());
        series.setGenres(request.getGenres());
        series.setAddedBy(request.getAddedBy());
        if (request.getPriority() != null) {
            series.setPriority(request.getPriority());
        }
        if (request.getSeasons() != null) {
            // Business rule: Series must have at least one season
            if (request.getSeasons().isEmpty()) {
                throw new IllegalArgumentException("Cannot update series with empty seasons list. Series must have at least one season.");
            }
            series.setSeasons(request.getSeasons());
            // Recalculate watch status based on updated seasons
            series.updateSeriesWatchStatus();
        }
        
        Series updatedSeries = seriesRepository.save(series);
        log.info("Series updated successfully: {}", id);
        
        return toResponse(updatedSeries);
    }
    
    /**
     * Delete series from catalog.
     * 
     * @param id series ID
     * @throws RuntimeException if series not found
     */
    public void deleteSeries(String id) {
        log.info("Deleting series: {}", id);
        
        if (!seriesRepository.existsById(id)) {
            throw new RuntimeException("Series not found with id: " + id);
        }
        
        seriesRepository.deleteById(id);
        log.info("Series deleted successfully: {}", id);
    }
    
    /**
     * Update watch status for a specific season.
     * Creates the season if it doesn't exist.
     * 
     * @param id series ID
     * @param seasonNumber season number
     * @param watchStatus new watch status
     * @return updated series
     * @throws RuntimeException if series not found
     */
    public SeriesResponse updateSeasonWatchStatus(String id, Integer seasonNumber, WatchStatus watchStatus) {
        log.info("Updating watch status for series {} season {}: {}", id, seasonNumber, watchStatus);
        
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Series not found with id: " + id));
        
        // Find or create season
        Season season = series.getSeasons().stream()
                .filter(s -> s.getSeasonNumber().equals(seasonNumber))
                .findFirst()
                .orElseGet(() -> {
                    Season newSeason = Season.builder()
                            .seasonNumber(seasonNumber)
                            .watchStatus(WatchStatus.UNWATCHED)
                            .build();
                    series.getSeasons().add(newSeason);
                    return newSeason;
                });
        
        season.setWatchStatus(watchStatus);
        
        // Update series watch status based on all seasons
        series.updateSeriesWatchStatus();
        
        Series updatedSeries = seriesRepository.save(series);
        log.info("Season watch status updated successfully for series: {}", id);
        
        return toResponse(updatedSeries);
    }
    
    /**
     * Update watch status for entire series (all seasons).
     * Marks all existing seasons with the given watch status.
     * 
     * @param id series ID
     * @param watchStatus new watch status
     * @return updated series
     * @throws RuntimeException if series not found
     */
    public SeriesResponse updateSeriesWatchStatus(String id, WatchStatus watchStatus) {
        log.info("Updating watch status for entire series {}: {}", id, watchStatus);
        
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Series not found with id: " + id));
        
        // Update all seasons
        series.getSeasons().forEach(season -> season.setWatchStatus(watchStatus));
        
        // Update series watch status
        series.updateSeriesWatchStatus();
        
        Series updatedSeries = seriesRepository.save(series);
        log.info("Series watch status updated successfully: {}", id);
        
        return toResponse(updatedSeries);
    }
    
    /**
     * Append a new season in UNWATCHED status with the next sequential season number.
     *
     * @param id series ID
     * @return updated series
     */
    public SeriesResponse addSeason(String id) {
        log.info("Adding new season for series {}", id);
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Series not found with id: " + id));
        
        List<Season> seasons = series.getSeasons();
        if (seasons == null) {
            seasons = new ArrayList<>();
            series.setSeasons(seasons);
        }
        int nextSeasonNumber = seasons.stream()
                .map(Season::getSeasonNumber)
                .filter(num -> num != null)
                .max(Integer::compareTo)
                .orElse(0) + 1;
        Season newSeason = Season.builder()
                .seasonNumber(nextSeasonNumber)
                .watchStatus(WatchStatus.UNWATCHED)
                .build();
        seasons.add(newSeason);
        series.updateSeriesWatchStatus();
        Series saved = seriesRepository.save(series);
        log.info("Added season {} for series {}", nextSeasonNumber, id);
        return toResponse(saved);
    }
    
    /**
     * Remove the highest-numbered season if more than one season exists.
     *
     * @param id series ID
     * @return updated series
     */
    public SeriesResponse removeLastSeason(String id) {
        log.info("Removing last season for series {}", id);
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Series not found with id: " + id));
        List<Season> seasons = series.getSeasons();
        if (seasons == null || seasons.isEmpty()) {
            throw new IllegalStateException("Series does not have any seasons to remove");
        }
        if (seasons.size() == 1) {
            throw new IllegalStateException("Series must contain at least one season");
        }
        Season lastSeason = seasons.stream()
                .max(Comparator.comparingInt(season -> season.getSeasonNumber() != null ? season.getSeasonNumber() : 0))
                .orElseThrow(() -> new IllegalStateException("Unable to determine last season"));
        seasons.remove(lastSeason);
        series.updateSeriesWatchStatus();
        Series saved = seriesRepository.save(series);
        log.info("Removed season {} for series {}", lastSeason.getSeasonNumber(), id);
        return toResponse(saved);
    }
    
    /**
     * Update series priority.
     * 
     * @param id series ID
     * @param priority new priority value
     * @return updated series
     * @throws RuntimeException if series not found
     */
    public SeriesResponse updatePriority(String id, Integer priority) {
        log.info("Updating priority for series {}: {}", id, priority);
        
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Series not found with id: " + id));
        
        series.setPriority(priority);
        Series updatedSeries = seriesRepository.save(series);
        
        log.info("Priority updated successfully for series: {}", id);
        return toResponse(updatedSeries);
    }
    
    /**
     * Manually trigger season refresh for a series.
     * This is a placeholder - actual implementation would call external API service.
     * 
     * @param id series ID
     * @return updated series
     * @throws RuntimeException if series not found
     */
    public SeriesResponse refreshSeasons(String id) {
        log.info("Manually refreshing seasons for series: {}", id);
        
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Series not found with id: " + id));
        
        Integer tmdbId = resolveTmdbId(series);
        if (tmdbId == null) {
            throw new RuntimeException("Series requires a valid TMDB link or ID to refresh seasons");
        }

        TmdbSeriesDetails details = tmdbApiService.getSeriesDetails(tmdbId);
        int targetSeasonCount = normalizeSeasonCount(details.getNumberOfSeasons());

        List<Season> seasons = ensureSeasonList(series);
        int previousMaxSeason = seasons.stream()
                .map(Season::getSeasonNumber)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);

        boolean removedSeasons = seasons.removeIf(season ->
                season.getSeasonNumber() != null && season.getSeasonNumber() > targetSeasonCount);

        Set<Integer> existingNumbers = seasons.stream()
                .map(Season::getSeasonNumber)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        boolean addedSeasons = false;
        for (int seasonNumber = 1; seasonNumber <= targetSeasonCount; seasonNumber++) {
            if (!existingNumbers.contains(seasonNumber)) {
                seasons.add(Season.builder()
                        .seasonNumber(seasonNumber)
                        .watchStatus(WatchStatus.UNWATCHED)
                        .build());
                addedSeasons = true;
            }
        }

        seasons.sort(Comparator.comparing(Season::getSeasonNumber, Comparator.nullsLast(Integer::compareTo)));

        boolean newSeasonsDetected = targetSeasonCount > previousMaxSeason;
        series.setHasNewSeasons(newSeasonsDetected);
        series.setTotalAvailableSeasons(targetSeasonCount);
        series.setSeriesStatus(mapSeriesStatus(details.getStatus()));
        series.setLastSeasonCheck(LocalDateTime.now());
        if (series.getTmdbId() == null) {
            series.setTmdbId(tmdbId);
        }
        series.updateSeriesWatchStatus();
        Series updatedSeries = seriesRepository.save(series);

        log.info("Season refresh completed for series: {}. Added missing seasons: {}, removed extras: {}",
                id, addedSeasons, removedSeasons);
        return toResponse(updatedSeries);
    }

    private List<Season> ensureSeasonList(Series series) {
        if (series.getSeasons() == null) {
            series.setSeasons(new ArrayList<>());
        }
        return series.getSeasons();
    }

    private Integer resolveTmdbId(Series series) {
        if (series.getTmdbId() != null) {
            return series.getTmdbId();
        }
        if (series.getLink() == null) {
            return null;
        }
        Matcher matcher = TMDB_TV_ID_PATTERN.matcher(series.getLink());
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ex) {
                log.warn("Failed to parse TMDB ID from link {}", series.getLink(), ex);
            }
        }
        return null;
    }

    private int normalizeSeasonCount(Integer numberOfSeasons) {
        if (numberOfSeasons == null || numberOfSeasons < 1) {
            return 1;
        }
        return numberOfSeasons;
    }

    private SeriesStatus mapSeriesStatus(String tmdbStatus) {
        if (tmdbStatus == null) {
            return null;
        }
        return switch (tmdbStatus.toLowerCase()) {
            case "ended", "canceled", "cancelled" -> SeriesStatus.COMPLETE;
            case "returning series", "in production", "planned" -> SeriesStatus.ONGOING;
            default -> null;
        };
    }
    
    /**
     * Convert Series entity to SeriesResponse DTO.
     * 
     * @param series series entity
     * @return series response DTO
     */
    private SeriesResponse toResponse(Series series) {
        return SeriesResponse.builder()
                .id(series.getId())
                .title(series.getTitle())
                .link(series.getLink())
                .comment(series.getComment())
                .coverImage(series.getCoverImage())
                .genres(series.getGenres())
                .seasons(series.getSeasons())
                .seriesWatchStatus(series.getSeriesWatchStatus())
                .totalAvailableSeasons(series.getTotalAvailableSeasons())
                .hasNewSeasons(series.getHasNewSeasons())
                .seriesStatus(series.getSeriesStatus())
                .addedBy(series.getAddedBy())
                .dateAdded(series.getDateAdded())
                .lastSeasonCheck(series.getLastSeasonCheck())
                .priority(series.getPriority())
                .tmdbId(series.getTmdbId())
                .build();
    }
}
