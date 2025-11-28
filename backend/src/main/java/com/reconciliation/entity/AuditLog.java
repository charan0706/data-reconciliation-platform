package com.reconciliation.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * System-wide audit logging for compliance and regulatory review.
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "entity_type", length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "user_role", length = 50)
    private String userRole;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "old_value", columnDefinition = "CLOB")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "CLOB")
    private String newValue;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "module", length = 100)
    private String module;

    @Column(name = "success")
    private Boolean success = true;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @PrePersist
    public void prePersist() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}

