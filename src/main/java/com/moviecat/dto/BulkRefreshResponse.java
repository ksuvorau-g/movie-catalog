package com.moviecat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkRefreshResponse {
    private Integer totalProcessed;
    private Integer successCount;
    private Integer failureCount;
    private Integer updatedCount;
}
