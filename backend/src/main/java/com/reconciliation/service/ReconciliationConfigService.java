package com.reconciliation.service;

import com.reconciliation.dto.AttributeMappingDTO;
import com.reconciliation.dto.ReconciliationConfigDTO;
import com.reconciliation.entity.*;
import com.reconciliation.enums.ComparisonType;
import com.reconciliation.enums.DiscrepancySeverity;
import com.reconciliation.exception.DuplicateResourceException;
import com.reconciliation.exception.ResourceNotFoundException;
import com.reconciliation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReconciliationConfigService {
    
    private final ReconciliationConfigRepository configRepository;
    private final SourceSystemRepository sourceSystemRepository;
    private final AttributeMappingRepository attributeMappingRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public List<ReconciliationConfigDTO> getAllConfigs() {
        return configRepository.findByIsActiveTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public ReconciliationConfigDTO getConfigById(Long id) {
        ReconciliationConfig config = configRepository.findByIdWithMappings(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reconciliation config not found with id: " + id));
        return toDTO(config);
    }
    
    public ReconciliationConfigDTO getConfigByCode(String configCode) {
        ReconciliationConfig config = configRepository.findByConfigCode(configCode)
                .orElseThrow(() -> new ResourceNotFoundException("Reconciliation config not found with code: " + configCode));
        return toDTO(config);
    }
    
    public Page<ReconciliationConfigDTO> searchConfigs(String searchTerm, Pageable pageable) {
        return configRepository.search(searchTerm, pageable).map(this::toDTO);
    }
    
    public List<ReconciliationConfigDTO> getScheduledConfigs() {
        return configRepository.findScheduledConfigs()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public ReconciliationConfigDTO createConfig(ReconciliationConfigDTO dto) {
        if (configRepository.existsByConfigCode(dto.getConfigCode())) {
            throw new DuplicateResourceException("Config with code '" + dto.getConfigCode() + "' already exists");
        }
        
        SourceSystem sourceSystem = sourceSystemRepository.findById(dto.getSourceSystemId())
                .orElseThrow(() -> new ResourceNotFoundException("Source system not found with id: " + dto.getSourceSystemId()));
        
        SourceSystem targetSystem = sourceSystemRepository.findById(dto.getTargetSystemId())
                .orElseThrow(() -> new ResourceNotFoundException("Target system not found with id: " + dto.getTargetSystemId()));
        
        ReconciliationConfig config = toEntity(dto);
        config.setSourceSystem(sourceSystem);
        config.setTargetSystem(targetSystem);
        config.setIsActive(true);
        
        if (dto.getOwnerId() != null) {
            User owner = userRepository.findById(dto.getOwnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getOwnerId()));
            config.setOwner(owner);
        }
        
        config = configRepository.save(config);
        
        // Save attribute mappings
        if (dto.getAttributeMappings() != null) {
            for (AttributeMappingDTO mappingDto : dto.getAttributeMappings()) {
                AttributeMapping mapping = toMappingEntity(mappingDto);
                mapping.setReconciliationConfig(config);
                attributeMappingRepository.save(mapping);
            }
        }
        
        auditService.logAction("CREATE", "ReconciliationConfig", config.getId(), null, dto.getConfigCode());
        log.info("Created reconciliation config: {}", config.getConfigCode());
        
        return toDTO(config);
    }
    
    public ReconciliationConfigDTO updateConfig(Long id, ReconciliationConfigDTO dto) {
        ReconciliationConfig config = configRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reconciliation config not found with id: " + id));
        
        String oldValue = config.getConfigCode();
        
        if (dto.getSourceSystemId() != null && !dto.getSourceSystemId().equals(config.getSourceSystem().getId())) {
            SourceSystem sourceSystem = sourceSystemRepository.findById(dto.getSourceSystemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Source system not found"));
            config.setSourceSystem(sourceSystem);
        }
        
        if (dto.getTargetSystemId() != null && !dto.getTargetSystemId().equals(config.getTargetSystem().getId())) {
            SourceSystem targetSystem = sourceSystemRepository.findById(dto.getTargetSystemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Target system not found"));
            config.setTargetSystem(targetSystem);
        }
        
        updateEntity(config, dto);
        config = configRepository.save(config);
        
        // Update attribute mappings
        if (dto.getAttributeMappings() != null) {
            attributeMappingRepository.deleteByReconciliationConfigId(id);
            for (AttributeMappingDTO mappingDto : dto.getAttributeMappings()) {
                AttributeMapping mapping = toMappingEntity(mappingDto);
                mapping.setReconciliationConfig(config);
                attributeMappingRepository.save(mapping);
            }
        }
        
        auditService.logAction("UPDATE", "ReconciliationConfig", config.getId(), oldValue, dto.getConfigCode());
        log.info("Updated reconciliation config: {}", config.getConfigCode());
        
        return toDTO(config);
    }
    
    public void deleteConfig(Long id) {
        ReconciliationConfig config = configRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reconciliation config not found with id: " + id));
        
        config.setIsActive(false);
        config.setScheduleEnabled(false);
        configRepository.save(config);
        
        auditService.logAction("DELETE", "ReconciliationConfig", id, config.getConfigCode(), null);
        log.info("Deactivated reconciliation config: {}", config.getConfigCode());
    }
    
    public void enableSchedule(Long id, boolean enabled) {
        ReconciliationConfig config = configRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reconciliation config not found with id: " + id));
        
        config.setScheduleEnabled(enabled);
        configRepository.save(config);
        
        auditService.logAction("SCHEDULE_" + (enabled ? "ENABLE" : "DISABLE"), "ReconciliationConfig", id, null, null);
        log.info("{} schedule for config: {}", enabled ? "Enabled" : "Disabled", config.getConfigCode());
    }
    
    private ReconciliationConfigDTO toDTO(ReconciliationConfig entity) {
        return ReconciliationConfigDTO.builder()
                .id(entity.getId())
                .configCode(entity.getConfigCode())
                .configName(entity.getConfigName())
                .description(entity.getDescription())
                .sourceSystemId(entity.getSourceSystem().getId())
                .sourceSystemName(entity.getSourceSystem().getSystemName())
                .targetSystemId(entity.getTargetSystem().getId())
                .targetSystemName(entity.getTargetSystem().getSystemName())
                .sourceQuery(entity.getSourceQuery())
                .targetQuery(entity.getTargetQuery())
                .sourceFilePattern(entity.getSourceFilePattern())
                .targetFilePattern(entity.getTargetFilePattern())
                .attributeMappings(entity.getAttributeMappings() != null ?
                        entity.getAttributeMappings().stream().map(this::toMappingDTO).collect(Collectors.toList()) : null)
                .primaryKeyAttributes(entity.getPrimaryKeyAttributes())
                .scheduleFrequency(entity.getScheduleFrequency())
                .cronExpression(entity.getCronExpression())
                .isScheduled(entity.getIsScheduled())
                .scheduleEnabled(entity.getScheduleEnabled())
                .tolerancePercentage(entity.getTolerancePercentage())
                .dateToleranceMinutes(entity.getDateToleranceMinutes())
                .ignoreCase(entity.getIgnoreCase())
                .trimWhitespace(entity.getTrimWhitespace())
                .nullEqualsEmpty(entity.getNullEqualsEmpty())
                .maxDiscrepancies(entity.getMaxDiscrepancies())
                .batchSize(entity.getBatchSize())
                .notificationEmails(entity.getNotificationEmails())
                .autoCreateIncidents(entity.getAutoCreateIncidents())
                .ownerId(entity.getOwner() != null ? entity.getOwner().getId() : null)
                .ownerName(entity.getOwner() != null ? entity.getOwner().getFullName() : null)
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().format(DATE_FORMATTER) : null)
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().format(DATE_FORMATTER) : null)
                .build();
    }
    
    private ReconciliationConfig toEntity(ReconciliationConfigDTO dto) {
        return ReconciliationConfig.builder()
                .configCode(dto.getConfigCode())
                .configName(dto.getConfigName())
                .description(dto.getDescription())
                .sourceQuery(dto.getSourceQuery())
                .targetQuery(dto.getTargetQuery())
                .sourceFilePattern(dto.getSourceFilePattern())
                .targetFilePattern(dto.getTargetFilePattern())
                .primaryKeyAttributes(dto.getPrimaryKeyAttributes())
                .scheduleFrequency(dto.getScheduleFrequency())
                .cronExpression(dto.getCronExpression())
                .isScheduled(dto.getIsScheduled() != null ? dto.getIsScheduled() : false)
                .scheduleEnabled(dto.getScheduleEnabled() != null ? dto.getScheduleEnabled() : true)
                .tolerancePercentage(dto.getTolerancePercentage())
                .dateToleranceMinutes(dto.getDateToleranceMinutes())
                .ignoreCase(dto.getIgnoreCase() != null ? dto.getIgnoreCase() : false)
                .trimWhitespace(dto.getTrimWhitespace() != null ? dto.getTrimWhitespace() : true)
                .nullEqualsEmpty(dto.getNullEqualsEmpty() != null ? dto.getNullEqualsEmpty() : false)
                .maxDiscrepancies(dto.getMaxDiscrepancies() != null ? dto.getMaxDiscrepancies() : 10000)
                .batchSize(dto.getBatchSize() != null ? dto.getBatchSize() : 1000)
                .notificationEmails(dto.getNotificationEmails())
                .autoCreateIncidents(dto.getAutoCreateIncidents() != null ? dto.getAutoCreateIncidents() : true)
                .build();
    }
    
    private void updateEntity(ReconciliationConfig entity, ReconciliationConfigDTO dto) {
        entity.setConfigName(dto.getConfigName());
        entity.setDescription(dto.getDescription());
        entity.setSourceQuery(dto.getSourceQuery());
        entity.setTargetQuery(dto.getTargetQuery());
        entity.setSourceFilePattern(dto.getSourceFilePattern());
        entity.setTargetFilePattern(dto.getTargetFilePattern());
        entity.setPrimaryKeyAttributes(dto.getPrimaryKeyAttributes());
        entity.setScheduleFrequency(dto.getScheduleFrequency());
        entity.setCronExpression(dto.getCronExpression());
        entity.setIsScheduled(dto.getIsScheduled());
        entity.setScheduleEnabled(dto.getScheduleEnabled());
        entity.setTolerancePercentage(dto.getTolerancePercentage());
        entity.setDateToleranceMinutes(dto.getDateToleranceMinutes());
        entity.setIgnoreCase(dto.getIgnoreCase());
        entity.setTrimWhitespace(dto.getTrimWhitespace());
        entity.setNullEqualsEmpty(dto.getNullEqualsEmpty());
        entity.setMaxDiscrepancies(dto.getMaxDiscrepancies());
        entity.setBatchSize(dto.getBatchSize());
        entity.setNotificationEmails(dto.getNotificationEmails());
        entity.setAutoCreateIncidents(dto.getAutoCreateIncidents());
    }
    
    private AttributeMappingDTO toMappingDTO(AttributeMapping entity) {
        return AttributeMappingDTO.builder()
                .id(entity.getId())
                .sourceAttribute(entity.getSourceAttribute())
                .targetAttribute(entity.getTargetAttribute())
                .displayName(entity.getDisplayName())
                .dataType(entity.getDataType())
                .comparisonType(entity.getComparisonType())
                .toleranceValue(entity.getToleranceValue())
                .toleranceType(entity.getToleranceType())
                .transformationExpression(entity.getTransformationExpression())
                .sourceTransformation(entity.getSourceTransformation())
                .targetTransformation(entity.getTargetTransformation())
                .isKeyAttribute(entity.getIsKeyAttribute())
                .isRequired(entity.getIsRequired())
                .isEnabled(entity.getIsEnabled())
                .sortOrder(entity.getSortOrder())
                .mismatchSeverity(entity.getMismatchSeverity())
                .customValidationRule(entity.getCustomValidationRule())
                .nullHandling(entity.getNullHandling())
                .formatPattern(entity.getFormatPattern())
                .build();
    }
    
    private AttributeMapping toMappingEntity(AttributeMappingDTO dto) {
        return AttributeMapping.builder()
                .sourceAttribute(dto.getSourceAttribute())
                .targetAttribute(dto.getTargetAttribute())
                .displayName(dto.getDisplayName())
                .dataType(dto.getDataType())
                .comparisonType(dto.getComparisonType() != null ? dto.getComparisonType() : ComparisonType.EXACT_MATCH)
                .toleranceValue(dto.getToleranceValue())
                .toleranceType(dto.getToleranceType())
                .transformationExpression(dto.getTransformationExpression())
                .sourceTransformation(dto.getSourceTransformation())
                .targetTransformation(dto.getTargetTransformation())
                .isKeyAttribute(dto.getIsKeyAttribute() != null ? dto.getIsKeyAttribute() : false)
                .isRequired(dto.getIsRequired() != null ? dto.getIsRequired() : true)
                .isEnabled(dto.getIsEnabled() != null ? dto.getIsEnabled() : true)
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .mismatchSeverity(dto.getMismatchSeverity() != null ? dto.getMismatchSeverity() : DiscrepancySeverity.MEDIUM)
                .customValidationRule(dto.getCustomValidationRule())
                .nullHandling(dto.getNullHandling())
                .formatPattern(dto.getFormatPattern())
                .build();
    }
}

