package com.moviecat.dto;

import com.moviecat.model.WatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating watch status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchStatusRequest {
    
    private WatchStatus watchStatus;
}
