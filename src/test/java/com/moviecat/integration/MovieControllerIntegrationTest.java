package com.moviecat.integration;

import com.moviecat.dto.MovieRequest;
import com.moviecat.dto.MovieResponse;
import com.moviecat.dto.PriorityRequest;
import com.moviecat.dto.WatchStatusRequest;
import com.moviecat.model.WatchStatus;
import com.moviecat.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Movie REST API endpoints.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.autoconfigure.exclude=de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration"
    }
)
class MovieControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MovieRepository movieRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/movies";
        movieRepository.deleteAll();
    }

    @Test
    void shouldCreateMovie() {
        // Given
        MovieRequest request = MovieRequest.builder()
                .title("Inception")
                .link("https://example.com/inception")
                .comment("Great movie about dreams")
                .coverImage("https://example.com/inception.jpg")
                .length(148)
                .genres(List.of("Sci-Fi", "Thriller"))
                .addedBy("John")
                .priority(5)
                .build();

        // When
        ResponseEntity<MovieResponse> response = restTemplate.postForEntity(
                baseUrl,
                request,
                MovieResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Inception");
        assertThat(response.getBody().getGenres()).contains("Sci-Fi", "Thriller");
        assertThat(response.getBody().getWatchStatus()).isEqualTo(WatchStatus.UNWATCHED);
        assertThat(response.getBody().getPriority()).isEqualTo(5);
    }

    @Test
    void shouldGetMovieById() {
        // Given
        MovieRequest request = MovieRequest.builder()
                .title("The Matrix")
                .genres(List.of("Sci-Fi"))
                .addedBy("Alice")
                .build();

        MovieResponse created = restTemplate.postForObject(baseUrl, request, MovieResponse.class);

        // When
        ResponseEntity<MovieResponse> response = restTemplate.getForEntity(
                baseUrl + "/" + created.getId(),
                MovieResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("The Matrix");
    }

    @Test
    void shouldGetAllMovies() {
        // Given
        MovieRequest movie1 = MovieRequest.builder()
                .title("Movie 1")
                .addedBy("User1")
                .build();

        MovieRequest movie2 = MovieRequest.builder()
                .title("Movie 2")
                .addedBy("User2")
                .build();

        restTemplate.postForObject(baseUrl, movie1, MovieResponse.class);
        restTemplate.postForObject(baseUrl, movie2, MovieResponse.class);

        // When
        ResponseEntity<MovieResponse[]> response = restTemplate.getForEntity(
                baseUrl,
                MovieResponse[].class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void shouldUpdateMovie() {
        // Given
        MovieRequest createRequest = MovieRequest.builder()
                .title("Original Title")
                .addedBy("User")
                .build();

        MovieResponse created = restTemplate.postForObject(baseUrl, createRequest, MovieResponse.class);

        MovieRequest updateRequest = MovieRequest.builder()
                .title("Updated Title")
                .comment("Added comment")
                .genres(List.of("Drama"))
                .addedBy("User")
                .build();

        // When
        restTemplate.put(baseUrl + "/" + created.getId(), updateRequest);

        // Then
        ResponseEntity<MovieResponse> response = restTemplate.getForEntity(
                baseUrl + "/" + created.getId(),
                MovieResponse.class
        );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Updated Title");
        assertThat(response.getBody().getComment()).isEqualTo("Added comment");
        assertThat(response.getBody().getGenres()).contains("Drama");
    }

    @Test
    void shouldDeleteMovie() {
        // Given
        MovieRequest request = MovieRequest.builder()
                .title("To Be Deleted")
                .addedBy("User")
                .build();

        MovieResponse created = restTemplate.postForObject(baseUrl, request, MovieResponse.class);

        // When
        restTemplate.delete(baseUrl + "/" + created.getId());

        // Then
        ResponseEntity<MovieResponse> response = restTemplate.getForEntity(
                baseUrl + "/" + created.getId(),
                MovieResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldUpdateWatchStatus() {
        // Given
        MovieRequest request = MovieRequest.builder()
                .title("Watched Movie")
                .addedBy("User")
                .build();

        MovieResponse created = restTemplate.postForObject(baseUrl, request, MovieResponse.class);

        WatchStatusRequest statusRequest = WatchStatusRequest.builder()
                .watchStatus(WatchStatus.WATCHED)
                .build();

        // When
        restTemplate.exchange(
                baseUrl + "/" + created.getId() + "/watch-status",
                org.springframework.http.HttpMethod.PATCH,
                new org.springframework.http.HttpEntity<>(statusRequest),
                MovieResponse.class
        );

        // Then
        ResponseEntity<MovieResponse> response = restTemplate.getForEntity(
                baseUrl + "/" + created.getId(),
                MovieResponse.class
        );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getWatchStatus()).isEqualTo(WatchStatus.WATCHED);
    }

    @Test
    void shouldUpdatePriority() {
        // Given
        MovieRequest request = MovieRequest.builder()
                .title("Priority Movie")
                .addedBy("User")
                .priority(0)
                .build();

        MovieResponse created = restTemplate.postForObject(baseUrl, request, MovieResponse.class);

        PriorityRequest priorityRequest = PriorityRequest.builder()
                .priority(10)
                .build();

        // When
        restTemplate.exchange(
                baseUrl + "/" + created.getId() + "/priority",
                org.springframework.http.HttpMethod.PATCH,
                new org.springframework.http.HttpEntity<>(priorityRequest),
                MovieResponse.class
        );

        // Then
        ResponseEntity<MovieResponse> response = restTemplate.getForEntity(
                baseUrl + "/" + created.getId(),
                MovieResponse.class
        );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPriority()).isEqualTo(10);
    }

    @Test
    void shouldWarnWhenCreatingDuplicateMovie() {
        // Given
        MovieRequest request = MovieRequest.builder()
                .title("Duplicate Movie")
                .addedBy("User")
                .build();

        // When - create same movie twice
        MovieResponse first = restTemplate.postForObject(baseUrl, request, MovieResponse.class);
        MovieResponse second = restTemplate.postForObject(baseUrl, request, MovieResponse.class);

        // Then - both should be created (warning is logged but not blocking)
        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        assertThat(first.getId()).isNotEqualTo(second.getId());
        assertThat(movieRepository.count()).isEqualTo(2);
    }
}
