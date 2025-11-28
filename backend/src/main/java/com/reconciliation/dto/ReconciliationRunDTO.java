package com.reconciliation.dto;

import com.reconciliation.enums.ReconciliationStatus;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconciliationRunDTO {
    
    private Long id;
    private String runId;
    private Long configId;
    private String configCode;
    private String configName;
    private ReconciliationStatus status;
    private String startedAt;
    private String completedAt;
    private String triggeredBy;
    private Boolean isScheduledRun;
    
    private Long sourceRecordCount;
    private Long targetRecordCount;
    private Long matchedRecordCount;
    private Long discrepancyCount;
    private Long missingInSourceCount;
    private Long missingInTargetCount;
    private Long attributeMismatchCount;
    
    private String errorMessage;
    private Long executionTimeMs;
    private Long sourceExtractionTimeMs;
    private Long targetExtractionTimeMs;
    private Long comparisonTimeMs;
    
    private String reportPath;
    private Double matchPercentage;
    
    private List<DiscrepancyDTO> discrepancies;
    private List<RunLogDTO> logs;
    
    // Summary statistics
    private DiscrepancySummaryDTO discrepancySummary;
}

