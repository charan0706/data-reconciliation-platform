package com.reconciliation.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunLogDTO {
    private Long id;
    private Long runId;
    private String logLevel;
    private String stepName;
    private String message;
    private String details;
    private String timestamp;
    private Long durationMs;
    private Long recordsProcessed;
    private String errorCode;
    private String stackTrace;
}

