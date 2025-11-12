package com.moviecat.service;

import com.moviecat.dto.RecommendationResponse;
import com.moviecat.model.*;
import com.moviecat.repository.MovieRepository;
import com.moviecat.repository.SeriesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Service for recommendation logic.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationService {
    
    private final MovieRepository movieRepository;
    private final SeriesRepository seriesRepository;
    private final Random random = new Random();
    
    /**
     * Get recommendation for next movie/series to watch.
     * Uses weighted random algorithm considering:
     * - Manual priority (highest)
     * - Series with new seasons
     * - Age-based weighting (older items prioritized)
     * 
     * @return recommended movie or series
     * @throws RuntimeException if no unwatched content available
     */
    public RecommendationResponse getRecommendation() {
        log.info("Getting recommendation");
        
        List<WeightedItem> candidates = new ArrayList<>();
        
        // Get unwatched movies
        List<Movie> unwatchedMovies = movieRepository.findByWatchStatus(WatchStatus.UNWATCHED);
        for (Movie movie : unwatchedMovies) {
            double weight = calculateWeight(movie.getDateAdded(), movie.getPriority(), false);
            candidates.add(new WeightedItem(movie, null, weight));
        }
        
        // Get series with unwatched seasons
        List<Series> allSeries = seriesRepository.findAll();
        for (Series series : allSeries) {
            if (series.getSeriesWatchStatus() == WatchStatus.UNWATCHED) {
                boolean hasNewSeasons = series.getHasNewSeasons() != null && series.getHasNewSeasons();
                double weight = calculateWeight(series.getDateAdded(), series.getPriority(), hasNewSeasons);
                candidates.add(new WeightedItem(null, series, weight));
            }
        }
        
        if (candidates.isEmpty()) {
            throw new RuntimeException("No unwatched content available for recommendation");
        }
        
        // Select based on weighted random
        WeightedItem selected = selectWeightedRandom(candidates);
        
        log.info("Recommendation selected: {} (weight: {})", 
                selected.movie != null ? selected.movie.getTitle() : selected.series.getTitle(),
                selected.weight);
        
        if (selected.movie != null) {
            return movieToRecommendation(selected.movie);
        } else {
            return seriesToRecommendation(selected.series);
        }
    }
    
    /**
     * Calculate weight for an item based on age, priority, and new seasons flag.
     * Priority levels:
     * - High manual priority (priority > 0): base weight * (1 + priority)
     * - Series with new seasons: base weight * 10
     * - Age-based: older items get higher weight
     */
    private double calculateWeight(LocalDateTime dateAdded, Integer priority, boolean hasNewSeasons) {
        // Calculate days since added
        long daysOld = ChronoUnit.DAYS.between(dateAdded, LocalDateTime.now());
        
        // Base weight increases with age (exponential curve)
        double baseWeight = Math.log(daysOld + 2) + 1; // +2 to avoid log(0)
        
        // Apply priority multiplier (highest priority)
        if (priority != null && priority > 0) {
            baseWeight *= (1 + priority);
        }
        
        // Apply new seasons boost (very high priority)
        if (hasNewSeasons) {
            baseWeight *= 10;
        }
        
        return baseWeight;
    }
    
    /**
     * Select item using weighted random selection.
     */
    private WeightedItem selectWeightedRandom(List<WeightedItem> candidates) {
        double totalWeight = candidates.stream().mapToDouble(item -> item.weight).sum();
        double randomValue = random.nextDouble() * totalWeight;
        
        double cumulativeWeight = 0;
        for (WeightedItem item : candidates) {
            cumulativeWeight += item.weight;
            if (randomValue <= cumulativeWeight) {
                return item;
            }
        }
        
        // Fallback to last item (shouldn't happen)
        return candidates.get(candidates.size() - 1);
    }
    
    /**
     * Convert Movie to RecommendationResponse.
     */
    private RecommendationResponse movieToRecommendation(Movie movie) {
        return RecommendationResponse.builder()
                .id(movie.getId())
                .contentType(ContentType.MOVIE)
                .title(movie.getTitle())
                .link(movie.getLink())
                .linkDescription(movie.getLinkDescription())
                .coverImage(movie.getCoverImage())
                .comment(movie.getComment())
                .priority(movie.getPriority())
                .length(movie.getLength())
                .build();
    }
    
    /**
     * Convert Series to RecommendationResponse.
     */
    private RecommendationResponse seriesToRecommendation(Series series) {
        return RecommendationResponse.builder()
                .id(series.getId())
                .contentType(ContentType.SERIES)
                .title(series.getTitle())
                .link(series.getLink())
                .linkDescription(series.getLinkDescription())
                .coverImage(series.getCoverImage())
                .comment(series.getComment())
                .priority(series.getPriority())
                .hasNewSeasons(series.getHasNewSeasons())
                .totalAvailableSeasons(series.getTotalAvailableSeasons())
                .build();
    }
    
    /**
     * Helper class to hold items with their calculated weights.
     */
    private static class WeightedItem {
        Movie movie;
        Series series;
        double weight;
        
        WeightedItem(Movie movie, Series series, double weight) {
            this.movie = movie;
            this.series = series;
            this.weight = weight;
        }
    }
}
