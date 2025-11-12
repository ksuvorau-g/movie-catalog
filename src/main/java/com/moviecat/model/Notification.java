package com.moviecat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Entity representing a notification for new TV series seasons.
 * Stored as a document in MongoDB 'notifications' collection.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {
    
    /**
     * Unique identifier (MongoDB ObjectId).
     */
    @Id
    private String id;
    
    /**
     * Reference to the series that has new seasons.
     */
    @Indexed
    private String seriesId;
    
    /**
     * Title of the series (for display purposes).
     */
    private String seriesTitle;
    
    /**
     * Notification message.
     */
    private String message;
    
    /**
     * Number of new seasons available.
     */
    private Integer newSeasonsCount;
    
    /**
     * When the notification was created.
     */
    @Indexed
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * Whether the notification has been dismissed.
     */
    @Indexed
    @Builder.Default
    private Boolean dismissed = false;
}
