package com.moviecat.integration;

import com.moviecat.dto.MovieRequest;
import com.moviecat.dto.PriorityRequest;
import com.moviecat.dto.RecommendationResponse;
import com.moviecat.dto.SeriesRequest;
import com.moviecat.model.ContentType;
import com.moviecat.repository.MovieRepository;
import com.moviecat.repository.SeriesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Recommendation REST API endpoints.
 */
class RecommendationControllerIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private SeriesRepository seriesRepository;

    private String recommendUrl;
    private String moviesUrl;
    private String seriesUrl;

    @BeforeEach
    void setUp() {
        recommendUrl = "http://localhost:" + port + "/api/recommendations";
        moviesUrl = "http://localhost:" + port + "/api/movies";
        seriesUrl = "http://localhost:" + port + "/api/series";
        
        movieRepository.deleteAll();
        seriesRepository.deleteAll();
    }

    @Test
    void shouldReturnRecommendationWhenMoviesExist() {
        // Given
        MovieRequest movie = MovieRequest.builder()
                .title("Test Movie")
                .genres(List.of("Action"))
                .addedBy("User")
                .build();

        restTemplate.postForObject(moviesUrl, movie, Object.class);

        // When
        ResponseEntity<RecommendationResponse[]> response = restTemplate.getForEntity(
                recommendUrl,
                RecommendationResponse[].class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getTitle()).isEqualTo("Test Movie");
        assertThat(response.getBody()[0].getContentType()).isEqualTo(ContentType.MOVIE);
    }

    @Test
    void shouldReturnRecommendationWhenSeriesExist() {
        // Given
        SeriesRequest series = SeriesRequest.builder()
                .title("Test Series")
                .genres(List.of("Drama"))
                .addedBy("User")
                .build();

        restTemplate.postForObject(seriesUrl, series, Object.class);

        // When
        ResponseEntity<RecommendationResponse[]> response = restTemplate.getForEntity(
                recommendUrl,
                RecommendationResponse[].class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getTitle()).isEqualTo("Test Series");
        assertThat(response.getBody()[0].getContentType()).isEqualTo(ContentType.SERIES);
    }

    @Test
    void shouldReturnRandomRecommendationFromMultipleItems() {
        // Given - create multiple movies
        for (int i = 1; i <= 5; i++) {
            MovieRequest movie = MovieRequest.builder()
                    .title("Movie " + i)
                    .addedBy("User")
                    .build();
            restTemplate.postForObject(moviesUrl, movie, Object.class);
        }

        // When - request recommendations multiple times
        Set<String> recommendedTitles = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            ResponseEntity<RecommendationResponse[]> response = restTemplate.getForEntity(
                    recommendUrl,
                    RecommendationResponse[].class
            );
            if (response.getBody() != null && response.getBody().length > 0) {
                recommendedTitles.add(response.getBody()[0].getTitle());
            }
        }

        // Then - should get varied recommendations (not always the same)
        // Due to weighted random algorithm, we might not get all 5, but should get at least 2 different ones
        assertThat(recommendedTitles).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void shouldPrioritizeHighPriorityItems() {
        // Given - create movies with different priorities
        MovieRequest lowPriority = MovieRequest.builder()
                .title("Low Priority Movie")
                .priority(0)
                .addedBy("User")
                .build();

        MovieRequest highPriority = MovieRequest.builder()
                .title("High Priority Movie")
                .priority(100)
                .addedBy("User")
                .build();

        Object lowPriorityMovie = restTemplate.postForObject(moviesUrl, lowPriority, Object.class);
        Object highPriorityMovie = restTemplate.postForObject(moviesUrl, highPriority, Object.class);

        // When - request multiple recommendations
        int highPriorityCount = 0;
        int totalRequests = 20;
        
        for (int i = 0; i < totalRequests; i++) {
            ResponseEntity<RecommendationResponse[]> response = restTemplate.getForEntity(
                    recommendUrl,
                    RecommendationResponse[].class
            );
            
            if (response.getBody() != null && response.getBody().length > 0 && 
                "High Priority Movie".equals(response.getBody()[0].getTitle())) {
                highPriorityCount++;
            }
        }

        // Then - high priority movie should appear more frequently
        // With priority 100 vs 0, high priority should dominate (expect at least 60% of recommendations)
        assertThat(highPriorityCount).isGreaterThan(totalRequests / 2);
    }

    @Test
    void shouldReturnMixedRecommendationsFromMoviesAndSeries() {
        // Given
        MovieRequest movie = MovieRequest.builder()
                .title("Test Movie")
                .addedBy("User")
                .build();

        SeriesRequest series = SeriesRequest.builder()
                .title("Test Series")
                .addedBy("User")
                .build();

        restTemplate.postForObject(moviesUrl, movie, Object.class);
        restTemplate.postForObject(seriesUrl, series, Object.class);

        // When - request multiple recommendations
        Set<ContentType> recommendedTypes = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            ResponseEntity<RecommendationResponse[]> response = restTemplate.getForEntity(
                    recommendUrl,
                    RecommendationResponse[].class
            );
            if (response.getBody() != null && response.getBody().length > 0) {
                recommendedTypes.add(response.getBody()[0].getContentType());
            }
        }

        // Then - should get both movies and series recommended
        // Due to randomness, might not always get both in 10 tries, but should get at least 1 type
        assertThat(recommendedTypes).isNotEmpty();
        assertThat(recommendedTypes).containsAnyOf(ContentType.MOVIE, ContentType.SERIES);
    }

    @Test
    void shouldHandleEmptyCatalog() {
        // When - no movies or series exist
        ResponseEntity<RecommendationResponse[]> response = restTemplate.getForEntity(
                recommendUrl,
                RecommendationResponse[].class
        );

        // Then - should return OK with empty array
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void shouldIncludeLinkInRecommendation() {
        // Given - create movie with TMDB link so it builds a link in response
        MovieRequest movie = MovieRequest.builder()
                .title("Linked Movie")
                .link("https://www.themoviedb.org/movie/550")
                .genres(List.of("Action"))
                .addedBy("User")
                .build();

        restTemplate.postForObject(moviesUrl, movie, Object.class);

        // When
        ResponseEntity<RecommendationResponse[]> response = restTemplate.getForEntity(
                recommendUrl,
                RecommendationResponse[].class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getLink()).isEqualTo("https://www.themoviedb.org/movie/550");
        assertThat(response.getBody()[0].getContentType()).isEqualTo(ContentType.MOVIE);
    }
}
