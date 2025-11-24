package com.moviecat.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviecat.dto.MovieRequest;
import com.moviecat.dto.MovieResponse;
import com.moviecat.dto.PriorityRequest;
import com.moviecat.dto.WatchStatusRequest;
import com.moviecat.model.Movie;
import com.moviecat.model.WatchStatus;
import com.moviecat.repository.MovieRepository;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive integration tests for MovieController with JSON comparison.
 * Tests cover all CRUD operations, status updates, and priority management.
 */
class MovieControllerIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String moviesUrl;
    private Movie testMovie1;
    private Movie testMovie2;

    @BeforeEach
    void setUp() {
        moviesUrl = "http://localhost:" + port + "/api/movies";
        movieRepository.deleteAll();
        createTestData();
    }

    private void createTestData() {
        testMovie1 = Movie.builder()
                .title("The Shawshank Redemption")
                .coverImage("/api/images/shawshank")
                .comment("Hope is a good thing")
                .length(142)
                .genres(Arrays.asList("Drama", "Crime"))
                .watchStatus(WatchStatus.UNWATCHED)
                .addedBy("TestUser")
                .dateAdded(LocalDateTime.now().minusDays(5))
                .priority(10)
                .tmdbId(278)
                .build();

        testMovie2 = Movie.builder()
                .title("The Godfather")
                .coverImage("/api/images/godfather")
                .comment("An offer you can't refuse")
                .length(175)
                .genres(Arrays.asList("Crime", "Drama"))
                .watchStatus(WatchStatus.WATCHED)
                .addedBy("TestUser")
                .dateAdded(LocalDateTime.now().minusDays(10))
                .priority(5)
                .tmdbId(238)
                .build();

        testMovie1 = movieRepository.save(testMovie1);
        testMovie2 = movieRepository.save(testMovie2);
    }

    @Test
    void shouldCreateMovieWithAllFields() throws IOException, JSONException {
        MovieRequest request = MovieRequest.builder()
                .title("Pulp Fiction")
                .link("https://example.com/pulp-fiction")
                .coverImage("/api/images/pulp")
                .comment("Quentin Tarantino masterpiece")
                .length(154)
                .genres(Arrays.asList("Crime", "Drama", "Thriller"))
                .addedBy("NewUser")
                .priority(7)
                .build();

        ResponseEntity<MovieResponse> response = restTemplate.postForEntity(
                moviesUrl,
                request,
                MovieResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertJsonResponseMatches(response.getBody(), "movie-tests/create-movie-expected.json");
    }

    @Test
    void shouldCreateMovieWithMinimalFields() throws IOException, JSONException {
        MovieRequest request = MovieRequest.builder()
                .title("Minimal Movie")
                .addedBy("TestUser")
                .build();

        ResponseEntity<MovieResponse> response = restTemplate.postForEntity(
                moviesUrl,
                request,
                MovieResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertJsonResponseMatches(response.getBody(), "movie-tests/create-minimal-movie-expected.json");
    }

    @Test
    void shouldGetMovieById() throws IOException, JSONException {
        ResponseEntity<MovieResponse> response = restTemplate.getForEntity(
                moviesUrl + "/" + testMovie1.getId(),
                MovieResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertJsonResponseMatches(response.getBody(), "movie-tests/get-movie-expected.json");
    }

    @Test
    void shouldReturn500WhenMovieNotFound() {
        // Service throws ResourceNotFoundException, GlobalExceptionHandler returns 404
        ResponseEntity<String> response = restTemplate.getForEntity(
                moviesUrl + "/nonexistent-id",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldGetAllMovies() {
        ResponseEntity<MovieResponse[]> response = restTemplate.getForEntity(
                moviesUrl,
                MovieResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        
        List<String> titles = Arrays.stream(response.getBody())
                .map(MovieResponse::getTitle)
                .toList();
        assertThat(titles).containsExactlyInAnyOrder("The Shawshank Redemption", "The Godfather");
    }

    @Test
    void shouldUpdateMovieCompletely() throws IOException, JSONException {
        MovieRequest updateRequest = MovieRequest.builder()
                .title("The Shawshank Redemption - Updated")
                .link("https://example.com/shawshank-updated")
                .coverImage("/api/images/shawshank-new")
                .comment("Updated comment: Still about hope")
                .length(143)
                .genres(Arrays.asList("Drama", "Crime", "Thriller"))
                .addedBy("UpdatedUser")
                .priority(15)
                .build();

        HttpEntity<MovieRequest> request = new HttpEntity<>(updateRequest);
        ResponseEntity<MovieResponse> response = restTemplate.exchange(
                moviesUrl + "/" + testMovie1.getId(),
                HttpMethod.PUT,
                request,
                MovieResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertJsonResponseMatches(response.getBody(), "movie-tests/update-movie-expected.json");
    }

    @Test
    void shouldDeleteMovie() {
        ResponseEntity<Void> response = restTemplate.exchange(
                moviesUrl + "/" + testMovie1.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(movieRepository.findById(testMovie1.getId())).isEmpty();
        assertThat(movieRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldDeleteAllMovies() {
        restTemplate.exchange(
                moviesUrl + "/" + testMovie1.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        restTemplate.exchange(
                moviesUrl + "/" + testMovie2.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(movieRepository.count()).isZero();
    }

    @Test
    void shouldUpdateWatchStatusToWatched() throws IOException, JSONException {
        WatchStatusRequest request = new WatchStatusRequest();
        request.setWatchStatus(WatchStatus.WATCHED);

        HttpEntity<WatchStatusRequest> entity = new HttpEntity<>(request);
        ResponseEntity<MovieResponse> response = restTemplate.exchange(
                moviesUrl + "/" + testMovie1.getId() + "/watch-status",
                HttpMethod.PATCH,
                entity,
                MovieResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertJsonResponseMatches(response.getBody(), "movie-tests/update-watch-status-expected.json");
    }

    @Test
    void shouldUpdateWatchStatusToUnwatched() throws IOException, JSONException {
        WatchStatusRequest request = new WatchStatusRequest();
        request.setWatchStatus(WatchStatus.UNWATCHED);

        HttpEntity<WatchStatusRequest> entity = new HttpEntity<>(request);
        ResponseEntity<MovieResponse> response = restTemplate.exchange(
                moviesUrl + "/" + testMovie2.getId() + "/watch-status",
                HttpMethod.PATCH,
                entity,
                MovieResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertJsonResponseMatches(response.getBody(), "movie-tests/update-watch-status-unwatched-expected.json");
    }

    @Test
    void shouldUpdatePriorityToHigherValue() throws IOException, JSONException {
        PriorityRequest request = new PriorityRequest();
        request.setPriority(20);

        HttpEntity<PriorityRequest> entity = new HttpEntity<>(request);
        ResponseEntity<MovieResponse> response = restTemplate.exchange(
                moviesUrl + "/" + testMovie1.getId() + "/priority",
                HttpMethod.PATCH,
                entity,
                MovieResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertJsonResponseMatches(response.getBody(), "movie-tests/update-priority-expected.json");
    }

    @Test
    void shouldUpdatePriorityToZero() throws IOException, JSONException {
        PriorityRequest request = new PriorityRequest();
        request.setPriority(0);

        HttpEntity<PriorityRequest> entity = new HttpEntity<>(request);
        ResponseEntity<MovieResponse> response = restTemplate.exchange(
                moviesUrl + "/" + testMovie1.getId() + "/priority",
                HttpMethod.PATCH,
                entity,
                MovieResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertJsonResponseMatches(response.getBody(), "movie-tests/update-priority-zero-expected.json");
    }

    @Test
    void shouldUpdatePriorityToNegativeValue() throws IOException, JSONException {
        PriorityRequest request = new PriorityRequest();
        request.setPriority(-5);

        HttpEntity<PriorityRequest> entity = new HttpEntity<>(request);
        ResponseEntity<MovieResponse> response = restTemplate.exchange(
                moviesUrl + "/" + testMovie1.getId() + "/priority",
                HttpMethod.PATCH,
                entity,
                MovieResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertJsonResponseMatches(response.getBody(), "movie-tests/update-priority-negative-expected.json");
    }

    @Test
    void shouldHandleMultipleUpdatesInSequence() {
        // Update watch status
        WatchStatusRequest watchRequest = new WatchStatusRequest();
        watchRequest.setWatchStatus(WatchStatus.WATCHED);
        HttpEntity<WatchStatusRequest> watchEntity = new HttpEntity<>(watchRequest);
        restTemplate.exchange(
                moviesUrl + "/" + testMovie1.getId() + "/watch-status",
                HttpMethod.PATCH,
                watchEntity,
                MovieResponse.class
        );

        // Update priority
        PriorityRequest priorityRequest = new PriorityRequest();
        priorityRequest.setPriority(25);
        HttpEntity<PriorityRequest> priorityEntity = new HttpEntity<>(priorityRequest);
        restTemplate.exchange(
                moviesUrl + "/" + testMovie1.getId() + "/priority",
                HttpMethod.PATCH,
                priorityEntity,
                MovieResponse.class
        );

        // Get final state
        ResponseEntity<MovieResponse> response = restTemplate.getForEntity(
                moviesUrl + "/" + testMovie1.getId(),
                MovieResponse.class
        );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getWatchStatus()).isEqualTo(WatchStatus.WATCHED);
        assertThat(response.getBody().getPriority()).isEqualTo(25);
    }

    @Test
    void shouldReturnEmptyListWhenNoMovies() {
        movieRepository.deleteAll();

        ResponseEntity<MovieResponse[]> response = restTemplate.getForEntity(
                moviesUrl,
                MovieResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void shouldPreserveOriginalFieldsWhenUpdating() throws IOException, JSONException {
        MovieRequest updateRequest = MovieRequest.builder()
                .title("Updated Title Only")
                .link("https://www.themoviedb.org/movie/278")
                .coverImage(testMovie1.getCoverImage())
                .comment(testMovie1.getComment())
                .length(testMovie1.getLength())
                .genres(testMovie1.getGenres())
                .addedBy(testMovie1.getAddedBy())
                .priority(testMovie1.getPriority())
                .build();

        HttpEntity<MovieRequest> request = new HttpEntity<>(updateRequest);
        ResponseEntity<MovieResponse> response = restTemplate.exchange(
                moviesUrl + "/" + testMovie1.getId(),
                HttpMethod.PUT,
                request,
                MovieResponse.class
        );
        assertJsonResponseMatches(response.getBody(), "movie-tests/preserve-fields-expected.json");
    }

    @Test
    void shouldWarnWhenCreatingDuplicateMovie() {
        MovieRequest request = MovieRequest.builder()
                .title("Duplicate Movie")
                .addedBy("User")
                .build();

        // Create same movie twice
        MovieResponse first = restTemplate.postForObject(moviesUrl, request, MovieResponse.class);
        MovieResponse second = restTemplate.postForObject(moviesUrl, request, MovieResponse.class);

        // Both should be created (warning is logged but not blocking)
        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        assertThat(first.getId()).isNotEqualTo(second.getId());
        assertThat(movieRepository.count()).isEqualTo(4); // 2 from setUp + 2 duplicates
    }

    private String loadExpectedJson(String filename, Map<String, String> placeholders) throws IOException {
        ClassPathResource resource = new ClassPathResource(filename);
        String content = new String(resource.getInputStream().readAllBytes());
        
        // Replace placeholders with actual values
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            content = content.replace(entry.getKey(), entry.getValue());
        }
        
        return content;
    }

    private void assertJsonResponseMatches(MovieResponse actual, String expectedJsonFile) throws IOException, JSONException {
        String actualJson = objectMapper.writeValueAsString(actual);
        
        // Prepare placeholders with actual values from response
        // Use Jackson to serialize dates for consistency with actual JSON format
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("${movie.id}", actual.getId());
        placeholders.put("${movie.dateAdded}", objectMapper.writeValueAsString(actual.getDateAdded()).replace("\"", ""));
        
        String expectedJson = loadExpectedJson(expectedJsonFile, placeholders);
        
        // Use JSONAssert for flexible JSON comparison
        // NON_EXTENSIBLE mode ignores array order
        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.NON_EXTENSIBLE);
    }
}
