package com.reconciliation.dto;

import com.reconciliation.enums.DiscrepancySeverity;
import com.reconciliation.enums.DiscrepancyType;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscrepancyDTO {
    
    private Long id;
    private Long runId;
    private String discrepancyCode;
    private DiscrepancyType discrepancyType;
    private DiscrepancySeverity severity;
    private String recordKey;
    private String attributeName;
    private String sourceValue;
    private String targetValue;
    private String expectedValue;
    private String actualValue;
    private Double differenceAmount;
    private Double differencePercentage;
    private String sourceRecordJson;
    private String targetRecordJson;
    private String description;
    private String businessImpact;
    private Boolean isAcknowledged;
    private String acknowledgedBy;
    private Boolean isFalsePositive;
    private String falsePositiveReason;
    private Long incidentId;
    private String incidentNumber;
    private Long rowNumber;
    private Integer batchNumber;
    private String createdAt;
}

