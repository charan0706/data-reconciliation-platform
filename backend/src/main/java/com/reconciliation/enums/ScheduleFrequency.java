package com.reconciliation.enums;

/**
 * Frequency options for scheduled reconciliations.
 */
public enum ScheduleFrequency {
    ON_DEMAND,      // Manual trigger only
    HOURLY,         // Every hour
    DAILY,          // Once per day
    WEEKLY,         // Once per week
    MONTHLY,        // Once per month
    QUARTERLY,      // Once per quarter
    CUSTOM_CRON     // Custom cron expression
}

