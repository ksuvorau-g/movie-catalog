package com.moviecat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating priority.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriorityRequest {
    
    private Integer priority;
}
