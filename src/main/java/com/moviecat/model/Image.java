package com.moviecat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Entity representing an image stored in the system.
 * Stored as a document in MongoDB 'images' collection.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "images")
public class Image {
    
    /**
     * Unique identifier (MongoDB ObjectId).
     */
    @Id
    private String id;
    
    /**
     * Original URL from which the image was downloaded.
     */
    private String originalUrl;
    
    /**
     * Filename of the stored image (with extension).
     */
    private String filename;
    
    /**
     * Content type of the image (e.g., image/jpeg, image/png).
     */
    private String contentType;
    
    /**
     * Size of the image in bytes.
     */
    private Long fileSize;
    
    /**
     * Date when the image was uploaded/downloaded.
     */
    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();
}
