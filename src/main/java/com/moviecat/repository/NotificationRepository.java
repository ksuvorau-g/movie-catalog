package com.moviecat.repository;

import com.moviecat.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Notification entity.
 * Provides CRUD operations and custom query methods for notifications.
 */
@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    
    /**
     * Find all active (non-dismissed) notifications.
     * 
     * @param dismissed false to find active notifications
     * @return list of active notifications
     */
    List<Notification> findByDismissed(Boolean dismissed);
    
    /**
     * Find notifications for a specific series.
     * 
     * @param seriesId the series ID
     * @return list of notifications for the specified series
     */
    List<Notification> findBySeriesId(String seriesId);
    
    /**
     * Find active notifications for a specific series.
     * 
     * @param seriesId the series ID
     * @param dismissed false to find active notifications
     * @return list of active notifications for the specified series
     */
    List<Notification> findBySeriesIdAndDismissed(String seriesId, Boolean dismissed);
}
