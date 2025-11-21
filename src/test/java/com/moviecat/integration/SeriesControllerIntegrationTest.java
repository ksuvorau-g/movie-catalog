package com.moviecat.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import com.moviecat.dto.PriorityRequest;
import com.moviecat.dto.SeriesRequest;
import com.moviecat.dto.SeriesResponse;
import com.moviecat.dto.WatchStatusRequest;
import com.moviecat.model.Season;
import com.moviecat.model.Series;
import com.moviecat.model.SeriesStatus;
import com.moviecat.model.WatchStatus;
import com.moviecat.repository.SeriesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
 * Comprehensive integration tests for SeriesController with JSON comparison.
 * Tests cover all CRUD operations, season management, status updates, and priority management.
 */
class SeriesControllerIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SeriesRepository seriesRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String seriesUrl;
    private Series testSeries1;
    private Series testSeries2;

    @BeforeEach
    void setUp() {
        seriesUrl = "http://localhost:" + port + "/api/series";
        seriesRepository.deleteAll();
        createTestData();
    }

    private void createTestData() {
        testSeries1 = Series.builder()
                .title("Game of Thrones")
                .link("https://example.com/got")
                .coverImage("/api/images/got")
                .comment("Epic fantasy series")
                .genres(Arrays.asList("Fantasy", "Drama", "Adventure"))
                .seasons(Arrays.asList(
                        Season.builder().seasonNumber(1).watchStatus(WatchStatus.WATCHED).build(),
                        Season.builder().seasonNumber(2).watchStatus(WatchStatus.UNWATCHED).build(),
                        Season.builder().seasonNumber(3).watchStatus(WatchStatus.UNWATCHED).build()
                ))
                .watchStatus(WatchStatus.UNWATCHED)
                .totalAvailableSeasons(8)
                .hasNewSeasons(true)
                .seriesStatus(SeriesStatus.COMPLETE)
                .addedBy("TestUser")
                .dateAdded(LocalDateTime.now().minusDays(30))
                .priority(15)
                .tmdbId(1399)
                .build();

        testSeries2 = Series.builder()
                .title("The Wire")
                .link("https://example.com/wire")
                .coverImage("/api/images/wire")
                .comment("Crime drama in Baltimore")
                .genres(Arrays.asList("Crime", "Drama"))
                .seasons(Arrays.asList(
                        Season.builder().seasonNumber(1).watchStatus(WatchStatus.WATCHED).build(),
                        Season.builder().seasonNumber(2).watchStatus(WatchStatus.WATCHED).build(),
                        Season.builder().seasonNumber(3).watchStatus(WatchStatus.WATCHED).build(),
                        Season.builder().seasonNumber(4).watchStatus(WatchStatus.WATCHED).build(),
                        Season.builder().seasonNumber(5).watchStatus(WatchStatus.WATCHED).build()
                ))
                .watchStatus(WatchStatus.WATCHED)
                .totalAvailableSeasons(5)
                .hasNewSeasons(false)
                .seriesStatus(SeriesStatus.COMPLETE)
                .addedBy("TestUser")
                .dateAdded(LocalDateTime.now().minusDays(60))
                .priority(10)
                .tmdbId(1438)
                .build();

        testSeries1 = seriesRepository.save(testSeries1);
        testSeries2 = seriesRepository.save(testSeries2);
    }

    @Test
    void shouldCreateSeriesWithAllFields() throws IOException, JSONException {
        SeriesRequest request = SeriesRequest.builder()
                .title("Stranger Things")
                .link("https://example.com/stranger-things")
                .coverImage("/api/images/stranger-things")
                .comment("Sci-fi horror set in the 1980s")
                .genres(Arrays.asList("Sci-Fi", "Horror", "Drama"))
                .seasons(Arrays.asList(
                        Season.builder().seasonNumber(1).watchStatus(WatchStatus.UNWATCHED).build(),
                        Season.builder().seasonNumber(2).watchStatus(WatchStatus.UNWATCHED).build()
                ))
                .addedBy("TestUser")
                .priority(5)
                .build();

        ResponseEntity<SeriesResponse> response = restTemplate.postForEntity(
                seriesUrl,
                request,
                SeriesResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertJsonResponseMatches(response.getBody(), "series-tests/create-series-expected.json");
    }

    @Test
    void shouldCreateSeriesWithMinimalFields() throws IOException, JSONException {
        SeriesRequest request = SeriesRequest.builder()
                .title("Minimal Series")
                .addedBy("TestUser")
                .build();

        ResponseEntity<SeriesResponse> response = restTemplate.postForEntity(
                seriesUrl,
                request,
                SeriesResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        
        assertJsonResponseMatches(response.getBody(), "series-tests/create-minimal-series-expected.json");
    }

    @Test
    void shouldGetSeriesById() throws IOException, JSONException {
        ResponseEntity<SeriesResponse> response = restTemplate.getForEntity(
                seriesUrl + "/" + testSeries1.getId(),
                SeriesResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertJsonResponseMatches(response.getBody(), "series-tests/get-series-expected.json");
    }

    @Test
    void shouldReturn500WhenSeriesNotFound() {
        // Note: Service throws RuntimeException for not found, resulting in 500
        // This should ideally return 404, but testing actual behavior
        ResponseEntity<String> response = restTemplate.getForEntity(
                seriesUrl + "/nonexistent-id",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldGetAllSeries() {
        ResponseEntity<SeriesResponse[]> response = restTemplate.getForEntity(
                seriesUrl,
                SeriesResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);

        List<String> titles = Arrays.stream(response.getBody())
                .map(SeriesResponse::getTitle)
                .toList();
        assertThat(titles).containsExactlyInAnyOrder("Game of Thrones", "The Wire");
    }

    @Test
    void shouldUpdateSeriesCompletely() throws IOException, JSONException {
        SeriesRequest updateRequest = SeriesRequest.builder()
                .title("Game of Thrones - Updated")
                .link("https://example.com/got-updated")
                .coverImage("/api/images/got-new")
                .comment("Updated: Epic fantasy series with dragons")
                .genres(Arrays.asList("Fantasy", "Drama", "Adventure", "Action"))
                .seasons(Arrays.asList(
                        Season.builder().seasonNumber(1).watchStatus(WatchStatus.UNWATCHED).build(),
                        Season.builder().seasonNumber(2).watchStatus(WatchStatus.UNWATCHED).build()
                ))
                .addedBy("UpdatedUser")
                .priority(20)
                .build();

        HttpEntity<SeriesRequest> request = new HttpEntity<>(updateRequest);
        ResponseEntity<SeriesResponse> response = restTemplate.exchange(
                seriesUrl + "/" + testSeries1.getId(),
                HttpMethod.PUT,
                request,
                SeriesResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertJsonResponseMatches(response.getBody(), "series-tests/update-series-expected.json");
    }

    @Test
    void shouldDeleteSeries() {
        ResponseEntity<Void> response = restTemplate.exchange(
                seriesUrl + "/" + testSeries1.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(seriesRepository.findById(testSeries1.getId())).isEmpty();
        assertThat(seriesRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldUpdateSeasonWatchStatus() throws IOException, JSONException {
        WatchStatusRequest request = new WatchStatusRequest();
        request.setWatchStatus(WatchStatus.WATCHED);

        HttpEntity<WatchStatusRequest> entity = new HttpEntity<>(request);
        ResponseEntity<SeriesResponse> response = restTemplate.exchange(
                seriesUrl + "/" + testSeries1.getId() + "/seasons/2/watch-status",
                HttpMethod.PATCH,
                entity,
                SeriesResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(testSeries1.getId());
        assertThat(response.getBody().getSeasons()).hasSize(3);
        
        Season season2 = response.getBody().getSeasons().stream()
                .filter(s -> s.getSeasonNumber().equals(2))
                .findFirst()
                .orElse(null);
        assertThat(season2).isNotNull();
        assertThat(season2.getWatchStatus()).isEqualTo(WatchStatus.WATCHED);
        assertThat(response.getBody().getWatchStatus()).isEqualTo(WatchStatus.UNWATCHED);

        assertJsonResponseMatches(response.getBody(), "series-tests/update-season-watch-status-expected.json");
    }

    @Test
    void shouldUpdateSeasonToUnwatched() {
        WatchStatusRequest request = new WatchStatusRequest();
        request.setWatchStatus(WatchStatus.UNWATCHED);

        HttpEntity<WatchStatusRequest> entity = new HttpEntity<>(request);
        ResponseEntity<SeriesResponse> response = restTemplate.exchange(
                seriesUrl + "/" + testSeries2.getId() + "/seasons/1/watch-status",
                HttpMethod.PATCH,
                entity,
                SeriesResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        Season season1 = response.getBody().getSeasons().stream()
                .filter(s -> s.getSeasonNumber().equals(1))
                .findFirst()
                .orElse(null);
        assertThat(season1).isNotNull();
        assertThat(season1.getWatchStatus()).isEqualTo(WatchStatus.UNWATCHED);
        assertThat(response.getBody().getWatchStatus()).isEqualTo(WatchStatus.UNWATCHED);
    }

    @Test
    void shouldUpdateSeriesWatchStatusToWatched() throws IOException, JSONException {
        WatchStatusRequest request = new WatchStatusRequest();
        request.setWatchStatus(WatchStatus.WATCHED);

        HttpEntity<WatchStatusRequest> entity = new HttpEntity<>(request);
        ResponseEntity<SeriesResponse> response = restTemplate.exchange(
                seriesUrl + "/" + testSeries1.getId() + "/watch-status",
                HttpMethod.PATCH,
                entity,
                SeriesResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(testSeries1.getId());
        assertThat(response.getBody().getWatchStatus()).isEqualTo(WatchStatus.WATCHED);
        assertThat(response.getBody().getHasNewSeasons()).isFalse();
        
        // All seasons should be marked as watched
        assertThat(response.getBody().getSeasons()).allSatisfy(s -> 
            assertThat(s.getWatchStatus()).isEqualTo(WatchStatus.WATCHED)
        );

        assertJsonResponseMatches(response.getBody(), "series-tests/update-series-watch-status-expected.json");
    }

    @Test
    void shouldUpdateSeriesWatchStatusToUnwatched() {
        WatchStatusRequest request = new WatchStatusRequest();
        request.setWatchStatus(WatchStatus.UNWATCHED);

        HttpEntity<WatchStatusRequest> entity = new HttpEntity<>(request);
        ResponseEntity<SeriesResponse> response = restTemplate.exchange(
                seriesUrl + "/" + testSeries2.getId() + "/watch-status",
                HttpMethod.PATCH,
                entity,
                SeriesResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getWatchStatus()).isEqualTo(WatchStatus.UNWATCHED);
        
        // All seasons should be marked as unwatched
        assertThat(response.getBody().getSeasons()).allSatisfy(s -> 
            assertThat(s.getWatchStatus()).isEqualTo(WatchStatus.UNWATCHED)
        );
    }

    @Test
    void shouldAddSeason() throws IOException, JSONException {
        ResponseEntity<SeriesResponse> response = restTemplate.postForEntity(
                seriesUrl + "/" + testSeries1.getId() + "/seasons/increase",
                null,
                SeriesResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSeasons()).hasSize(4);
        
        Season newSeason = response.getBody().getSeasons().stream()
                .filter(s -> s.getSeasonNumber().equals(4))
                .findFirst()
                .orElse(null);
        assertThat(newSeason).isNotNull();
        assertThat(newSeason.getWatchStatus()).isEqualTo(WatchStatus.UNWATCHED);

        assertJsonResponseMatches(response.getBody(), "series-tests/add-season-expected.json");
    }

    @Test
    void shouldRemoveLastSeason() throws IOException, JSONException {
        ResponseEntity<SeriesResponse> response = restTemplate.postForEntity(
                seriesUrl + "/" + testSeries1.getId() + "/seasons/decrease",
                null,
                SeriesResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSeasons()).hasSize(2);
        
        boolean hasSeason3 = response.getBody().getSeasons().stream()
                .anyMatch(s -> s.getSeasonNumber().equals(3));
        assertThat(hasSeason3).isFalse();

        assertJsonResponseMatches(response.getBody(), "series-tests/remove-season-expected.json");
    }

    @Test
    void shouldNotRemoveLastSeasonWhenOnlyOneRemains() {
        // Create series with only one season
        Series singleSeasonSeries = Series.builder()
                .title("Mini Series")
                .seasons(Arrays.asList(
                        Season.builder().seasonNumber(1).watchStatus(WatchStatus.UNWATCHED).build()
                ))
                .watchStatus(WatchStatus.UNWATCHED)
                .addedBy("TestUser")
                .build();
        singleSeasonSeries = seriesRepository.save(singleSeasonSeries);

        ResponseEntity<String> response = restTemplate.postForEntity(
                seriesUrl + "/" + singleSeasonSeries.getId() + "/seasons/decrease",
                null,
                String.class
        );

        // Service throws IllegalStateException, results in 500
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldUpdatePriority() throws IOException, JSONException {
        PriorityRequest request = new PriorityRequest();
        request.setPriority(25);

        HttpEntity<PriorityRequest> entity = new HttpEntity<>(request);
        ResponseEntity<SeriesResponse> response = restTemplate.exchange(
                seriesUrl + "/" + testSeries1.getId() + "/priority",
                HttpMethod.PATCH,
                entity,
                SeriesResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(testSeries1.getId());
        assertThat(response.getBody().getPriority()).isEqualTo(25);
        assertThat(response.getBody().getTitle()).isEqualTo("Game of Thrones");

        assertJsonResponseMatches(response.getBody(), "series-tests/update-priority-expected.json");
    }

    @Test
    void shouldUpdatePriorityToZero() {
        PriorityRequest request = new PriorityRequest();
        request.setPriority(0);

        HttpEntity<PriorityRequest> entity = new HttpEntity<>(request);
        ResponseEntity<SeriesResponse> response = restTemplate.exchange(
                seriesUrl + "/" + testSeries1.getId() + "/priority",
                HttpMethod.PATCH,
                entity,
                SeriesResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPriority()).isEqualTo(0);
    }

    @Test
    void shouldFailRefreshSeasonsWithoutTmdbId() {
        // Test series1 has tmdbId but no valid link pattern
        // Without proper external API setup, this will fail
        ResponseEntity<String> response = restTemplate.postForEntity(
                seriesUrl + "/" + testSeries1.getId() + "/refresh",
                null,
                String.class
        );

        // Service requires valid TMDB link or throws exception
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldHandleMultipleUpdatesInSequence() {
        // Add a season
        restTemplate.postForEntity(
                seriesUrl + "/" + testSeries1.getId() + "/seasons/increase",
                null,
                SeriesResponse.class
        );

        // Update priority
        PriorityRequest priorityRequest = new PriorityRequest();
        priorityRequest.setPriority(30);
        HttpEntity<PriorityRequest> priorityEntity = new HttpEntity<>(priorityRequest);
        restTemplate.exchange(
                seriesUrl + "/" + testSeries1.getId() + "/priority",
                HttpMethod.PATCH,
                priorityEntity,
                SeriesResponse.class
        );

        // Mark series as watched
        WatchStatusRequest watchRequest = new WatchStatusRequest();
        watchRequest.setWatchStatus(WatchStatus.WATCHED);
        HttpEntity<WatchStatusRequest> watchEntity = new HttpEntity<>(watchRequest);
        restTemplate.exchange(
                seriesUrl + "/" + testSeries1.getId() + "/watch-status",
                HttpMethod.PATCH,
                watchEntity,
                SeriesResponse.class
        );

        // Get final state
        ResponseEntity<SeriesResponse> response = restTemplate.getForEntity(
                seriesUrl + "/" + testSeries1.getId(),
                SeriesResponse.class
        );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSeasons()).hasSize(4);
        assertThat(response.getBody().getPriority()).isEqualTo(30);
        assertThat(response.getBody().getWatchStatus()).isEqualTo(WatchStatus.WATCHED);
        assertThat(response.getBody().getHasNewSeasons()).isFalse();
    }

    @Test
    void shouldReturnEmptyListWhenNoSeries() {
        seriesRepository.deleteAll();

        ResponseEntity<SeriesResponse[]> response = restTemplate.getForEntity(
                seriesUrl,
                SeriesResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void shouldCalculateSeriesWatchStatusAutomatically() {
        // Create series with mixed season statuses
        SeriesRequest request = SeriesRequest.builder()
                .title("Test Series")
                .seasons(Arrays.asList(
                        Season.builder().seasonNumber(1).watchStatus(WatchStatus.WATCHED).build(),
                        Season.builder().seasonNumber(2).watchStatus(WatchStatus.UNWATCHED).build()
                ))
                .addedBy("TestUser")
                .build();

        ResponseEntity<SeriesResponse> response = restTemplate.postForEntity(
                seriesUrl,
                request,
                SeriesResponse.class
        );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getWatchStatus()).isEqualTo(WatchStatus.UNWATCHED);
    }

    @Test
    void shouldMarkSeriesAsWatchedWhenAllSeasonsWatched() {
        // Create series with all seasons watched
        SeriesRequest request = SeriesRequest.builder()
                .title("Complete Series")
                .seasons(Arrays.asList(
                        Season.builder().seasonNumber(1).watchStatus(WatchStatus.WATCHED).build(),
                        Season.builder().seasonNumber(2).watchStatus(WatchStatus.WATCHED).build()
                ))
                .addedBy("TestUser")
                .build();

        ResponseEntity<SeriesResponse> response = restTemplate.postForEntity(
                seriesUrl,
                request,
                SeriesResponse.class
        );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getWatchStatus()).isEqualTo(WatchStatus.WATCHED);
    }

    @Test
    void shouldResetHasNewSeasonsWhenAllSeasonsWatched() {
        // Start with series that has new seasons
        testSeries1.setHasNewSeasons(true);
        seriesRepository.save(testSeries1);

        // Mark all seasons as watched
        WatchStatusRequest request = new WatchStatusRequest();
        request.setWatchStatus(WatchStatus.WATCHED);

        HttpEntity<WatchStatusRequest> entity = new HttpEntity<>(request);
        ResponseEntity<SeriesResponse> response = restTemplate.exchange(
                seriesUrl + "/" + testSeries1.getId() + "/watch-status",
                HttpMethod.PATCH,
                entity,
                SeriesResponse.class
        );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getHasNewSeasons()).isFalse();
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

    private void assertJsonResponseMatches(SeriesResponse actual, String expectedJsonFile) throws IOException, JSONException {
        String actualJson = objectMapper.writeValueAsString(actual);
        
        // Prepare placeholders with actual values from response
        // Use Jackson to serialize dates for consistency with actual JSON format
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("${series.id}", actual.getId());
        placeholders.put("${series.dateAdded}", objectMapper.writeValueAsString(actual.getDateAdded()).replace("\"", ""));
        
        String expectedJson = loadExpectedJson(expectedJsonFile, placeholders);
        
        // Use JSONAssert for flexible JSON comparison
        // NON_EXTENSIBLE mode ignores array order
        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.NON_EXTENSIBLE);
    }
}
