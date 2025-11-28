package com.reconciliation.entity;

import com.reconciliation.enums.ReconciliationStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single execution of a reconciliation job.
 */
@Entity
@Table(name = "reconciliation_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconciliationRun extends BaseEntity {

    @Column(name = "run_id", unique = true, nullable = false, length = 50)
    private String runId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reconciliation_config_id", nullable = false)
    private ReconciliationConfig reconciliationConfig;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ReconciliationStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "triggered_by", length = 100)
    private String triggeredBy;

    @Column(name = "is_scheduled_run")
    private Boolean isScheduledRun = false;

    @Column(name = "source_record_count")
    private Long sourceRecordCount = 0L;

    @Column(name = "target_record_count")
    private Long targetRecordCount = 0L;

    @Column(name = "matched_record_count")
    private Long matchedRecordCount = 0L;

    @Column(name = "discrepancy_count")
    private Long discrepancyCount = 0L;

    @Column(name = "missing_in_source_count")
    private Long missingInSourceCount = 0L;

    @Column(name = "missing_in_target_count")
    private Long missingInTargetCount = 0L;

    @Column(name = "attribute_mismatch_count")
    private Long attributeMismatchCount = 0L;

    @Column(name = "error_message", length = 4000)
    private String errorMessage;

    @Column(name = "error_stack_trace", columnDefinition = "CLOB")
    private String errorStackTrace;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "source_extraction_time_ms")
    private Long sourceExtractionTimeMs;

    @Column(name = "target_extraction_time_ms")
    private Long targetExtractionTimeMs;

    @Column(name = "comparison_time_ms")
    private Long comparisonTimeMs;

    @Column(name = "report_path", length = 1000)
    private String reportPath;

    @Column(name = "source_file_path", length = 1000)
    private String sourceFilePath;

    @Column(name = "target_file_path", length = 1000)
    private String targetFilePath;

    @OneToMany(mappedBy = "reconciliationRun", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Discrepancy> discrepancies = new ArrayList<>();

    @OneToMany(mappedBy = "reconciliationRun", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RunLog> runLogs = new ArrayList<>();

    public void addDiscrepancy(Discrepancy discrepancy) {
        discrepancies.add(discrepancy);
        discrepancy.setReconciliationRun(this);
    }

    public void addRunLog(RunLog log) {
        runLogs.add(log);
        log.setReconciliationRun(this);
    }

    public double getMatchPercentage() {
        long total = sourceRecordCount + targetRecordCount;
        if (total == 0) return 100.0;
        return (matchedRecordCount * 2.0 / total) * 100.0;
    }
}

