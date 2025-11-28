package com.reconciliation.dto;

import com.reconciliation.enums.DiscrepancySeverity;
import com.reconciliation.enums.IncidentStatus;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentDTO {
    
    private Long id;
    private String incidentNumber;
    private String title;
    private String description;
    private IncidentStatus status;
    private DiscrepancySeverity severity;
    
    private Long reconciliationRunId;
    private String runId;
    private Long reconciliationConfigId;
    private String configCode;
    private String configName;
    
    private Integer discrepancyCount;
    private Long affectedRecords;
    
    private Long assignedToId;
    private String assignedToName;
    private Long makerId;
    private String makerName;
    private Long checkerId;
    private String checkerName;
    
    private String assignedAt;
    private String investigationStartedAt;
    private String resolutionProposedAt;
    private String resolutionApprovedAt;
    private String closedAt;
    private String dueDate;
    private Boolean slaBreach;
    
    private String rootCause;
    private String proposedResolution;
    private String resolutionNotes;
    private String checkerComments;
    private String rejectionReason;
    private Integer rejectionCount;
    
    private Integer escalationLevel;
    private String escalatedAt;
    private String escalatedTo;
    
    private List<IncidentCommentDTO> comments;
    private List<IncidentHistoryDTO> history;
    private List<DiscrepancyDTO> discrepancies;
    
    private String createdAt;
    private String updatedAt;
    private String createdBy;
}

