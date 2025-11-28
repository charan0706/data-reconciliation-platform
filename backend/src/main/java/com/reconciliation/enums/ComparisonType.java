package com.reconciliation.enums;

/**
 * Types of comparison operations for data reconciliation.
 */
public enum ComparisonType {
    EXACT_MATCH,            // Values must match exactly
    CASE_INSENSITIVE,       // String comparison ignoring case
    NUMERIC_TOLERANCE,      // Numeric comparison with tolerance
    DATE_TOLERANCE,         // Date comparison with tolerance
    CONTAINS,               // Source contains target or vice versa
    REGEX_MATCH,            // Regular expression match
    CUSTOM_EXPRESSION,      // Custom SpEL or script expression
    IGNORE                  // Skip this attribute in comparison
}

