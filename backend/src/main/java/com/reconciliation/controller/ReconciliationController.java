package com.reconciliation.controller;

import com.reconciliation.dto.*;
import com.reconciliation.entity.ReconciliationRun;
import com.reconciliation.enums.ReconciliationStatus;
import com.reconciliation.repository.ReconciliationRunRepository;
import com.reconciliation.scheduler.ReconciliationSchedulerService;
import com.reconciliation.service.DashboardService;
import com.reconciliation.service.ReconciliationConfigService;
import com.reconciliation.service.ReconciliationEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/reconciliation")
@RequiredArgsConstructor
@Tag(name = "Reconciliation", description = "Manage reconciliation configurations and runs")
public class ReconciliationController {
    
    private final ReconciliationConfigService configService;
    private final ReconciliationEngineService engineService;
    private final ReconciliationSchedulerService schedulerService;
    private final ReconciliationRunRepository runRepository;
    private final DashboardService dashboardService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // ========== Configuration Endpoints ==========
    
    @GetMapping("/configs")
    @Operation(summary = "Get all reconciliation configurations")
    public ResponseEntity<ApiResponse<List<ReconciliationConfigDTO>>> getAllConfigs() {
        List<ReconciliationConfigDTO> configs = configService.getAllConfigs();
        return ResponseEntity.ok(ApiResponse.success(configs));
    }
    
    @GetMapping("/configs/{id}")
    @Operation(summary = "Get configuration by ID")
    public ResponseEntity<ApiResponse<ReconciliationConfigDTO>> getConfigById(@PathVariable Long id) {
        ReconciliationConfigDTO config = configService.getConfigById(id);
        return ResponseEntity.ok(ApiResponse.success(config));
    }
    
    @GetMapping("/configs/code/{configCode}")
    @Operation(summary = "Get configuration by code")
    public ResponseEntity<ApiResponse<ReconciliationConfigDTO>> getConfigByCode(@PathVariable String configCode) {
        ReconciliationConfigDTO config = configService.getConfigByCode(configCode);
        return ResponseEntity.ok(ApiResponse.success(config));
    }
    
    @GetMapping("/configs/search")
    @Operation(summary = "Search configurations")
    public ResponseEntity<ApiResponse<List<ReconciliationConfigDTO>>> searchConfigs(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ReconciliationConfigDTO> page = configService.searchConfigs(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(page.getContent(), 
                buildPageInfo(page)));
    }
    
    @GetMapping("/configs/scheduled")
    @Operation(summary = "Get scheduled configurations")
    public ResponseEntity<ApiResponse<List<ReconciliationConfigDTO>>> getScheduledConfigs() {
        List<ReconciliationConfigDTO> configs = configService.getScheduledConfigs();
        return ResponseEntity.ok(ApiResponse.success(configs));
    }
    
    @PostMapping("/configs")
    @PreAuthorize("hasAnyRole('ADMIN', 'DATA_OWNER')")
    @Operation(summary = "Create new configuration")
    public ResponseEntity<ApiResponse<ReconciliationConfigDTO>> createConfig(
            @Valid @RequestBody ReconciliationConfigDTO dto) {
        ReconciliationConfigDTO created = configService.createConfig(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Configuration created successfully"));
    }
    
    @PutMapping("/configs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DATA_OWNER')")
    @Operation(summary = "Update configuration")
    public ResponseEntity<ApiResponse<ReconciliationConfigDTO>> updateConfig(
            @PathVariable Long id,
            @Valid @RequestBody ReconciliationConfigDTO dto) {
        ReconciliationConfigDTO updated = configService.updateConfig(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Configuration updated successfully"));
    }
    
    @DeleteMapping("/configs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete configuration")
    public ResponseEntity<ApiResponse<Void>> deleteConfig(@PathVariable Long id) {
        configService.deleteConfig(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Configuration deactivated successfully"));
    }
    
    @PutMapping("/configs/{id}/schedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'DATA_OWNER')")
    @Operation(summary = "Enable/disable schedule")
    public ResponseEntity<ApiResponse<Void>> toggleSchedule(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        configService.enableSchedule(id, enabled);
        return ResponseEntity.ok(ApiResponse.success(null, 
                enabled ? "Schedule enabled" : "Schedule disabled"));
    }
    
    // ========== Run Endpoints ==========
    
    @PostMapping("/configs/{configId}/run")
    @PreAuthorize("hasAnyRole('ADMIN', 'DATA_OWNER', 'RISK_ANALYST')")
    @Operation(summary = "Trigger on-demand reconciliation")
    public ResponseEntity<ApiResponse<ReconciliationRunDTO>> triggerReconciliation(
            @PathVariable Long configId,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "SYSTEM";
        CompletableFuture<ReconciliationRun> future = engineService.executeReconciliation(configId, username, false);
        
        // Return immediately with pending status
        return ResponseEntity.accepted()
                .body(ApiResponse.success(null, "Reconciliation triggered successfully"));
    }
    
    @GetMapping("/runs")
    @Operation(summary = "Get all runs with pagination")
    public ResponseEntity<ApiResponse<List<ReconciliationRunDTO>>> getAllRuns(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ReconciliationRun> page = runRepository.findAll(pageable);
        List<ReconciliationRunDTO> runs = page.getContent().stream()
                .map(this::toRunDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(runs, buildPageInfo(page)));
    }
    
    @GetMapping("/runs/{runId}")
    @Operation(summary = "Get run by ID")
    public ResponseEntity<ApiResponse<ReconciliationRunDTO>> getRunById(@PathVariable String runId) {
        ReconciliationRun run = runRepository.findByRunId(runId)
                .orElseThrow(() -> new RuntimeException("Run not found: " + runId));
        return ResponseEntity.ok(ApiResponse.success(toRunDTO(run)));
    }
    
    @GetMapping("/configs/{configId}/runs")
    @Operation(summary = "Get runs for a configuration")
    public ResponseEntity<ApiResponse<List<ReconciliationRunDTO>>> getRunsByConfig(
            @PathVariable Long configId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ReconciliationRun> page = runRepository.findByReconciliationConfigId(configId, pageable);
        List<ReconciliationRunDTO> runs = page.getContent().stream()
                .map(this::toRunDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(runs, buildPageInfo(page)));
    }
    
    @GetMapping("/runs/status/{status}")
    @Operation(summary = "Get runs by status")
    public ResponseEntity<ApiResponse<List<ReconciliationRunDTO>>> getRunsByStatus(
            @PathVariable ReconciliationStatus status) {
        List<ReconciliationRunDTO> runs = runRepository.findByStatus(status).stream()
                .map(this::toRunDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(runs));
    }
    
    @GetMapping("/runs/{runId}/discrepancies")
    @Operation(summary = "Get discrepancies for a run")
    public ResponseEntity<ApiResponse<DiscrepancySummaryDTO>> getRunDiscrepancies(
            @PathVariable String runId) {
        ReconciliationRun run = runRepository.findByRunId(runId)
                .orElseThrow(() -> new RuntimeException("Run not found: " + runId));
        DiscrepancySummaryDTO summary = dashboardService.getDiscrepancySummary(run.getId());
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
    
    @GetMapping("/configs/{configId}/summary")
    @Operation(summary = "Get configuration summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getConfigSummary(@PathVariable Long configId) {
        Map<String, Object> summary = dashboardService.getConfigSummary(configId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
    
    // ========== Helper Methods ==========
    
    private ReconciliationRunDTO toRunDTO(ReconciliationRun entity) {
        return ReconciliationRunDTO.builder()
                .id(entity.getId())
                .runId(entity.getRunId())
                .configId(entity.getReconciliationConfig().getId())
                .configCode(entity.getReconciliationConfig().getConfigCode())
                .configName(entity.getReconciliationConfig().getConfigName())
                .status(entity.getStatus())
                .startedAt(entity.getStartedAt() != null ? entity.getStartedAt().format(DATE_FORMATTER) : null)
                .completedAt(entity.getCompletedAt() != null ? entity.getCompletedAt().format(DATE_FORMATTER) : null)
                .triggeredBy(entity.getTriggeredBy())
                .isScheduledRun(entity.getIsScheduledRun())
                .sourceRecordCount(entity.getSourceRecordCount())
                .targetRecordCount(entity.getTargetRecordCount())
                .matchedRecordCount(entity.getMatchedRecordCount())
                .discrepancyCount(entity.getDiscrepancyCount())
                .missingInSourceCount(entity.getMissingInSourceCount())
                .missingInTargetCount(entity.getMissingInTargetCount())
                .attributeMismatchCount(entity.getAttributeMismatchCount())
                .errorMessage(entity.getErrorMessage())
                .executionTimeMs(entity.getExecutionTimeMs())
                .matchPercentage(entity.getMatchPercentage())
                .build();
    }
    
    private <T> ApiResponse.PageInfo buildPageInfo(Page<T> page) {
        return ApiResponse.PageInfo.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}

