package com.moviecat.repository;

import com.moviecat.model.Series;
import com.moviecat.model.SeriesStatus;
import com.moviecat.model.WatchStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Series entity.
 * Provides CRUD operations and custom query methods for TV series.
 */
@Repository
public interface SeriesRepository extends MongoRepository<Series, String> {
    
    /**
     * Find series by title (case-insensitive).
     * Useful for duplicate detection.
     * 
     * @param title the series title
     * @return list of series with matching title
     */
    List<Series> findByTitleIgnoreCase(String title);
    
    /**
     * Find series by overall watch status.
     * 
     * @param watchStatus the watch status (WATCHED or UNWATCHED)
     * @return list of series with the specified watch status
     */
    List<Series> findByWatchStatus(WatchStatus watchStatus);
    
    /**
     * Find series with new unwatched seasons.
     * 
     * @param hasNewSeasons true to find series with new seasons
     * @return list of series with new seasons
     */
    List<Series> findByHasNewSeasons(Boolean hasNewSeasons);
    
    /**
     * Find series by status (complete or ongoing).
     * 
     * @param seriesStatus the series status
     * @return list of series with the specified status
     */
    List<Series> findBySeriesStatus(SeriesStatus seriesStatus);
    
    /**
     * Find series by genre (case-insensitive).
     * 
     * @param genre the genre name
     * @return list of series containing the specified genre
     */
    List<Series> findByGenresContainingIgnoreCase(String genre);
    
    /**
     * Find series added by a specific person.
     * 
     * @param addedBy the name of the person who added the series
     * @return list of series added by the specified person
     */
    List<Series> findByAddedBy(String addedBy);
    
    /**
     * Find all series that have a link URL.
     * Used for season refresh scheduling.
     * 
     * @return list of series with link
     */
    @Query("{ 'link': { $exists: true, $ne: null, $ne: '' } }")
    List<Series> findAllWithLink();
    
    /**
     * Find series with unwatched seasons (watchStatus = UNWATCHED).
     * Used for recommendations.
     * 
     * @return list of series that are not fully watched
     */
    @Query("{ 'watchStatus': 'UNWATCHED' }")
    List<Series> findUnwatchedSeries();
}
