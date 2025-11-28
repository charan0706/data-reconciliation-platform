package com.reconciliation.entity;

import com.reconciliation.enums.DiscrepancySeverity;
import com.reconciliation.enums.DiscrepancyType;
import lombok.*;

import javax.persistence.*;

/**
 * Represents a discrepancy found during reconciliation.
 */
@Entity
@Table(name = "discrepancies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Discrepancy extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reconciliation_run_id", nullable = false)
    private ReconciliationRun reconciliationRun;

    @Column(name = "discrepancy_code", nullable = false, length = 50)
    private String discrepancyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "discrepancy_type", nullable = false, length = 50)
    private DiscrepancyType discrepancyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private DiscrepancySeverity severity;

    @Column(name = "record_key", nullable = false, length = 500)
    private String recordKey;

    @Column(name = "attribute_name", length = 200)
    private String attributeName;

    @Column(name = "source_value", length = 4000)
    private String sourceValue;

    @Column(name = "target_value", length = 4000)
    private String targetValue;

    @Column(name = "expected_value", length = 4000)
    private String expectedValue;

    @Column(name = "actual_value", length = 4000)
    private String actualValue;

    @Column(name = "difference_amount")
    private Double differenceAmount;

    @Column(name = "difference_percentage")
    private Double differencePercentage;

    @Column(name = "source_record_json", columnDefinition = "CLOB")
    private String sourceRecordJson;

    @Column(name = "target_record_json", columnDefinition = "CLOB")
    private String targetRecordJson;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "business_impact", length = 1000)
    private String businessImpact;

    @Column(name = "is_acknowledged")
    private Boolean isAcknowledged = false;

    @Column(name = "acknowledged_by", length = 100)
    private String acknowledgedBy;

    @Column(name = "is_false_positive")
    private Boolean isFalsePositive = false;

    @Column(name = "false_positive_reason", length = 1000)
    private String falsePositiveReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id")
    private Incident incident;

    @Column(name = "row_number")
    private Long rowNumber;

    @Column(name = "batch_number")
    private Integer batchNumber;
}

