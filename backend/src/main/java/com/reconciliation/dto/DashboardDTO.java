package com.reconciliation.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDTO {
    
    // Summary Metrics
    private Long totalSystems;
    private Long totalConfigurations;
    private Long totalRunsToday;
    private Long totalRunsThisWeek;
    private Long totalRunsThisMonth;
    
    // Discrepancy Metrics
    private Long totalOpenDiscrepancies;
    private Long discrepanciesToday;
    private Long discrepanciesThisWeek;
    private Double averageMatchPercentage;
    
    // Incident Metrics
    private Long totalOpenIncidents;
    private Long criticalIncidents;
    private Long highSeverityIncidents;
    private Long pendingReviewIncidents;
    private Long overdueIncidents;
    private Long slaBreachedIncidents;
    
    // Status Distribution
    private Map<String, Long> runsByStatus;
    private Map<String, Long> incidentsByStatus;
    private Map<String, Long> incidentsBySeverity;
    
    // Recent Activity
    private List<ReconciliationRunDTO> recentRuns;
    private List<IncidentDTO> recentIncidents;
    
    // Trend Data
    private List<TrendDataDTO> discrepancyTrend;
    private List<TrendDataDTO> runTrend;
    
    // Top Issues
    private List<TopIssueDTO> topDiscrepancyTypes;
    private List<TopIssueDTO> topAffectedSystems;
    private List<TopIssueDTO> topAffectedAttributes;
    
    // Performance Metrics
    private Double averageExecutionTime;
    private Double successRate;
    private Long scheduledRunsCompleted;
    private Long scheduledRunsFailed;
}

