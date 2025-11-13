package com.moviecat.repository;

import com.moviecat.model.Image;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Image entities.
 * Provides CRUD operations for images.
 */
@Repository
public interface ImageRepository extends MongoRepository<Image, String> {
}
