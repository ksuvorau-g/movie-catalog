package com.moviecat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a movie in the catalog.
 * Stored as a document in MongoDB 'movies' collection.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "movies")
public class Movie {
    
    /**
     * Unique identifier (MongoDB ObjectId).
     */
    @Id
    private String id;
    
    /**
     * Movie title (required field).
     */
    @Indexed
    private String title;
    
    /**
     * URL where the movie can be accessed/watched (optional).
     */
    private String link;
    
    /**
     * User's personal notes, thoughts, or review (optional).
     */
    private String comment;
    
    /**
     * ID reference to image in the images collection (optional).
     */
    private String coverImage;
    
    /**
     * Duration of the movie in minutes (optional).
     */
    private Integer length;
    
    /**
     * Movie genres - can have multiple genres, free text (optional).
     */
    @Builder.Default
    private List<String> genres = new ArrayList<>();
    
    /**
     * Watch status (watched/unwatched).
     */
    @Indexed
    @Builder.Default
    private WatchStatus watchStatus = WatchStatus.UNWATCHED;
    
    /**
     * Name of the person who added this movie (optional).
     */
    @Indexed
    private String addedBy;
    
    /**
     * Date when the movie was added to the catalog (automatic).
     */
    @Indexed
    @Builder.Default
    private LocalDateTime dateAdded = LocalDateTime.now();
    
    /**
     * Manual priority for recommendations (optional).
     * Higher values indicate higher priority.
     * Can be increased/decreased manually through API.
     * Default is 0 (normal priority).
     */
    @Indexed
    @Builder.Default
    private Integer priority = 0;
}
