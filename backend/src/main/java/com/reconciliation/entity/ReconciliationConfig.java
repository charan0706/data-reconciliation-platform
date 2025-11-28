package com.reconciliation.entity;

import com.reconciliation.enums.ScheduleFrequency;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for a reconciliation job between source and target systems.
 */
@Entity
@Table(name = "reconciliation_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconciliationConfig extends BaseEntity {

    @Column(name = "config_code", unique = true, nullable = false, length = 50)
    private String configCode;

    @Column(name = "config_name", nullable = false, length = 200)
    private String configName;

    @Column(name = "description", length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_system_id", nullable = false)
    private SourceSystem sourceSystem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_system_id", nullable = false)
    private SourceSystem targetSystem;

    @Column(name = "source_query", columnDefinition = "CLOB")
    private String sourceQuery;

    @Column(name = "target_query", columnDefinition = "CLOB")
    private String targetQuery;

    @Column(name = "source_file_pattern", length = 500)
    private String sourceFilePattern;

    @Column(name = "target_file_pattern", length = 500)
    private String targetFilePattern;

    @OneToMany(mappedBy = "reconciliationConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AttributeMapping> attributeMappings = new ArrayList<>();

    @Column(name = "primary_key_attributes", length = 500)
    private String primaryKeyAttributes;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_frequency", length = 50)
    private ScheduleFrequency scheduleFrequency;

    @Column(name = "cron_expression", length = 100)
    private String cronExpression;

    @Column(name = "is_scheduled")
    private Boolean isScheduled = false;

    @Column(name = "schedule_enabled")
    private Boolean scheduleEnabled = true;

    @Column(name = "tolerance_percentage")
    private Double tolerancePercentage;

    @Column(name = "date_tolerance_minutes")
    private Integer dateToleranceMinutes;

    @Column(name = "ignore_case")
    private Boolean ignoreCase = false;

    @Column(name = "trim_whitespace")
    private Boolean trimWhitespace = true;

    @Column(name = "null_equals_empty")
    private Boolean nullEqualsEmpty = false;

    @Column(name = "max_discrepancies")
    private Integer maxDiscrepancies = 10000;

    @Column(name = "batch_size")
    private Integer batchSize = 1000;

    @Column(name = "notification_emails", length = 1000)
    private String notificationEmails;

    @Column(name = "auto_create_incidents")
    private Boolean autoCreateIncidents = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "reconciliationConfig", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ReconciliationRun> runs = new ArrayList<>();

    public void addAttributeMapping(AttributeMapping mapping) {
        attributeMappings.add(mapping);
        mapping.setReconciliationConfig(this);
    }

    public void removeAttributeMapping(AttributeMapping mapping) {
        attributeMappings.remove(mapping);
        mapping.setReconciliationConfig(null);
    }
}

