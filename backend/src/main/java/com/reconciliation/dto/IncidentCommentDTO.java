package com.reconciliation.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentCommentDTO {
    private Long id;
    private Long incidentId;
    private String commentText;
    private Long userId;
    private String userName;
    private Boolean isInternal;
    private String attachmentPath;
    private String createdAt;
}

