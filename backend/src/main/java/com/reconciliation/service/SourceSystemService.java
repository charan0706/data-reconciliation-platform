package com.reconciliation.service;

import com.reconciliation.dto.SourceSystemDTO;
import com.reconciliation.entity.SourceSystem;
import com.reconciliation.enums.SystemType;
import com.reconciliation.exception.ResourceNotFoundException;
import com.reconciliation.exception.DuplicateResourceException;
import com.reconciliation.repository.SourceSystemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SourceSystemService {
    
    private final SourceSystemRepository sourceSystemRepository;
    private final AuditService auditService;
    
    public List<SourceSystemDTO> getAllSystems() {
        return sourceSystemRepository.findByIsActiveTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public SourceSystemDTO getSystemById(Long id) {
        SourceSystem system = sourceSystemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Source system not found with id: " + id));
        return toDTO(system);
    }
    
    public SourceSystemDTO getSystemByCode(String systemCode) {
        SourceSystem system = sourceSystemRepository.findBySystemCode(systemCode)
                .orElseThrow(() -> new ResourceNotFoundException("Source system not found with code: " + systemCode));
        return toDTO(system);
    }
    
    public List<SourceSystemDTO> getSystemsByType(SystemType type) {
        return sourceSystemRepository.findBySystemType(type)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<SourceSystemDTO> getActiveSources() {
        return sourceSystemRepository.findActiveSources()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<SourceSystemDTO> getActiveTargets() {
        return sourceSystemRepository.findActiveTargets()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public SourceSystemDTO createSystem(SourceSystemDTO dto) {
        if (sourceSystemRepository.existsBySystemCode(dto.getSystemCode())) {
            throw new DuplicateResourceException("System with code '" + dto.getSystemCode() + "' already exists");
        }
        
        SourceSystem system = toEntity(dto);
        system.setIsActive(true);
        system = sourceSystemRepository.save(system);
        
        auditService.logAction("CREATE", "SourceSystem", system.getId(), null, dto.toString());
        log.info("Created source system: {}", system.getSystemCode());
        
        return toDTO(system);
    }
    
    public SourceSystemDTO updateSystem(Long id, SourceSystemDTO dto) {
        SourceSystem system = sourceSystemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Source system not found with id: " + id));
        
        String oldValue = toDTO(system).toString();
        
        updateEntity(system, dto);
        system = sourceSystemRepository.save(system);
        
        auditService.logAction("UPDATE", "SourceSystem", system.getId(), oldValue, dto.toString());
        log.info("Updated source system: {}", system.getSystemCode());
        
        return toDTO(system);
    }
    
    public void deleteSystem(Long id) {
        SourceSystem system = sourceSystemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Source system not found with id: " + id));
        
        system.setIsActive(false);
        sourceSystemRepository.save(system);
        
        auditService.logAction("DELETE", "SourceSystem", id, toDTO(system).toString(), null);
        log.info("Deactivated source system: {}", system.getSystemCode());
    }
    
    public boolean testConnection(Long id) {
        SourceSystem system = sourceSystemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Source system not found with id: " + id));
        
        // TODO: Implement actual connection testing based on system type
        log.info("Testing connection for system: {}", system.getSystemCode());
        
        switch (system.getSystemType()) {
            case DATABASE:
                return testDatabaseConnection(system);
            case FILE_SYSTEM:
                return testFileSystemConnection(system);
            case API_ENDPOINT:
                return testApiConnection(system);
            default:
                log.warn("Connection test not implemented for type: {}", system.getSystemType());
                return true;
        }
    }
    
    private boolean testDatabaseConnection(SourceSystem system) {
        // Simplified - in production, use proper JDBC connection testing
        log.info("Testing database connection to: {}:{}/{}", 
                system.getHost(), system.getPort(), system.getDatabaseName());
        return true;
    }
    
    private boolean testFileSystemConnection(SourceSystem system) {
        // Check if file path exists
        log.info("Testing file system access to: {}", system.getFilePath());
        return true;
    }
    
    private boolean testApiConnection(SourceSystem system) {
        // Test API endpoint availability
        log.info("Testing API connection to: {}", system.getApiUrl());
        return true;
    }
    
    private SourceSystemDTO toDTO(SourceSystem entity) {
        return SourceSystemDTO.builder()
                .id(entity.getId())
                .systemCode(entity.getSystemCode())
                .systemName(entity.getSystemName())
                .description(entity.getDescription())
                .systemType(entity.getSystemType())
                .connectionString(entity.getConnectionString())
                .host(entity.getHost())
                .port(entity.getPort())
                .databaseName(entity.getDatabaseName())
                .schemaName(entity.getSchemaName())
                .username(entity.getUsername())
                .filePath(entity.getFilePath())
                .apiUrl(entity.getApiUrl())
                .additionalConfig(entity.getAdditionalConfig())
                .dataOwner(entity.getDataOwner())
                .contactEmail(entity.getContactEmail())
                .isSource(entity.getIsSource())
                .isTarget(entity.getIsTarget())
                .testConnectionQuery(entity.getTestConnectionQuery())
                .isActive(entity.getIsActive())
                .build();
    }
    
    private SourceSystem toEntity(SourceSystemDTO dto) {
        return SourceSystem.builder()
                .systemCode(dto.getSystemCode())
                .systemName(dto.getSystemName())
                .description(dto.getDescription())
                .systemType(dto.getSystemType())
                .connectionString(dto.getConnectionString())
                .host(dto.getHost())
                .port(dto.getPort())
                .databaseName(dto.getDatabaseName())
                .schemaName(dto.getSchemaName())
                .username(dto.getUsername())
                .encryptedPassword(dto.getPassword()) // Should encrypt in production
                .filePath(dto.getFilePath())
                .apiUrl(dto.getApiUrl())
                .apiKey(dto.getApiKey())
                .additionalConfig(dto.getAdditionalConfig())
                .dataOwner(dto.getDataOwner())
                .contactEmail(dto.getContactEmail())
                .isSource(dto.getIsSource() != null ? dto.getIsSource() : true)
                .isTarget(dto.getIsTarget() != null ? dto.getIsTarget() : true)
                .testConnectionQuery(dto.getTestConnectionQuery())
                .build();
    }
    
    private void updateEntity(SourceSystem entity, SourceSystemDTO dto) {
        entity.setSystemName(dto.getSystemName());
        entity.setDescription(dto.getDescription());
        entity.setSystemType(dto.getSystemType());
        entity.setConnectionString(dto.getConnectionString());
        entity.setHost(dto.getHost());
        entity.setPort(dto.getPort());
        entity.setDatabaseName(dto.getDatabaseName());
        entity.setSchemaName(dto.getSchemaName());
        entity.setUsername(dto.getUsername());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            entity.setEncryptedPassword(dto.getPassword());
        }
        entity.setFilePath(dto.getFilePath());
        entity.setApiUrl(dto.getApiUrl());
        if (dto.getApiKey() != null) {
            entity.setApiKey(dto.getApiKey());
        }
        entity.setAdditionalConfig(dto.getAdditionalConfig());
        entity.setDataOwner(dto.getDataOwner());
        entity.setContactEmail(dto.getContactEmail());
        entity.setIsSource(dto.getIsSource());
        entity.setIsTarget(dto.getIsTarget());
        entity.setTestConnectionQuery(dto.getTestConnectionQuery());
    }
}

