package com.moviecat.controller;

import com.moviecat.dto.RecommendationResponse;
import com.moviecat.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for recommendation operations.
 */
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendations", description = "Movie/series recommendation endpoints")
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    
    @GetMapping
    @Operation(summary = "Get recommendation", description = "Get next recommended movie or series to watch")
    public RecommendationResponse getRecommendation() {
        return recommendationService.getRecommendation();
    }
}
