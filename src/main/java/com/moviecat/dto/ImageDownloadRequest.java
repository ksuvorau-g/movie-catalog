package com.moviecat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for image download request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageDownloadRequest {
    
    /**
     * URL of the image to download.
     */
    @NotBlank(message = "Image URL is required")
    private String imageUrl;
}
