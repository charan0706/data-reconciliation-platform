package com.reconciliation.dto;

import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscrepancySummaryDTO {
    private Long totalDiscrepancies;
    private Long missingInSource;
    private Long missingInTarget;
    private Long attributeMismatches;
    private Map<String, Long> byType;
    private Map<String, Long> bySeverity;
    private Map<String, Long> byAttribute;
}

