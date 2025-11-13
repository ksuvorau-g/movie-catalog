package com.moviecat.controller;

import com.moviecat.dto.ImageDownloadRequest;
import com.moviecat.dto.ImageResponse;
import com.moviecat.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * REST controller for image management operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Tag(name = "Images", description = "Image management operations")
public class ImageController {
    
    private final ImageService imageService;
    
    /**
     * Download an image from a URL and store it.
     * 
     * @param request the image download request
     * @return the saved image metadata
     */
    @PostMapping("/download")
    @Operation(summary = "Download and save an image from a URL")
    public ResponseEntity<ImageResponse> downloadImage(
            @Valid @RequestBody ImageDownloadRequest request) {
        try {
            log.info("Received request to download image from: {}", request.getImageUrl());
            ImageResponse response = imageService.downloadAndSaveImage(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IOException e) {
            log.error("Failed to download image from: {}", request.getImageUrl(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Unexpected error downloading image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get an image by its ID (serves the actual image file).
     * 
     * @param id the image ID
     * @return the image file
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get an image by ID")
    public ResponseEntity<Resource> getImage(@PathVariable String id) {
        try {
            Resource imageFile = imageService.getImageFile(id);
            ImageResponse metadata = imageService.getImageById(id);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(metadata.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "inline; filename=\"" + metadata.getFilename() + "\"")
                    .body(imageFile);
        } catch (MalformedURLException e) {
            log.error("Invalid file path for image: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            log.error("Image not found: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Get image metadata by ID.
     * 
     * @param id the image ID
     * @return the image metadata
     */
    @GetMapping("/{id}/metadata")
    @Operation(summary = "Get image metadata by ID")
    public ResponseEntity<ImageResponse> getImageMetadata(@PathVariable String id) {
        try {
            ImageResponse response = imageService.getImageById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Image not found: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Delete an image by ID.
     * 
     * @param id the image ID
     * @return no content response
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an image by ID")
    public ResponseEntity<Void> deleteImage(@PathVariable String id) {
        try {
            imageService.deleteImage(id);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            log.error("Failed to delete image file: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            log.error("Image not found: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
