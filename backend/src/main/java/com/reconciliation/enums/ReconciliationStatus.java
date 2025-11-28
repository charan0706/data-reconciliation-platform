package com.reconciliation.enums;

/**
 * Status of a reconciliation run.
 */
public enum ReconciliationStatus {
    PENDING,
    IN_PROGRESS,
    EXTRACTING_SOURCE,
    EXTRACTING_TARGET,
    COMPARING,
    GENERATING_REPORT,
    COMPLETED,
    COMPLETED_WITH_DISCREPANCIES,
    FAILED,
    CANCELLED
}

