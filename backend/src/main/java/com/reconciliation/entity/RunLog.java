package com.reconciliation.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Detailed logging for reconciliation run steps.
 */
@Entity
@Table(name = "run_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reconciliation_run_id", nullable = false)
    private ReconciliationRun reconciliationRun;

    @Column(name = "log_level", nullable = false, length = 20)
    private String logLevel;

    @Column(name = "step_name", length = 100)
    private String stepName;

    @Column(name = "message", columnDefinition = "CLOB", nullable = false)
    private String message;

    @Column(name = "details", columnDefinition = "CLOB")
    private String details;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "records_processed")
    private Long recordsProcessed;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "stack_trace", columnDefinition = "CLOB")
    private String stackTrace;
}

