package com.reconciliation.dto;

import com.reconciliation.enums.ScheduleFrequency;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconciliationConfigDTO {
    
    private Long id;
    
    @NotBlank(message = "Config code is required")
    private String configCode;
    
    @NotBlank(message = "Config name is required")
    private String configName;
    
    private String description;
    
    @NotNull(message = "Source system is required")
    private Long sourceSystemId;
    private String sourceSystemName;
    
    @NotNull(message = "Target system is required")
    private Long targetSystemId;
    private String targetSystemName;
    
    private String sourceQuery;
    private String targetQuery;
    private String sourceFilePattern;
    private String targetFilePattern;
    
    private List<AttributeMappingDTO> attributeMappings;
    
    private String primaryKeyAttributes;
    private ScheduleFrequency scheduleFrequency;
    private String cronExpression;
    private Boolean isScheduled;
    private Boolean scheduleEnabled;
    
    private Double tolerancePercentage;
    private Integer dateToleranceMinutes;
    private Boolean ignoreCase;
    private Boolean trimWhitespace;
    private Boolean nullEqualsEmpty;
    private Integer maxDiscrepancies;
    private Integer batchSize;
    
    private String notificationEmails;
    private Boolean autoCreateIncidents;
    
    private Long ownerId;
    private String ownerName;
    
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;
    
    // Statistics
    private Long totalRuns;
    private Long lastRunDiscrepancies;
    private String lastRunStatus;
    private String lastRunAt;
}

