package com.moviecat.integration;

import com.moviecat.dto.CatalogItemResponse;
import com.moviecat.dto.MovieRequest;
import com.moviecat.dto.SeriesRequest;
import com.moviecat.model.ContentType;
import com.moviecat.model.WatchStatus;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Catalog REST API endpoints.
 */
class CatalogControllerIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private SeriesRepository seriesRepository;

    private String catalogUrl;
    private String moviesUrl;
    private String seriesUrl;

    @BeforeEach
    void setUp() {
        catalogUrl = "http://localhost:" + port + "/api/catalog";
        moviesUrl = "http://localhost:" + port + "/api/movies";
        seriesUrl = "http://localhost:" + port + "/api/series";
        
        movieRepository.deleteAll();
        seriesRepository.deleteAll();
    }

    @Test
    void shouldGetCombinedCatalog() {
        // Given - create movies and series
        MovieRequest movie = MovieRequest.builder()
                .title("Test Movie")
                .genres(List.of("Action"))
                .addedBy("User1")
                .build();

        SeriesRequest series = SeriesRequest.builder()
                .title("Test Series")
                .genres(List.of("Drama"))
                .addedBy("User1")
                .build();

        restTemplate.postForObject(moviesUrl, movie, Object.class);
        restTemplate.postForObject(seriesUrl, series, Object.class);

        // When
        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl,
                CatalogItemResponse[].class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        
        boolean hasMovie = false;
        boolean hasSeries = false;
        for (CatalogItemResponse item : response.getBody()) {
            if (item.getContentType() == ContentType.MOVIE) {
                hasMovie = true;
                assertThat(item.getTitle()).isEqualTo("Test Movie");
            } else if (item.getContentType() == ContentType.SERIES) {
                hasSeries = true;
                assertThat(item.getTitle()).isEqualTo("Test Series");
            }
        }
        assertThat(hasMovie).isTrue();
        assertThat(hasSeries).isTrue();
    }

    @Test
    void shouldFilterCatalogByContentType() {
        // Given
        MovieRequest movie = MovieRequest.builder()
                .title("Movie Only")
                .addedBy("User")
                .build();

        SeriesRequest series = SeriesRequest.builder()
                .title("Series Only")
                .addedBy("User")
                .build();

        restTemplate.postForObject(moviesUrl, movie, Object.class);
        restTemplate.postForObject(seriesUrl, series, Object.class);

        // When - filter by MOVIE
        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl + "?contentType=MOVIE",
                CatalogItemResponse[].class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getContentType()).isEqualTo(ContentType.MOVIE);
        assertThat(response.getBody()[0].getTitle()).isEqualTo("Movie Only");
    }

    @Test
    void shouldFilterCatalogByGenre() {
        // Given
        MovieRequest sciFiMovie = MovieRequest.builder()
                .title("Sci-Fi Movie")
                .genres(List.of("Sci-Fi", "Action"))
                .addedBy("User")
                .build();

        MovieRequest dramaMovie = MovieRequest.builder()
                .title("Drama Movie")
                .genres(List.of("Drama"))
                .addedBy("User")
                .build();

        restTemplate.postForObject(moviesUrl, sciFiMovie, Object.class);
        restTemplate.postForObject(moviesUrl, dramaMovie, Object.class);

        // When
        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl + "?genre=Sci-Fi",
                CatalogItemResponse[].class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getTitle()).isEqualTo("Sci-Fi Movie");
        assertThat(response.getBody()[0].getGenres()).contains("Sci-Fi");
    }

    @Test
    void shouldFilterCatalogByWatchStatus() {
        // Given
        MovieRequest unwatchedMovie = MovieRequest.builder()
                .title("Unwatched Movie")
                .addedBy("User")
                .build();

        restTemplate.postForObject(moviesUrl, unwatchedMovie, Object.class);

        // When
        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl + "?watchStatus=UNWATCHED",
                CatalogItemResponse[].class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getWatchStatus()).isEqualTo(WatchStatus.UNWATCHED.name());
    }

    @Test
    void shouldSortCatalogByTitle() {
        // Given
        MovieRequest movieZ = MovieRequest.builder()
                .title("Z Movie")
                .addedBy("User")
                .build();

        MovieRequest movieA = MovieRequest.builder()
                .title("A Movie")
                .addedBy("User")
                .build();

        MovieRequest movieM = MovieRequest.builder()
                .title("M Movie")
                .addedBy("User")
                .build();

        restTemplate.postForObject(moviesUrl, movieZ, Object.class);
        restTemplate.postForObject(moviesUrl, movieA, Object.class);
        restTemplate.postForObject(moviesUrl, movieM, Object.class);

        // When
        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl + "?sortBy=title",
                CatalogItemResponse[].class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(3);
        assertThat(response.getBody()[0].getTitle()).isEqualTo("A Movie");
        assertThat(response.getBody()[1].getTitle()).isEqualTo("M Movie");
        assertThat(response.getBody()[2].getTitle()).isEqualTo("Z Movie");
    }

    @Test
    void shouldSearchCatalog() {
        // Given
        MovieRequest movie1 = MovieRequest.builder()
                .title("The Dark Knight")
                .comment("Batman saves Gotham")
                .genres(List.of("Action", "Superhero"))
                .addedBy("User")
                .build();

        MovieRequest movie2 = MovieRequest.builder()
                .title("Inception")
                .comment("Dream heist")
                .genres(List.of("Sci-Fi"))
                .addedBy("User")
                .build();

        restTemplate.postForObject(moviesUrl, movie1, Object.class);
        restTemplate.postForObject(moviesUrl, movie2, Object.class);

        // When - search for "Batman"
        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl + "/search?query=Batman",
                CatalogItemResponse[].class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getTitle()).isEqualTo("The Dark Knight");
    }

    @Test
    void shouldSearchCatalogByGenre() {
        // Given
        MovieRequest movie = MovieRequest.builder()
                .title("Generic Title")
                .genres(List.of("Western"))
                .addedBy("User")
                .build();

        restTemplate.postForObject(moviesUrl, movie, Object.class);

        // When
        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl + "/search?query=western",
                CatalogItemResponse[].class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getGenres()).contains("Western");
    }

    @Test
    void shouldHandleMultipleFilters() {
        // Given
        MovieRequest targetMovie = MovieRequest.builder()
                .title("Target Movie")
                .genres(List.of("Action"))
                .addedBy("Alice")
                .build();

        MovieRequest otherMovie = MovieRequest.builder()
                .title("Other Movie")
                .genres(List.of("Drama"))
                .addedBy("Bob")
                .build();

        restTemplate.postForObject(moviesUrl, targetMovie, Object.class);
        restTemplate.postForObject(moviesUrl, otherMovie, Object.class);

        // When - filter by genre AND addedBy
        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl + "?genre=Action&addedBy=Alice",
                CatalogItemResponse[].class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getTitle()).isEqualTo("Target Movie");
        assertThat(response.getBody()[0].getAddedBy()).isEqualTo("Alice");
    }
}
