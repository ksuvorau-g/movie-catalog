package com.moviecat.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviecat.dto.CatalogItemResponse;
import com.moviecat.model.*;
import com.moviecat.repository.MovieRepository;
import com.moviecat.repository.SeriesRepository;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive integration tests for CatalogController with JSON comparison.
 * Tests cover all filtering, sorting, and search scenarios with maximum field population.
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

    @Autowired
    private ObjectMapper objectMapper;

    private String catalogUrl;
    private Movie movie1;
    private Movie movie2;
    private Series series1;

    @BeforeEach
    void setUp() {
        catalogUrl = "http://localhost:" + port + "/api/catalog";
        
        movieRepository.deleteAll();
        seriesRepository.deleteAll();

        createTestData();
    }

    private void createTestData() {
        movie1 = Movie.builder()
                .title("Inception")
                .coverImage("/api/images/image1")
                .comment("Mind-bending thriller about dreams within dreams")
                .length(148)
                .genres(Arrays.asList("Sci-Fi", "Thriller", "Action"))
                .watchStatus(WatchStatus.UNWATCHED)
                .addedBy("Alice")
                .dateAdded(LocalDateTime.now().minusDays(10))
                .priority(5)
                .tmdbId(27205)
                .build();

        movie2 = Movie.builder()
                .title("The Matrix")
                .coverImage("/api/images/image3")
                .comment("Reality is not what it seems")
                .length(136)
                .genres(Arrays.asList("Sci-Fi", "Action"))
                .watchStatus(WatchStatus.WATCHED)
                .addedBy("Alice")
                .dateAdded(LocalDateTime.now().minusDays(5))
                .priority(0)
                .tmdbId(603)
                .build();

        series1 = Series.builder()
                .title("Breaking Bad")
                .coverImage("/api/images/image2")
                .comment("Chemistry teacher turns into drug kingpin")
                .genres(Arrays.asList("Crime", "Drama", "Thriller"))
                .seasons(Arrays.asList(
                        Season.builder().seasonNumber(1).watchStatus(WatchStatus.WATCHED).build(),
                        Season.builder().seasonNumber(2).watchStatus(WatchStatus.UNWATCHED).build()
                ))
                .watchStatus(WatchStatus.UNWATCHED)
                .totalAvailableSeasons(5)
                .hasNewSeasons(true)
                .seriesStatus(SeriesStatus.COMPLETE)
                .addedBy("Bob")
                .dateAdded(LocalDateTime.now().minusDays(7))
                .priority(10)
                .tmdbId(1396)
                .tmdbId(1396)
                .build();

        movie1 = movieRepository.save(movie1);
        movie2 = movieRepository.save(movie2);
        series1 = seriesRepository.save(series1);
    }

    @Test
    void shouldGetCombinedCatalogWithAllFields() throws IOException, JSONException {
        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl,
                CatalogItemResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(3);

        assertJsonResponseMatches(response.getBody(), "combined-catalog-expected.json");
    }

    @Test
    void shouldFilterByMovieContentType() throws IOException, JSONException {
        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl + "?contentType=MOVIE",
                CatalogItemResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(Arrays.stream(response.getBody()).allMatch(item -> item.getContentType() == ContentType.MOVIE)).isTrue();

        assertJsonResponseMatches(response.getBody(), "filter-by-movie-type-expected.json");
    }

    @Test
    void shouldFilterBySeriesContentType() throws IOException, JSONException {
        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl + "?contentType=SERIES",
                CatalogItemResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getContentType()).isEqualTo(ContentType.SERIES);

        assertJsonResponseMatches(response.getBody(), "filter-by-series-type-expected.json");
    }

    @Test
    void shouldFilterByGenre() throws IOException, JSONException {
        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl + "?genre=Crime",
                CatalogItemResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getTitle()).isEqualTo("Breaking Bad");
        assertThat(response.getBody()[0].getGenres()).contains("Crime");

        assertJsonResponseMatches(response.getBody(), "filter-by-genre-expected.json");
    }

    @Test
    void shouldFilterByUnwatchedStatus() throws IOException, JSONException {
        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl + "?watchStatus=UNWATCHED",
                CatalogItemResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(Arrays.stream(response.getBody()).allMatch(item -> item.getWatchStatus() == WatchStatus.UNWATCHED)).isTrue();

        assertJsonResponseMatches(response.getBody(), "filter-by-unwatched-expected.json");
    }

    @Test
    void shouldFilterByWatchedStatus() {
        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl + "?watchStatus=WATCHED",
                CatalogItemResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getTitle()).isEqualTo("The Matrix");
        assertThat(response.getBody()[0].getWatchStatus()).isEqualTo(WatchStatus.WATCHED);
    }

    @Test
    void shouldFilterByAddedBy() throws IOException, JSONException {
        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl + "?addedBy=Alice",
                CatalogItemResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(Arrays.stream(response.getBody()).allMatch(item -> "Alice".equals(item.getAddedBy()))).isTrue();

        assertJsonResponseMatches(response.getBody(), "filter-by-addedby-expected.json");
    }

    @Test
    void shouldFilterByHasNewSeasons() throws IOException, JSONException {
        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl + "?contentType=SERIES&hasNewSeasons=true",
                CatalogItemResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getTitle()).isEqualTo("Breaking Bad");
        assertThat(response.getBody()[0].getHasNewSeasons()).isTrue();

        assertJsonResponseMatches(response.getBody(), "filter-by-new-seasons-expected.json");
    }

    @Test
    void shouldFilterBySeriesStatusComplete() {
        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl + "?contentType=SERIES&seriesStatus=COMPLETE",
                CatalogItemResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getTitle()).isEqualTo("Breaking Bad");
        assertThat(response.getBody()[0].getSeriesStatus()).isEqualTo("COMPLETE");
    }

    @Test
    void shouldSortByTitle() throws IOException, JSONException {
        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl + "?sortBy=title",
                CatalogItemResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(3);
        assertThat(response.getBody()[0].getTitle()).isEqualTo("Breaking Bad");
        assertThat(response.getBody()[1].getTitle()).isEqualTo("Inception");
        assertThat(response.getBody()[2].getTitle()).isEqualTo("The Matrix");

        assertJsonResponseMatches(response.getBody(), "sort-by-title-expected.json");
    }

    @Test
    void shouldSortByDateAdded() {
        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl + "?sortBy=dateAdded",
                CatalogItemResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(3);
    }

    @Test
    void shouldSortByLength() {
        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl + "?sortBy=length",
                CatalogItemResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(3);
    }

    @Test
    void shouldSearchByTitle() throws IOException, JSONException {
        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl + "/search?query=inception",
                CatalogItemResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getTitle()).isEqualTo("Inception");

        assertJsonResponseMatches(response.getBody(), "search-by-title-expected.json");
    }

    @Test
    void shouldSearchByComment() throws IOException, JSONException {
        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl + "/search?query=kingpin",
                CatalogItemResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getTitle()).isEqualTo("Breaking Bad");

        assertJsonResponseMatches(response.getBody(), "search-by-comment-expected.json");
    }

    @Test
    void shouldSearchByGenre() {
        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl + "/search?query=thriller",
                CatalogItemResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void shouldApplyMultipleFilters() throws IOException, JSONException {
        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl + "?contentType=MOVIE&watchStatus=UNWATCHED&addedBy=Alice",
                CatalogItemResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getTitle()).isEqualTo("Inception");

        assertJsonResponseMatches(response.getBody(), "multiple-filters-expected.json");
    }

    @Test
    void shouldReturnEmptyListWhenNoMatches() {
        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl + "?genre=NonExistentGenre",
                CatalogItemResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void shouldSearchCaseInsensitive() {
        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl + "/search?query=INCEPTION",
                CatalogItemResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getTitle()).isEqualTo("Inception");
    }

    @Test
    void shouldReturnEmptyListForEmptyDatabase() {
        movieRepository.deleteAll();
        seriesRepository.deleteAll();

        ResponseEntity<CatalogItemResponse[]> response = restTemplate.getForEntity(
                catalogUrl,
                CatalogItemResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    private String loadExpectedJson(String filename, Map<String, String> placeholders) throws IOException {
        ClassPathResource resource = new ClassPathResource("catalog-tests/" + filename);
        String content = new String(resource.getInputStream().readAllBytes());
        
        // Replace placeholders with actual values
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            content = content.replace(entry.getKey(), entry.getValue());
        }
        
        return content;
    }

    private void assertJsonResponseMatches(CatalogItemResponse[] actual, String expectedJsonFile) throws IOException, JSONException {
        String actualJson = objectMapper.writeValueAsString(actual);
        
        // Prepare placeholders with actual values from response (not from test entities)
        // This ensures date formats match exactly as serialized by Jackson
        Map<String, String> placeholders = new HashMap<>();
        for (CatalogItemResponse item : actual) {
            if (item.getId().equals(movie1.getId())) {
                placeholders.put("${movie1.id}", item.getId());
                placeholders.put("${movie1.dateAdded}", objectMapper.writeValueAsString(item.getDateAdded()).replace("\"", ""));
            } else if (item.getId().equals(movie2.getId())) {
                placeholders.put("${movie2.id}", item.getId());
                placeholders.put("${movie2.dateAdded}", objectMapper.writeValueAsString(item.getDateAdded()).replace("\"", ""));
            } else if (item.getId().equals(series1.getId())) {
                placeholders.put("${series1.id}", item.getId());
                placeholders.put("${series1.dateAdded}", objectMapper.writeValueAsString(item.getDateAdded()).replace("\"", ""));
            }
        }
        
        String expectedJson = loadExpectedJson(expectedJsonFile, placeholders);
        
        // Use JSONAssert for flexible JSON comparison
        // NON_EXTENSIBLE mode ignores array order
        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.NON_EXTENSIBLE);
    }
}
