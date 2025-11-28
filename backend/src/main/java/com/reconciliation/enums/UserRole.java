package com.reconciliation.enums;

/**
 * User roles for the reconciliation platform.
 */
public enum UserRole {
    ADMIN,              // Full system access
    DATA_OWNER,         // Owns data systems, can configure reconciliations
    RISK_ANALYST,       // Investigates discrepancies
    MAKER,              // Proposes resolutions
    CHECKER,            // Approves resolutions
    VIEWER,             // Read-only access
    SYSTEM              // System/service account
}

