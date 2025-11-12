package com.moviecat.controller;

import com.moviecat.dto.CatalogItemResponse;
import com.moviecat.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for catalog operations (combined movies and series).
 */
@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
@Tag(name = "Catalog", description = "Combined catalog endpoints for movies and series")
public class CatalogController {
    
    private final CatalogService catalogService;
    
    @GetMapping
    @Operation(summary = "Get catalog", description = "Retrieve combined catalog with optional filters")
    public List<CatalogItemResponse> getCatalog(
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String watchStatus,
            @RequestParam(required = false) String addedBy,
            @RequestParam(required = false) Boolean hasNewSeasons,
            @RequestParam(required = false) String seriesStatus,
            @RequestParam(required = false) String sortBy) {
        return catalogService.getCatalog(contentType, genre, watchStatus, addedBy, hasNewSeasons, seriesStatus, sortBy);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search catalog", description = "Search catalog by title or other attributes")
    public List<CatalogItemResponse> searchCatalog(@RequestParam String query) {
        return catalogService.searchCatalog(query);
    }
}
