package com.reconciliation.enums;

/**
 * Status for incidents created from discrepancies.
 * Implements Maker-Checker workflow.
 */
public enum IncidentStatus {
    OPEN,                   // Incident created, awaiting assignment
    ASSIGNED,               // Assigned to analyst
    UNDER_INVESTIGATION,    // Being investigated
    PENDING_MAKER_ACTION,   // Waiting for maker to propose resolution
    PENDING_CHECKER_REVIEW, // Waiting for checker approval
    CHECKER_REJECTED,       // Checker rejected the resolution
    RESOLVED,               // Resolved and approved
    CLOSED,                 // Closed after resolution
    ESCALATED,              // Escalated to higher authority
    CANCELLED               // Cancelled/false positive
}

