package com.reconciliation.entity;

import com.reconciliation.enums.IncidentStatus;
import lombok.*;

import javax.persistence.*;

/**
 * Audit history for incident status changes.
 */
@Entity
@Table(name = "incident_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 50)
    private IncidentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 50)
    private IncidentStatus toStatus;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "action_by", length = 100)
    private String actionBy;

    @Column(name = "comments", length = 2000)
    private String comments;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;
}

