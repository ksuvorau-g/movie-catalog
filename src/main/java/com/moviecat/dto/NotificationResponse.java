package com.moviecat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for notifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    
    private String id;
    private String seriesId;
    private String seriesTitle;
    private String message;
    private Integer newSeasonsCount;
    private LocalDateTime createdAt;
    private Boolean dismissed;
}
