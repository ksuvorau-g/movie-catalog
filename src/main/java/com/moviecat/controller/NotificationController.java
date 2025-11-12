package com.moviecat.controller;

import com.moviecat.dto.NotificationResponse;
import com.moviecat.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for notification operations.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management endpoints")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @GetMapping
    @Operation(summary = "Get notifications", description = "Retrieve all active notifications")
    public List<NotificationResponse> getNotifications() {
        return notificationService.getActiveNotifications();
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Dismiss notification", description = "Dismiss a notification")
    public void dismissNotification(@PathVariable String id) {
        notificationService.dismissNotification(id);
    }
}
