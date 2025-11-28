package com.reconciliation.controller;

import com.reconciliation.dto.ApiResponse;
import com.reconciliation.dto.IncidentCommentDTO;
import com.reconciliation.dto.IncidentDTO;
import com.reconciliation.enums.IncidentStatus;
import com.reconciliation.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/incidents")
@RequiredArgsConstructor
@Tag(name = "Incidents", description = "Incident management with Maker-Checker workflow")
public class IncidentController {
    
    private final IncidentService incidentService;
    
    @GetMapping
    @Operation(summary = "Get all incidents")
    public ResponseEntity<ApiResponse<List<IncidentDTO>>> getAllIncidents(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<IncidentDTO> page = incidentService.getAllIncidents(pageable);
        return ResponseEntity.ok(ApiResponse.success(page.getContent(), buildPageInfo(page)));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get incident by ID")
    public ResponseEntity<ApiResponse<IncidentDTO>> getIncidentById(@PathVariable Long id) {
        IncidentDTO incident = incidentService.getIncidentById(id);
        return ResponseEntity.ok(ApiResponse.success(incident));
    }
    
    @GetMapping("/number/{incidentNumber}")
    @Operation(summary = "Get incident by number")
    public ResponseEntity<ApiResponse<IncidentDTO>> getIncidentByNumber(@PathVariable String incidentNumber) {
        IncidentDTO incident = incidentService.getIncidentByNumber(incidentNumber);
        return ResponseEntity.ok(ApiResponse.success(incident));
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Get incidents by status")
    public ResponseEntity<ApiResponse<List<IncidentDTO>>> getIncidentsByStatus(
            @PathVariable IncidentStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<IncidentDTO> page = incidentService.getIncidentsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(page.getContent(), buildPageInfo(page)));
    }
    
    @GetMapping("/assigned/{userId}")
    @Operation(summary = "Get incidents assigned to user")
    public ResponseEntity<ApiResponse<List<IncidentDTO>>> getAssignedIncidents(@PathVariable Long userId) {
        List<IncidentDTO> incidents = incidentService.getIncidentsByAssignee(userId);
        return ResponseEntity.ok(ApiResponse.success(incidents));
    }
    
    @GetMapping("/pending-review/{checkerId}")
    @Operation(summary = "Get incidents pending checker review")
    public ResponseEntity<ApiResponse<List<IncidentDTO>>> getPendingReviewIncidents(@PathVariable Long checkerId) {
        List<IncidentDTO> incidents = incidentService.getPendingReviewIncidents(checkerId);
        return ResponseEntity.ok(ApiResponse.success(incidents));
    }
    
    @GetMapping("/overdue")
    @Operation(summary = "Get overdue incidents")
    public ResponseEntity<ApiResponse<List<IncidentDTO>>> getOverdueIncidents() {
        List<IncidentDTO> incidents = incidentService.getOverdueIncidents();
        return ResponseEntity.ok(ApiResponse.success(incidents));
    }
    
    // ========== Workflow Actions ==========
    
    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'DATA_OWNER')")
    @Operation(summary = "Assign incident to user")
    public ResponseEntity<ApiResponse<IncidentDTO>> assignIncident(
            @PathVariable Long id,
            @RequestParam Long userId,
            Authentication authentication) {
        IncidentDTO incident = incidentService.assignIncident(id, userId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(incident, "Incident assigned successfully"));
    }
    
    @PostMapping("/{id}/start-investigation")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAKER', 'RISK_ANALYST')")
    @Operation(summary = "Start investigation (Maker action)")
    public ResponseEntity<ApiResponse<IncidentDTO>> startInvestigation(
            @PathVariable Long id,
            Authentication authentication) {
        IncidentDTO incident = incidentService.startInvestigation(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(incident, "Investigation started"));
    }
    
    @PostMapping("/{id}/submit-resolution")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAKER', 'RISK_ANALYST')")
    @Operation(summary = "Submit resolution for review (Maker action)")
    public ResponseEntity<ApiResponse<IncidentDTO>> submitResolution(
            @PathVariable Long id,
            @RequestParam String rootCause,
            @RequestParam String proposedResolution,
            @RequestParam(required = false) String resolutionNotes,
            Authentication authentication) {
        IncidentDTO incident = incidentService.submitResolution(
                id, rootCause, proposedResolution, resolutionNotes, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(incident, "Resolution submitted for review"));
    }
    
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHECKER')")
    @Operation(summary = "Approve resolution (Checker action)")
    public ResponseEntity<ApiResponse<IncidentDTO>> approveResolution(
            @PathVariable Long id,
            @RequestParam(required = false) String comments,
            Authentication authentication) {
        IncidentDTO incident = incidentService.approveResolution(id, comments, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(incident, "Resolution approved"));
    }
    
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHECKER')")
    @Operation(summary = "Reject resolution (Checker action)")
    public ResponseEntity<ApiResponse<IncidentDTO>> rejectResolution(
            @PathVariable Long id,
            @RequestParam String rejectionReason,
            Authentication authentication) {
        IncidentDTO incident = incidentService.rejectResolution(id, rejectionReason, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(incident, "Resolution rejected"));
    }
    
    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'DATA_OWNER')")
    @Operation(summary = "Close incident")
    public ResponseEntity<ApiResponse<IncidentDTO>> closeIncident(
            @PathVariable Long id,
            Authentication authentication) {
        IncidentDTO incident = incidentService.closeIncident(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(incident, "Incident closed"));
    }
    
    @PostMapping("/{id}/escalate")
    @PreAuthorize("hasAnyRole('ADMIN', 'DATA_OWNER', 'RISK_ANALYST')")
    @Operation(summary = "Escalate incident")
    public ResponseEntity<ApiResponse<IncidentDTO>> escalateIncident(
            @PathVariable Long id,
            @RequestParam String escalatedTo,
            @RequestParam String reason,
            Authentication authentication) {
        IncidentDTO incident = incidentService.escalateIncident(id, escalatedTo, reason, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(incident, "Incident escalated"));
    }
    
    @PostMapping("/{id}/comments")
    @Operation(summary = "Add comment to incident")
    public ResponseEntity<ApiResponse<IncidentDTO>> addComment(
            @PathVariable Long id,
            @Valid @RequestBody IncidentCommentDTO commentDto,
            Authentication authentication) {
        IncidentDTO incident = incidentService.addComment(id, commentDto, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(incident, "Comment added"));
    }
    
    // ========== Statistics ==========
    
    @GetMapping("/stats")
    @Operation(summary = "Get incident statistics")
    public ResponseEntity<ApiResponse<List<Object[]>>> getIncidentStats() {
        List<Object[]> stats = incidentService.getIncidentStatsByStatus();
        return ResponseEntity.ok(ApiResponse.success(stats));
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

