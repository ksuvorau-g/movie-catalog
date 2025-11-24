package com.moviecat.service;

import com.moviecat.dto.NotificationResponse;
import com.moviecat.exception.ResourceNotFoundException;
import com.moviecat.model.Notification;
import com.moviecat.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for notification operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    /**
     * Get all active (non-dismissed) notifications.
     * 
     * @return list of active notifications
     */
    public List<NotificationResponse> getActiveNotifications() {
        log.info("Getting active notifications");
        
        List<Notification> notifications = notificationRepository.findByDismissed(false);
        log.info("Found {} active notifications", notifications.size());
        
        return notifications.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Dismiss a notification.
     * 
     * @param id notification ID
     * @throws RuntimeException if notification not found
     */
    public void dismissNotification(String id) {
        log.info("Dismissing notification: {}", id);
        
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        
        notification.setDismissed(true);
        notificationRepository.save(notification);
        
        log.info("Notification dismissed successfully: {}", id);
    }
    
    /**
     * Create a new notification for a series with new seasons.
     * Used internally by the season refresh scheduler.
     * 
     * @param seriesId series ID
     * @param seriesTitle series title
     * @param newSeasonsCount number of new seasons
     * @return created notification
     */
    public NotificationResponse createNotification(String seriesId, String seriesTitle, Integer newSeasonsCount) {
        log.info("Creating notification for series {} with {} new seasons", seriesTitle, newSeasonsCount);
        
        String message = String.format("New season%s available for %s", 
                newSeasonsCount > 1 ? "s" : "", seriesTitle);
        
        Notification notification = Notification.builder()
                .seriesId(seriesId)
                .seriesTitle(seriesTitle)
                .message(message)
                .newSeasonsCount(newSeasonsCount)
                .dismissed(false)
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification created successfully with id: {}", savedNotification.getId());
        
        return toResponse(savedNotification);
    }
    
    /**
     * Convert Notification entity to NotificationResponse DTO.
     */
    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .seriesId(notification.getSeriesId())
                .seriesTitle(notification.getSeriesTitle())
                .message(notification.getMessage())
                .newSeasonsCount(notification.getNewSeasonsCount())
                .createdAt(notification.getCreatedAt())
                .dismissed(notification.getDismissed())
                .build();
    }
}
