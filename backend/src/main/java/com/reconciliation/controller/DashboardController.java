package com.reconciliation.controller;

import com.reconciliation.dto.ApiResponse;
import com.reconciliation.dto.DashboardDTO;
import com.reconciliation.dto.DiscrepancySummaryDTO;
import com.reconciliation.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard metrics and reporting")
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    @GetMapping
    @Operation(summary = "Get dashboard metrics")
    public ResponseEntity<ApiResponse<DashboardDTO>> getDashboard() {
        DashboardDTO dashboard = dashboardService.getDashboardMetrics();
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
    
    @GetMapping("/config/{configId}/summary")
    @Operation(summary = "Get configuration summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getConfigSummary(@PathVariable Long configId) {
        Map<String, Object> summary = dashboardService.getConfigSummary(configId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
    
    @GetMapping("/run/{runId}/discrepancies")
    @Operation(summary = "Get discrepancy summary for a run")
    public ResponseEntity<ApiResponse<DiscrepancySummaryDTO>> getDiscrepancySummary(@PathVariable Long runId) {
        DiscrepancySummaryDTO summary = dashboardService.getDiscrepancySummary(runId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}

