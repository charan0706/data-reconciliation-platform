package com.reconciliation.dto;

import com.reconciliation.enums.IncidentStatus;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentHistoryDTO {
    private Long id;
    private Long incidentId;
    private IncidentStatus fromStatus;
    private IncidentStatus toStatus;
    private String action;
    private String actionBy;
    private String comments;
    private String createdAt;
}

