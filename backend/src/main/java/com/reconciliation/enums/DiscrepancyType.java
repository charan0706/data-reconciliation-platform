package com.reconciliation.enums;

/**
 * Types of discrepancies that can be identified during reconciliation.
 */
public enum DiscrepancyType {
    MISSING_IN_SOURCE,       // Record exists in target but not in source
    MISSING_IN_TARGET,       // Record exists in source but not in target
    ATTRIBUTE_MISMATCH,      // Record exists in both but attributes differ
    DATA_TYPE_MISMATCH,      // Data type inconsistency
    NULL_VALUE_DIFFERENCE,   // One has null, other has value
    PRECISION_DIFFERENCE,    // Numeric precision difference
    FORMAT_DIFFERENCE,       // Date/string format difference
    DUPLICATE_RECORD,        // Duplicate records found
    REFERENTIAL_INTEGRITY,   // Foreign key relationship broken
    BUSINESS_RULE_VIOLATION  // Custom business rule violated
}

