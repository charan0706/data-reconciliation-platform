package com.reconciliation.entity;

import lombok.*;

import javax.persistence.*;

/**
 * Comments on incidents during investigation.
 */
@Entity
@Table(name = "incident_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentComment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Column(name = "comment_text", columnDefinition = "CLOB", nullable = false)
    private String commentText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_internal")
    private Boolean isInternal = false;

    @Column(name = "attachment_path", length = 1000)
    private String attachmentPath;
}

