package com.reconciliation.service;

import com.reconciliation.dto.*;
import com.reconciliation.enums.DiscrepancySeverity;
import com.reconciliation.enums.IncidentStatus;
import com.reconciliation.enums.ReconciliationStatus;
import com.reconciliation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for dashboard metrics and reporting.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {
    
    private final SourceSystemRepository sourceSystemRepository;
    private final ReconciliationConfigRepository configRepository;
    private final ReconciliationRunRepository runRepository;
    private final IncidentRepository incidentRepository;
    private final DiscrepancyRepository discrepancyRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Get comprehensive dashboard metrics.
     */
    public DashboardDTO getDashboardMetrics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime startOfWeek = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime startOfMonth = LocalDate.now().minusMonths(1).atStartOfDay();
        
        return DashboardDTO.builder()
                // Summary Metrics
                .totalSystems(sourceSystemRepository.count())
                .totalConfigurations(configRepository.count())
                .totalRunsToday(countRunsSince(startOfDay))
                .totalRunsThisWeek(countRunsSince(startOfWeek))
                .totalRunsThisMonth(countRunsSince(startOfMonth))
                
                // Discrepancy Metrics
                .totalOpenDiscrepancies(getTotalOpenDiscrepancies())
                .discrepanciesToday(getDiscrepanciesSince(startOfDay))
                .discrepanciesThisWeek(getDiscrepanciesSince(startOfWeek))
                .averageMatchPercentage(getAverageMatchPercentage())
                
                // Incident Metrics
                .totalOpenIncidents(getOpenIncidentCount())
                .criticalIncidents(getIncidentCountBySeverity(DiscrepancySeverity.CRITICAL))
                .highSeverityIncidents(getIncidentCountBySeverity(DiscrepancySeverity.HIGH))
                .pendingReviewIncidents(getIncidentCountByStatus(IncidentStatus.PENDING_CHECKER_REVIEW))
                .overdueIncidents((long) incidentRepository.findOverdueIncidents(now).size())
                .slaBreachedIncidents((long) incidentRepository.findSlaBreachedIncidents().size())
                
                // Status Distribution
                .runsByStatus(getRunsByStatus())
                .incidentsByStatus(getIncidentsByStatus())
                .incidentsBySeverity(getIncidentsBySeverity())
                
                // Recent Activity
                .recentRuns(getRecentRuns(10))
                .recentIncidents(getRecentIncidents(10))
                
                // Trend Data
                .discrepancyTrend(getDiscrepancyTrend(30))
                .runTrend(getRunTrend(30))
                
                // Performance Metrics
                .averageExecutionTime(getAverageExecutionTime())
                .successRate(getSuccessRate())
                .scheduledRunsCompleted(getScheduledRunsCompleted(startOfMonth))
                .scheduledRunsFailed(getScheduledRunsFailed(startOfMonth))
                .build();
    }
    
    /**
     * Get run summary for a specific configuration.
     */
    public Map<String, Object> getConfigSummary(Long configId) {
        Map<String, Object> summary = new HashMap<>();
        
        var runs = runRepository.findByReconciliationConfigIdOrderByStartedAtDesc(configId);
        
        if (!runs.isEmpty()) {
            var latestRun = runs.get(0);
            summary.put("lastRunId", latestRun.getRunId());
            summary.put("lastRunStatus", latestRun.getStatus());
            summary.put("lastRunAt", formatDateTime(latestRun.getStartedAt()));
            summary.put("lastRunDiscrepancies", latestRun.getDiscrepancyCount());
        }
        
        summary.put("totalRuns", runs.size());
        summary.put("successfulRuns", runs.stream()
                .filter(r -> r.getStatus() == ReconciliationStatus.COMPLETED || 
                             r.getStatus() == ReconciliationStatus.COMPLETED_WITH_DISCREPANCIES)
                .count());
        summary.put("failedRuns", runs.stream()
                .filter(r -> r.getStatus() == ReconciliationStatus.FAILED)
                .count());
        
        // Average match percentage
        double avgMatch = runs.stream()
                .mapToDouble(r -> r.getMatchPercentage())
                .average()
                .orElse(0.0);
        summary.put("averageMatchPercentage", Math.round(avgMatch * 100.0) / 100.0);
        
        return summary;
    }
    
    /**
     * Get discrepancy breakdown for a run.
     */
    public DiscrepancySummaryDTO getDiscrepancySummary(Long runId) {
        List<Object[]> byType = discrepancyRepository.countByType(runId);
        List<Object[]> bySeverity = discrepancyRepository.countBySeverity(runId);
        List<Object[]> byAttribute = discrepancyRepository.countByAttribute(runId);
        
        Map<String, Long> typeMap = byType.stream()
                .collect(Collectors.toMap(
                        arr -> arr[0].toString(),
                        arr -> (Long) arr[1]
                ));
        
        Map<String, Long> severityMap = bySeverity.stream()
                .collect(Collectors.toMap(
                        arr -> arr[0].toString(),
                        arr -> (Long) arr[1]
                ));
        
        Map<String, Long> attributeMap = byAttribute.stream()
                .limit(10)
                .collect(Collectors.toMap(
                        arr -> arr[0].toString(),
                        arr -> (Long) arr[1]
                ));
        
        return DiscrepancySummaryDTO.builder()
                .totalDiscrepancies(discrepancyRepository.countByReconciliationRunId(runId))
                .missingInSource(typeMap.getOrDefault("MISSING_IN_SOURCE", 0L))
                .missingInTarget(typeMap.getOrDefault("MISSING_IN_TARGET", 0L))
                .attributeMismatches(typeMap.getOrDefault("ATTRIBUTE_MISMATCH", 0L))
                .byType(typeMap)
                .bySeverity(severityMap)
                .byAttribute(attributeMap)
                .build();
    }
    
    // Helper methods
    
    private long countRunsSince(LocalDateTime since) {
        return runRepository.findByDateRange(since, LocalDateTime.now()).size();
    }
    
    private long getTotalOpenDiscrepancies() {
        Long count = runRepository.countTotalDiscrepanciesSince(LocalDateTime.now().minusDays(30));
        return count != null ? count : 0L;
    }
    
    private long getDiscrepanciesSince(LocalDateTime since) {
        Long count = runRepository.countTotalDiscrepanciesSince(since);
        return count != null ? count : 0L;
    }
    
    private Double getAverageMatchPercentage() {
        // Calculate from recent runs
        var recentRuns = runRepository.findByDateRange(
                LocalDateTime.now().minusDays(30), 
                LocalDateTime.now());
        
        return recentRuns.stream()
                .mapToDouble(r -> r.getMatchPercentage())
                .average()
                .orElse(100.0);
    }
    
    private long getOpenIncidentCount() {
        return incidentRepository.countOpenSince(LocalDateTime.now().minusYears(1));
    }
    
    private long getIncidentCountBySeverity(DiscrepancySeverity severity) {
        return incidentRepository.findOpenBySeverity(severity).size();
    }
    
    private long getIncidentCountByStatus(IncidentStatus status) {
        return runRepository.countByStatus(ReconciliationStatus.valueOf(status.name()));
    }
    
    private Map<String, Long> getRunsByStatus() {
        Map<String, Long> result = new HashMap<>();
        for (ReconciliationStatus status : ReconciliationStatus.values()) {
            Long count = runRepository.countByStatus(status);
            if (count > 0) {
                result.put(status.name(), count);
            }
        }
        return result;
    }
    
    private Map<String, Long> getIncidentsByStatus() {
        Map<String, Long> result = new HashMap<>();
        List<Object[]> counts = incidentRepository.countByStatus();
        for (Object[] arr : counts) {
            result.put(arr[0].toString(), (Long) arr[1]);
        }
        return result;
    }
    
    private Map<String, Long> getIncidentsBySeverity() {
        Map<String, Long> result = new HashMap<>();
        List<Object[]> counts = incidentRepository.countOpenBySeverity();
        for (Object[] arr : counts) {
            result.put(arr[0].toString(), (Long) arr[1]);
        }
        return result;
    }
    
    private List<ReconciliationRunDTO> getRecentRuns(int limit) {
        return runRepository.findAll(PageRequest.of(0, limit, 
                        org.springframework.data.domain.Sort.by("startedAt").descending()))
                .stream()
                .map(this::toRunDTO)
                .collect(Collectors.toList());
    }
    
    private List<IncidentDTO> getRecentIncidents(int limit) {
        return incidentRepository.findAll(PageRequest.of(0, limit,
                        org.springframework.data.domain.Sort.by("createdAt").descending()))
                .stream()
                .map(this::toIncidentDTO)
                .collect(Collectors.toList());
    }
    
    private List<TrendDataDTO> getDiscrepancyTrend(int days) {
        List<TrendDataDTO> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            
            Long count = runRepository.countTotalDiscrepanciesSince(start);
            
            trend.add(TrendDataDTO.builder()
                    .date(date.toString())
                    .count(count != null ? count : 0L)
                    .build());
        }
        
        return trend;
    }
    
    private List<TrendDataDTO> getRunTrend(int days) {
        List<TrendDataDTO> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            
            long count = runRepository.findByDateRange(start, end).size();
            
            trend.add(TrendDataDTO.builder()
                    .date(date.toString())
                    .count(count)
                    .build());
        }
        
        return trend;
    }
    
    private Double getAverageExecutionTime() {
        // Calculate average from recent successful runs
        var recentRuns = runRepository.findByDateRange(
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now());
        
        return recentRuns.stream()
                .filter(r -> r.getExecutionTimeMs() != null)
                .mapToLong(r -> r.getExecutionTimeMs())
                .average()
                .orElse(0.0);
    }
    
    private Double getSuccessRate() {
        var recentRuns = runRepository.findByDateRange(
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now());
        
        if (recentRuns.isEmpty()) return 100.0;
        
        long successful = recentRuns.stream()
                .filter(r -> r.getStatus() == ReconciliationStatus.COMPLETED ||
                             r.getStatus() == ReconciliationStatus.COMPLETED_WITH_DISCREPANCIES)
                .count();
        
        return (successful * 100.0) / recentRuns.size();
    }
    
    private Long getScheduledRunsCompleted(LocalDateTime since) {
        var runs = runRepository.findByDateRange(since, LocalDateTime.now());
        return runs.stream()
                .filter(r -> r.getIsScheduledRun() &&
                        (r.getStatus() == ReconciliationStatus.COMPLETED ||
                         r.getStatus() == ReconciliationStatus.COMPLETED_WITH_DISCREPANCIES))
                .count();
    }
    
    private Long getScheduledRunsFailed(LocalDateTime since) {
        var runs = runRepository.findByDateRange(since, LocalDateTime.now());
        return runs.stream()
                .filter(r -> r.getIsScheduledRun() && r.getStatus() == ReconciliationStatus.FAILED)
                .count();
    }
    
    private ReconciliationRunDTO toRunDTO(com.reconciliation.entity.ReconciliationRun entity) {
        return ReconciliationRunDTO.builder()
                .id(entity.getId())
                .runId(entity.getRunId())
                .configId(entity.getReconciliationConfig().getId())
                .configCode(entity.getReconciliationConfig().getConfigCode())
                .configName(entity.getReconciliationConfig().getConfigName())
                .status(entity.getStatus())
                .startedAt(formatDateTime(entity.getStartedAt()))
                .completedAt(formatDateTime(entity.getCompletedAt()))
                .discrepancyCount(entity.getDiscrepancyCount())
                .matchPercentage(entity.getMatchPercentage())
                .build();
    }
    
    private IncidentDTO toIncidentDTO(com.reconciliation.entity.Incident entity) {
        return IncidentDTO.builder()
                .id(entity.getId())
                .incidentNumber(entity.getIncidentNumber())
                .title(entity.getTitle())
                .status(entity.getStatus())
                .severity(entity.getSeverity())
                .discrepancyCount(entity.getDiscrepancyCount())
                .createdAt(formatDateTime(entity.getCreatedAt()))
                .build();
    }
    
    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_FORMATTER) : null;
    }
}

