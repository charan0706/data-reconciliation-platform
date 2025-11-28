package com.reconciliation.entity;

import com.reconciliation.enums.DiscrepancySeverity;
import com.reconciliation.enums.IncidentStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Incident created from discrepancies for investigation and resolution.
 * Implements Maker-Checker workflow.
 */
@Entity
@Table(name = "incidents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident extends BaseEntity {

    @Column(name = "incident_number", unique = true, nullable = false, length = 50)
    private String incidentNumber;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "description", columnDefinition = "CLOB")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private IncidentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private DiscrepancySeverity severity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reconciliation_run_id")
    private ReconciliationRun reconciliationRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reconciliation_config_id")
    private ReconciliationConfig reconciliationConfig;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Discrepancy> discrepancies = new ArrayList<>();

    @Column(name = "discrepancy_count")
    private Integer discrepancyCount = 0;

    @Column(name = "affected_records")
    private Long affectedRecords = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maker_id")
    private User maker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checker_id")
    private User checker;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "investigation_started_at")
    private LocalDateTime investigationStartedAt;

    @Column(name = "resolution_proposed_at")
    private LocalDateTime resolutionProposedAt;

    @Column(name = "resolution_approved_at")
    private LocalDateTime resolutionApprovedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "sla_breach")
    private Boolean slaBreach = false;

    @Column(name = "root_cause", columnDefinition = "CLOB")
    private String rootCause;

    @Column(name = "proposed_resolution", columnDefinition = "CLOB")
    private String proposedResolution;

    @Column(name = "resolution_notes", columnDefinition = "CLOB")
    private String resolutionNotes;

    @Column(name = "checker_comments", columnDefinition = "CLOB")
    private String checkerComments;

    @Column(name = "rejection_reason", length = 2000)
    private String rejectionReason;

    @Column(name = "rejection_count")
    private Integer rejectionCount = 0;

    @Column(name = "escalation_level")
    private Integer escalationLevel = 0;

    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;

    @Column(name = "escalated_to", length = 200)
    private String escalatedTo;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<IncidentComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<IncidentHistory> history = new ArrayList<>();

    public void addComment(IncidentComment comment) {
        comments.add(comment);
        comment.setIncident(this);
    }

    public void addHistory(IncidentHistory historyEntry) {
        history.add(historyEntry);
        historyEntry.setIncident(this);
    }
}

