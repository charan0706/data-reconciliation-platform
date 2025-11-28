package com.reconciliation.controller;

import com.reconciliation.dto.ApiResponse;
import com.reconciliation.dto.SourceSystemDTO;
import com.reconciliation.enums.SystemType;
import com.reconciliation.service.SourceSystemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/systems")
@RequiredArgsConstructor
@Tag(name = "Source Systems", description = "Manage source and target systems")
public class SourceSystemController {
    
    private final SourceSystemService sourceSystemService;
    
    @GetMapping
    @Operation(summary = "Get all systems")
    public ResponseEntity<ApiResponse<List<SourceSystemDTO>>> getAllSystems() {
        List<SourceSystemDTO> systems = sourceSystemService.getAllSystems();
        return ResponseEntity.ok(ApiResponse.success(systems));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get system by ID")
    public ResponseEntity<ApiResponse<SourceSystemDTO>> getSystemById(@PathVariable Long id) {
        SourceSystemDTO system = sourceSystemService.getSystemById(id);
        return ResponseEntity.ok(ApiResponse.success(system));
    }
    
    @GetMapping("/code/{systemCode}")
    @Operation(summary = "Get system by code")
    public ResponseEntity<ApiResponse<SourceSystemDTO>> getSystemByCode(@PathVariable String systemCode) {
        SourceSystemDTO system = sourceSystemService.getSystemByCode(systemCode);
        return ResponseEntity.ok(ApiResponse.success(system));
    }
    
    @GetMapping("/type/{type}")
    @Operation(summary = "Get systems by type")
    public ResponseEntity<ApiResponse<List<SourceSystemDTO>>> getSystemsByType(@PathVariable SystemType type) {
        List<SourceSystemDTO> systems = sourceSystemService.getSystemsByType(type);
        return ResponseEntity.ok(ApiResponse.success(systems));
    }
    
    @GetMapping("/sources")
    @Operation(summary = "Get active source systems")
    public ResponseEntity<ApiResponse<List<SourceSystemDTO>>> getActiveSources() {
        List<SourceSystemDTO> systems = sourceSystemService.getActiveSources();
        return ResponseEntity.ok(ApiResponse.success(systems));
    }
    
    @GetMapping("/targets")
    @Operation(summary = "Get active target systems")
    public ResponseEntity<ApiResponse<List<SourceSystemDTO>>> getActiveTargets() {
        List<SourceSystemDTO> systems = sourceSystemService.getActiveTargets();
        return ResponseEntity.ok(ApiResponse.success(systems));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DATA_OWNER')")
    @Operation(summary = "Create new system")
    public ResponseEntity<ApiResponse<SourceSystemDTO>> createSystem(@Valid @RequestBody SourceSystemDTO dto) {
        SourceSystemDTO created = sourceSystemService.createSystem(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "System created successfully"));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DATA_OWNER')")
    @Operation(summary = "Update system")
    public ResponseEntity<ApiResponse<SourceSystemDTO>> updateSystem(
            @PathVariable Long id, 
            @Valid @RequestBody SourceSystemDTO dto) {
        SourceSystemDTO updated = sourceSystemService.updateSystem(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "System updated successfully"));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete system")
    public ResponseEntity<ApiResponse<Void>> deleteSystem(@PathVariable Long id) {
        sourceSystemService.deleteSystem(id);
        return ResponseEntity.ok(ApiResponse.success(null, "System deactivated successfully"));
    }
    
    @PostMapping("/{id}/test-connection")
    @Operation(summary = "Test system connection")
    public ResponseEntity<ApiResponse<Boolean>> testConnection(@PathVariable Long id) {
        boolean success = sourceSystemService.testConnection(id);
        return ResponseEntity.ok(ApiResponse.success(success, 
                success ? "Connection successful" : "Connection failed"));
    }
}

