package com.moviecat.service;

import com.moviecat.dto.CatalogItemResponse;
import com.moviecat.model.*;
import com.moviecat.repository.MovieRepository;
import com.moviecat.repository.SeriesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.moviecat.util.TmdbLinkUtil.buildTmdbLink;

/**
 * Service for catalog operations (combined movies and series).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CatalogService {
    
    private final MovieRepository movieRepository;
    private final SeriesRepository seriesRepository;
    
    /**
     * Get combined catalog of movies and series with optional filters.
     * 
     * @param contentType filter by content type (MOVIE, SERIES, or null for both)
     * @param genre filter by genre
     * @param watchStatus filter by watch status
     * @param addedBy filter by person who added
     * @param hasNewSeasons filter series with new seasons
     * @param seriesStatus filter by series status (COMPLETE, ONGOING)
     * @param sortBy sort field (title, dateAdded, length)
     * @return list of catalog items
     */
    public List<CatalogItemResponse> getCatalog(
            String contentType,
            String genre,
            String watchStatus,
            String addedBy,
            Boolean hasNewSeasons,
            String seriesStatus,
            String sortBy) {
        log.info("Getting catalog with filters - contentType: {}, genre: {}, watchStatus: {}, addedBy: {}, hasNewSeasons: {}, seriesStatus: {}, sortBy: {}",
                contentType, genre, watchStatus, addedBy, hasNewSeasons, seriesStatus, sortBy);
        
        List<CatalogItemResponse> catalogItems = new ArrayList<>();
        
        // Determine which content types to include
        boolean includeMovies = contentType == null || contentType.equalsIgnoreCase("MOVIE");
        boolean includeSeries = contentType == null || contentType.equalsIgnoreCase("SERIES");
        
        // Get and filter movies
        if (includeMovies) {
            List<Movie> movies = movieRepository.findAll();
            Stream<Movie> movieStream = movies.stream();
            
            if (genre != null && !genre.isEmpty()) {
                movieStream = movieStream.filter(m -> m.getGenres() != null && 
                        m.getGenres().stream().anyMatch(g -> g.equalsIgnoreCase(genre)));
            }
            if (watchStatus != null && !watchStatus.isEmpty()) {
                WatchStatus status = WatchStatus.valueOf(watchStatus.toUpperCase());
                movieStream = movieStream.filter(m -> m.getWatchStatus() == status);
            }
            if (addedBy != null && !addedBy.isEmpty()) {
                movieStream = movieStream.filter(m -> addedBy.equals(m.getAddedBy()));
            }
            
            catalogItems.addAll(movieStream.map(this::movieToResponse).collect(Collectors.toList()));
        }
        
        // Get and filter series
        if (includeSeries) {
            List<Series> seriesList = seriesRepository.findAll();
            Stream<Series> seriesStream = seriesList.stream();
            
            if (genre != null && !genre.isEmpty()) {
                seriesStream = seriesStream.filter(s -> s.getGenres() != null && 
                        s.getGenres().stream().anyMatch(g -> g.equalsIgnoreCase(genre)));
            }
            if (watchStatus != null && !watchStatus.isEmpty()) {
                WatchStatus status = WatchStatus.valueOf(watchStatus.toUpperCase());
                seriesStream = seriesStream.filter(s -> s.getWatchStatus() == status);
            }
            if (addedBy != null && !addedBy.isEmpty()) {
                seriesStream = seriesStream.filter(s -> addedBy.equals(s.getAddedBy()));
            }
            if (hasNewSeasons != null) {
                seriesStream = seriesStream.filter(s -> hasNewSeasons.equals(s.getHasNewSeasons()));
            }
            if (seriesStatus != null && !seriesStatus.isEmpty()) {
                SeriesStatus status = SeriesStatus.valueOf(seriesStatus.toUpperCase());
                seriesStream = seriesStream.filter(s -> s.getSeriesStatus() == status);
            }
            
            catalogItems.addAll(seriesStream.map(this::seriesToResponse).collect(Collectors.toList()));
        }
        
        // Apply sorting - always prioritize unwatched items first
        Comparator<CatalogItemResponse> comparator = createWatchStatusComparator();
        
        if (sortBy != null && !sortBy.isEmpty()) {
            Comparator<CatalogItemResponse> secondarySort;
            switch (sortBy.toLowerCase()) {
                case "title":
                    secondarySort = Comparator.comparing(CatalogItemResponse::getTitle);
                    break;
                case "dateadded":
                    secondarySort = Comparator.comparing(CatalogItemResponse::getDateAdded).reversed();
                    break;
                case "length":
                    secondarySort = Comparator.comparing(item -> 
                            item.getLength() != null ? item.getLength() : 0, Comparator.reverseOrder());
                    break;
                default:
                    log.warn("Unknown sort field: {}", sortBy);
                    secondarySort = Comparator.comparing(CatalogItemResponse::getDateAdded).reversed();
            }
            comparator = comparator.thenComparing(secondarySort);
        } else {
            // Default sort: unwatched first, then by priority (higher first), then by date added (older first)
            comparator = comparator
                    .thenComparing(Comparator.comparing(CatalogItemResponse::getPriority, 
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .thenComparing(Comparator.comparing(CatalogItemResponse::getDateAdded));
        }
        
        catalogItems.sort(comparator);
        
        log.info("Returning {} catalog items", catalogItems.size());
        return catalogItems;
    }
    
    /**
     * Search catalog by title or other attributes.
     * 
     * @param query search query
     * @return list of matching catalog items
     */
    public List<CatalogItemResponse> searchCatalog(String query) {
        log.info("Searching catalog with query: {}", query);
        
        List<CatalogItemResponse> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        // Search movies
        List<Movie> movies = movieRepository.findAll();
        results.addAll(movies.stream()
                .filter(m -> m.getTitle().toLowerCase().contains(lowerQuery) ||
                        (m.getComment() != null && m.getComment().toLowerCase().contains(lowerQuery)) ||
                        (m.getGenres() != null && m.getGenres().stream()
                                .anyMatch(g -> g.toLowerCase().contains(lowerQuery))))
                .map(this::movieToResponse)
                .collect(Collectors.toList()));
        
        // Search series
        List<Series> seriesList = seriesRepository.findAll();
        results.addAll(seriesList.stream()
                .filter(s -> s.getTitle().toLowerCase().contains(lowerQuery) ||
                        (s.getComment() != null && s.getComment().toLowerCase().contains(lowerQuery)) ||
                        (s.getGenres() != null && s.getGenres().stream()
                                .anyMatch(g -> g.toLowerCase().contains(lowerQuery))))
                .map(this::seriesToResponse)
                .collect(Collectors.toList()));
        
        // Sort results: unwatched items first, then by priority (higher first), then by date added (older first)
        Comparator<CatalogItemResponse> comparator = createWatchStatusComparator()
                .thenComparing(Comparator.comparing(CatalogItemResponse::getPriority, 
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .thenComparing(Comparator.comparing(CatalogItemResponse::getDateAdded));
        results.sort(comparator);
        
        log.info("Found {} matching items", results.size());
        return results;
    }
    
    /**
     * Create comparator that prioritizes unwatched items first.
     */
    private Comparator<CatalogItemResponse> createWatchStatusComparator() {
        return Comparator.comparing((CatalogItemResponse item) -> {
            // UNWATCHED = 0, WATCHED = 1, so unwatched items come first
            return item.getWatchStatus() == WatchStatus.UNWATCHED ? 0 : 1;
        });
    }
    
    /**
     * Convert Movie entity to CatalogItemResponse.
     */
    private CatalogItemResponse movieToResponse(Movie movie) {
        return CatalogItemResponse.builder()
                .id(movie.getId())
                .contentType(ContentType.MOVIE)
                .title(movie.getTitle())
                .link(buildTmdbLink(movie.getTmdbId(), true))
                .coverImage(movie.getCoverImage())
                .comment(movie.getComment())
                .genres(movie.getGenres())
                .watchStatus(movie.getWatchStatus())
                .addedBy(movie.getAddedBy())
                .dateAdded(movie.getDateAdded())
                .priority(movie.getPriority())
                .tmdbId(movie.getTmdbId())
                .length(movie.getLength())
                .build();
    }
    
    /**
     * Convert Series entity to CatalogItemResponse.
     */
    private CatalogItemResponse seriesToResponse(Series series) {
        return CatalogItemResponse.builder()
                .id(series.getId())
                .contentType(ContentType.SERIES)
                .title(series.getTitle())
                .link(buildTmdbLink(series.getTmdbId(), false))
                .coverImage(series.getCoverImage())
                .comment(series.getComment())
                .genres(series.getGenres())
                .watchStatus(series.getWatchStatus() != null ? series.getWatchStatus() : null)
                .addedBy(series.getAddedBy())
                .dateAdded(series.getDateAdded())
                .priority(series.getPriority())
                .tmdbId(series.getTmdbId())
                .seasons(series.getSeasons())
                .hasNewSeasons(series.getHasNewSeasons())
                .seriesStatus(series.getSeriesStatus() != null ? series.getSeriesStatus().toString() : null)
                .totalAvailableSeasons(series.getTotalAvailableSeasons())
                .build();
    }

}
