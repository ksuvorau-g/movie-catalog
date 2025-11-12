package com.moviecat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Movie Catalog REST API Service.
 * 
 * This service provides functionality to:
 * - Manage movie and TV series catalog
 * - Track watched/unwatched status
 * - Recommend next movie/series to watch
 * - Automatically refresh TV series season information
 */
@SpringBootApplication
@EnableScheduling
public class MovieCatalogApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovieCatalogApplication.class, args);
    }

}
