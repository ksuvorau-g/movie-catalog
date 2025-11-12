package com.moviecat.controller;

import com.moviecat.dto.PriorityRequest;
import com.moviecat.dto.SeriesRequest;
import com.moviecat.dto.SeriesResponse;
import com.moviecat.dto.WatchStatusRequest;
import com.moviecat.service.SeriesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for TV series operations.
 */
@RestController
@RequestMapping("/api/series")
@RequiredArgsConstructor
@Tag(name = "Series", description = "TV series management endpoints")
public class SeriesController {
    
    private final SeriesService seriesService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add new series", description = "Add a new TV series to the catalog")
    public SeriesResponse addSeries(@RequestBody SeriesRequest request) {
        return seriesService.addSeries(request);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get series by ID", description = "Retrieve series details by ID")
    public SeriesResponse getSeries(@PathVariable String id) {
        return seriesService.getSeriesById(id);
    }
    
    @GetMapping
    @Operation(summary = "Get all series", description = "Retrieve all series in the catalog")
    public List<SeriesResponse> getAllSeries() {
        return seriesService.getAllSeries();
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update series", description = "Update series information")
    public SeriesResponse updateSeries(@PathVariable String id, @RequestBody SeriesRequest request) {
        return seriesService.updateSeries(id, request);
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete series", description = "Delete series from catalog")
    public void deleteSeries(@PathVariable String id) {
        seriesService.deleteSeries(id);
    }
    
    @PatchMapping("/{id}/seasons/{seasonNumber}/watch-status")
    @Operation(summary = "Update season watch status", description = "Mark a specific season as watched or unwatched")
    public SeriesResponse updateSeasonWatchStatus(
            @PathVariable String id,
            @PathVariable Integer seasonNumber,
            @RequestBody WatchStatusRequest request) {
        return seriesService.updateSeasonWatchStatus(id, seasonNumber, request.getWatchStatus());
    }
    
    @PatchMapping("/{id}/watch-status")
    @Operation(summary = "Update series watch status", description = "Mark entire series as watched or unwatched (all seasons)")
    public SeriesResponse updateSeriesWatchStatus(@PathVariable String id, @RequestBody WatchStatusRequest request) {
        return seriesService.updateSeriesWatchStatus(id, request.getWatchStatus());
    }
    
    @PatchMapping("/{id}/priority")
    @Operation(summary = "Update priority", description = "Update series recommendation priority")
    public SeriesResponse updatePriority(@PathVariable String id, @RequestBody PriorityRequest request) {
        return seriesService.updatePriority(id, request.getPriority());
    }
    
    @PostMapping("/{id}/refresh")
    @Operation(summary = "Refresh seasons", description = "Manually trigger season refresh from external source")
    public SeriesResponse refreshSeasons(@PathVariable String id) {
        return seriesService.refreshSeasons(id);
    }
}
