package com.moviecat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for image response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponse {
    
    /**
     * Unique image ID.
     */
    private String id;
    
    /**
     * Original URL from which the image was downloaded.
     */
    private String originalUrl;
    
    /**
     * Filename of the stored image.
     */
    private String filename;
    
    /**
     * Content type of the image.
     */
    private String contentType;
    
    /**
     * Size of the image in bytes.
     */
    private Long fileSize;
    
    /**
     * Upload timestamp.
     */
    private LocalDateTime uploadedAt;
    
    /**
     * URL to access the image.
     */
    private String imageUrl;
}
