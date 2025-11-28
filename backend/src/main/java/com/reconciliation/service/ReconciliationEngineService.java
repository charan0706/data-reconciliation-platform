package com.reconciliation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reconciliation.entity.*;
import com.reconciliation.enums.*;
import com.reconciliation.exception.ReconciliationException;
import com.reconciliation.exception.ResourceNotFoundException;
import com.reconciliation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Core reconciliation engine that performs data comparison between source and target systems.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReconciliationEngineService {
    
    private final ReconciliationConfigRepository configRepository;
    private final ReconciliationRunRepository runRepository;
    private final DiscrepancyRepository discrepancyRepository;
    private final RunLogRepository runLogRepository;
    private final AttributeMappingRepository attributeMappingRepository;
    private final DataExtractionService dataExtractionService;
    private final IncidentService incidentService;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;
    
    /**
     * Execute reconciliation for a given configuration.
     */
    @Async
    @Transactional
    public CompletableFuture<ReconciliationRun> executeReconciliation(Long configId, String triggeredBy, boolean isScheduled) {
        ReconciliationConfig config = configRepository.findByIdWithMappings(configId)
                .orElseThrow(() -> new ResourceNotFoundException("Config not found: " + configId));
        
        // Create run record
        ReconciliationRun run = createRun(config, triggeredBy, isScheduled);
        
        try {
            log.info("Starting reconciliation run {} for config {}", run.getRunId(), config.getConfigCode());
            run.setStatus(ReconciliationStatus.IN_PROGRESS);
            run.setStartedAt(LocalDateTime.now());
            run = runRepository.save(run);
            
            logStep(run, "INFO", "INITIALIZATION", "Reconciliation started", null);
            
            // Step 1: Extract source data
            run.setStatus(ReconciliationStatus.EXTRACTING_SOURCE);
            runRepository.save(run);
            long sourceStart = System.currentTimeMillis();
            List<Map<String, Object>> sourceData = extractData(run, config, true);
            run.setSourceExtractionTimeMs(System.currentTimeMillis() - sourceStart);
            run.setSourceRecordCount((long) sourceData.size());
            logStep(run, "INFO", "SOURCE_EXTRACTION", 
                    String.format("Extracted %d records from source", sourceData.size()), null);
            
            // Step 2: Extract target data
            run.setStatus(ReconciliationStatus.EXTRACTING_TARGET);
            runRepository.save(run);
            long targetStart = System.currentTimeMillis();
            List<Map<String, Object>> targetData = extractData(run, config, false);
            run.setTargetExtractionTimeMs(System.currentTimeMillis() - targetStart);
            run.setTargetRecordCount((long) targetData.size());
            logStep(run, "INFO", "TARGET_EXTRACTION", 
                    String.format("Extracted %d records from target", targetData.size()), null);
            
            // Step 3: Compare data
            run.setStatus(ReconciliationStatus.COMPARING);
            runRepository.save(run);
            long compareStart = System.currentTimeMillis();
            List<AttributeMapping> mappings = attributeMappingRepository.findEnabledMappings(configId);
            ComparisonResult result = compareData(run, config, sourceData, targetData, mappings);
            run.setComparisonTimeMs(System.currentTimeMillis() - compareStart);
            
            // Update run statistics
            run.setMatchedRecordCount(result.matchedCount);
            run.setDiscrepancyCount((long) result.discrepancies.size());
            run.setMissingInSourceCount(result.missingInSource);
            run.setMissingInTargetCount(result.missingInTarget);
            run.setAttributeMismatchCount(result.attributeMismatches);
            
            logStep(run, "INFO", "COMPARISON", 
                    String.format("Comparison complete. Matched: %d, Discrepancies: %d", 
                            result.matchedCount, result.discrepancies.size()), null);
            
            // Step 4: Save discrepancies
            saveDiscrepancies(run, result.discrepancies);
            
            // Step 5: Create incidents if configured
            if (config.getAutoCreateIncidents() && !result.discrepancies.isEmpty()) {
                run.setStatus(ReconciliationStatus.GENERATING_REPORT);
                runRepository.save(run);
                createIncidentsForDiscrepancies(run, config, result.discrepancies);
            }
            
            // Complete
            run.setStatus(result.discrepancies.isEmpty() ? 
                    ReconciliationStatus.COMPLETED : ReconciliationStatus.COMPLETED_WITH_DISCREPANCIES);
            run.setCompletedAt(LocalDateTime.now());
            run.setExecutionTimeMs(System.currentTimeMillis() - run.getStartedAt().getNano() / 1000000);
            run = runRepository.save(run);
            
            logStep(run, "INFO", "COMPLETION", "Reconciliation completed successfully", null);
            auditService.logAction("EXECUTE", "ReconciliationRun", run.getId(), null, run.getRunId());
            
            log.info("Reconciliation run {} completed. Status: {}, Discrepancies: {}", 
                    run.getRunId(), run.getStatus(), run.getDiscrepancyCount());
            
            return CompletableFuture.completedFuture(run);
            
        } catch (Exception e) {
            log.error("Reconciliation run {} failed: {}", run.getRunId(), e.getMessage(), e);
            run.setStatus(ReconciliationStatus.FAILED);
            run.setCompletedAt(LocalDateTime.now());
            run.setErrorMessage(e.getMessage());
            run.setErrorStackTrace(getStackTrace(e));
            runRepository.save(run);
            
            logStep(run, "ERROR", "FAILURE", e.getMessage(), getStackTrace(e));
            auditService.logError("EXECUTE", "ReconciliationRun", run.getId(), e.getMessage());
            
            return CompletableFuture.failedFuture(new ReconciliationException("Reconciliation failed", e));
        }
    }
    
    private ReconciliationRun createRun(ReconciliationConfig config, String triggeredBy, boolean isScheduled) {
        String runId = String.format("RUN-%s-%s", 
                config.getConfigCode(), 
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        
        return runRepository.save(ReconciliationRun.builder()
                .runId(runId)
                .reconciliationConfig(config)
                .status(ReconciliationStatus.PENDING)
                .triggeredBy(triggeredBy)
                .isScheduledRun(isScheduled)
                .sourceRecordCount(0L)
                .targetRecordCount(0L)
                .matchedRecordCount(0L)
                .discrepancyCount(0L)
                .missingInSourceCount(0L)
                .missingInTargetCount(0L)
                .attributeMismatchCount(0L)
                .build());
    }
    
    private List<Map<String, Object>> extractData(ReconciliationRun run, ReconciliationConfig config, boolean isSource) {
        try {
            SourceSystem system = isSource ? config.getSourceSystem() : config.getTargetSystem();
            String query = isSource ? config.getSourceQuery() : config.getTargetQuery();
            String filePattern = isSource ? config.getSourceFilePattern() : config.getTargetFilePattern();
            
            return dataExtractionService.extractData(system, query, filePattern);
        } catch (Exception e) {
            throw new ReconciliationException("Failed to extract " + (isSource ? "source" : "target") + " data", e);
        }
    }
    
    private ComparisonResult compareData(ReconciliationRun run, ReconciliationConfig config,
                                         List<Map<String, Object>> sourceData, 
                                         List<Map<String, Object>> targetData,
                                         List<AttributeMapping> mappings) {
        ComparisonResult result = new ComparisonResult();
        List<String> keyAttributes = parseKeyAttributes(config.getPrimaryKeyAttributes());
        
        // Index target data by key
        Map<String, Map<String, Object>> targetIndex = new HashMap<>();
        for (Map<String, Object> targetRecord : targetData) {
            String key = buildRecordKey(targetRecord, keyAttributes, mappings, false);
            targetIndex.put(key, targetRecord);
        }
        
        Set<String> processedKeys = new HashSet<>();
        int discrepancyCounter = 0;
        
        // Compare source records against target
        for (Map<String, Object> sourceRecord : sourceData) {
            String key = buildRecordKey(sourceRecord, keyAttributes, mappings, true);
            processedKeys.add(key);
            
            Map<String, Object> targetRecord = targetIndex.get(key);
            
            if (targetRecord == null) {
                // Missing in target
                if (discrepancyCounter < config.getMaxDiscrepancies()) {
                    result.discrepancies.add(createDiscrepancy(
                            run, key, DiscrepancyType.MISSING_IN_TARGET, null,
                            null, null, sourceRecord, null,
                            DiscrepancySeverity.HIGH, discrepancyCounter++));
                }
                result.missingInTarget++;
            } else {
                // Compare attributes
                List<Discrepancy> mismatches = compareAttributes(run, key, sourceRecord, targetRecord, 
                        mappings, config, discrepancyCounter);
                if (mismatches.isEmpty()) {
                    result.matchedCount++;
                } else {
                    result.attributeMismatches += mismatches.size();
                    for (Discrepancy d : mismatches) {
                        if (discrepancyCounter < config.getMaxDiscrepancies()) {
                            result.discrepancies.add(d);
                            discrepancyCounter++;
                        }
                    }
                }
            }
        }
        
        // Find records missing in source
        for (Map.Entry<String, Map<String, Object>> entry : targetIndex.entrySet()) {
            if (!processedKeys.contains(entry.getKey())) {
                if (discrepancyCounter < config.getMaxDiscrepancies()) {
                    result.discrepancies.add(createDiscrepancy(
                            run, entry.getKey(), DiscrepancyType.MISSING_IN_SOURCE, null,
                            null, null, null, entry.getValue(),
                            DiscrepancySeverity.HIGH, discrepancyCounter++));
                }
                result.missingInSource++;
            }
        }
        
        return result;
    }
    
    private List<Discrepancy> compareAttributes(ReconciliationRun run, String recordKey,
                                                 Map<String, Object> sourceRecord,
                                                 Map<String, Object> targetRecord,
                                                 List<AttributeMapping> mappings,
                                                 ReconciliationConfig config,
                                                 int discrepancyCounter) {
        List<Discrepancy> mismatches = new ArrayList<>();
        
        for (AttributeMapping mapping : mappings) {
            if (!mapping.getIsEnabled() || mapping.getComparisonType() == ComparisonType.IGNORE) {
                continue;
            }
            
            Object sourceValue = sourceRecord.get(mapping.getSourceAttribute());
            Object targetValue = targetRecord.get(mapping.getTargetAttribute());
            
            // Apply transformations if configured
            sourceValue = applyTransformation(sourceValue, mapping.getSourceTransformation());
            targetValue = applyTransformation(targetValue, mapping.getTargetTransformation());
            
            // Normalize values based on config
            if (config.getTrimWhitespace() && sourceValue instanceof String) {
                sourceValue = ((String) sourceValue).trim();
            }
            if (config.getTrimWhitespace() && targetValue instanceof String) {
                targetValue = ((String) targetValue).trim();
            }
            
            boolean matches = compareValues(sourceValue, targetValue, mapping, config);
            
            if (!matches) {
                mismatches.add(createDiscrepancy(
                        run, recordKey, DiscrepancyType.ATTRIBUTE_MISMATCH,
                        mapping.getDisplayName() != null ? mapping.getDisplayName() : mapping.getSourceAttribute(),
                        String.valueOf(sourceValue), String.valueOf(targetValue),
                        sourceRecord, targetRecord,
                        mapping.getMismatchSeverity(), discrepancyCounter));
            }
        }
        
        return mismatches;
    }
    
    private boolean compareValues(Object sourceValue, Object targetValue, 
                                   AttributeMapping mapping, ReconciliationConfig config) {
        // Handle nulls
        if (sourceValue == null && targetValue == null) {
            return true;
        }
        if (sourceValue == null || targetValue == null) {
            if (config.getNullEqualsEmpty()) {
                String s = sourceValue == null ? "" : String.valueOf(sourceValue);
                String t = targetValue == null ? "" : String.valueOf(targetValue);
                return s.isEmpty() && t.isEmpty();
            }
            return false;
        }
        
        switch (mapping.getComparisonType()) {
            case EXACT_MATCH:
                return sourceValue.equals(targetValue);
                
            case CASE_INSENSITIVE:
                return String.valueOf(sourceValue).equalsIgnoreCase(String.valueOf(targetValue));
                
            case NUMERIC_TOLERANCE:
                try {
                    double s = Double.parseDouble(String.valueOf(sourceValue));
                    double t = Double.parseDouble(String.valueOf(targetValue));
                    double tolerance = mapping.getToleranceValue() != null ? mapping.getToleranceValue() : 0.0;
                    if ("PERCENTAGE".equals(mapping.getToleranceType())) {
                        return Math.abs(s - t) <= Math.abs(s * tolerance / 100);
                    }
                    return Math.abs(s - t) <= tolerance;
                } catch (NumberFormatException e) {
                    return sourceValue.equals(targetValue);
                }
                
            case DATE_TOLERANCE:
                // Simplified date comparison
                return sourceValue.equals(targetValue);
                
            case CONTAINS:
                return String.valueOf(sourceValue).contains(String.valueOf(targetValue)) ||
                       String.valueOf(targetValue).contains(String.valueOf(sourceValue));
                
            case REGEX_MATCH:
                if (mapping.getFormatPattern() != null) {
                    return String.valueOf(sourceValue).matches(mapping.getFormatPattern()) &&
                           String.valueOf(targetValue).matches(mapping.getFormatPattern());
                }
                return sourceValue.equals(targetValue);
                
            default:
                return sourceValue.equals(targetValue);
        }
    }
    
    private Discrepancy createDiscrepancy(ReconciliationRun run, String recordKey, 
                                          DiscrepancyType type, String attributeName,
                                          String sourceValue, String targetValue,
                                          Map<String, Object> sourceRecord, Map<String, Object> targetRecord,
                                          DiscrepancySeverity severity, int counter) {
        String code = String.format("DISC-%s-%05d", run.getRunId(), counter);
        
        Discrepancy discrepancy = Discrepancy.builder()
                .reconciliationRun(run)
                .discrepancyCode(code)
                .discrepancyType(type)
                .severity(severity)
                .recordKey(recordKey)
                .attributeName(attributeName)
                .sourceValue(sourceValue)
                .targetValue(targetValue)
                .rowNumber((long) counter)
                .build();
        
        try {
            if (sourceRecord != null) {
                discrepancy.setSourceRecordJson(objectMapper.writeValueAsString(sourceRecord));
            }
            if (targetRecord != null) {
                discrepancy.setTargetRecordJson(objectMapper.writeValueAsString(targetRecord));
            }
        } catch (Exception e) {
            log.warn("Failed to serialize record JSON", e);
        }
        
        // Calculate difference for numeric values
        if (sourceValue != null && targetValue != null && type == DiscrepancyType.ATTRIBUTE_MISMATCH) {
            try {
                double s = Double.parseDouble(sourceValue);
                double t = Double.parseDouble(targetValue);
                discrepancy.setDifferenceAmount(Math.abs(s - t));
                if (s != 0) {
                    discrepancy.setDifferencePercentage(Math.abs((s - t) / s) * 100);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        
        return discrepancy;
    }
    
    private String buildRecordKey(Map<String, Object> record, List<String> keyAttributes, 
                                   List<AttributeMapping> mappings, boolean isSource) {
        StringBuilder key = new StringBuilder();
        for (String attr : keyAttributes) {
            String actualAttr = attr;
            for (AttributeMapping mapping : mappings) {
                if (isSource && mapping.getSourceAttribute().equals(attr)) {
                    actualAttr = mapping.getSourceAttribute();
                    break;
                } else if (!isSource && mapping.getTargetAttribute().equals(attr)) {
                    actualAttr = mapping.getTargetAttribute();
                    break;
                }
            }
            if (key.length() > 0) key.append("|");
            Object value = record.get(actualAttr);
            key.append(value != null ? value.toString() : "NULL");
        }
        return key.toString();
    }
    
    private List<String> parseKeyAttributes(String primaryKeyAttributes) {
        if (primaryKeyAttributes == null || primaryKeyAttributes.isEmpty()) {
            return Collections.singletonList("id");
        }
        return Arrays.stream(primaryKeyAttributes.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
    
    private Object applyTransformation(Object value, String transformation) {
        if (transformation == null || transformation.isEmpty() || value == null) {
            return value;
        }
        // Simplified transformation - in production, use SpEL or scripting engine
        String strValue = String.valueOf(value);
        switch (transformation.toUpperCase()) {
            case "UPPERCASE":
                return strValue.toUpperCase();
            case "LOWERCASE":
                return strValue.toLowerCase();
            case "TRIM":
                return strValue.trim();
            default:
                return value;
        }
    }
    
    private void saveDiscrepancies(ReconciliationRun run, List<Discrepancy> discrepancies) {
        if (discrepancies.isEmpty()) return;
        
        discrepancyRepository.saveAll(discrepancies);
        log.info("Saved {} discrepancies for run {}", discrepancies.size(), run.getRunId());
    }
    
    private void createIncidentsForDiscrepancies(ReconciliationRun run, ReconciliationConfig config, 
                                                  List<Discrepancy> discrepancies) {
        try {
            incidentService.createIncidentFromRun(run, discrepancies);
        } catch (Exception e) {
            log.error("Failed to create incidents for run {}: {}", run.getRunId(), e.getMessage());
        }
    }
    
    private void logStep(ReconciliationRun run, String level, String step, String message, String details) {
        RunLog log = RunLog.builder()
                .reconciliationRun(run)
                .logLevel(level)
                .stepName(step)
                .message(message)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
        runLogRepository.save(log);
    }
    
    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
            if (sb.length() > 4000) break;
        }
        return sb.toString();
    }
    
    // Inner class for comparison results
    private static class ComparisonResult {
        long matchedCount = 0;
        long missingInSource = 0;
        long missingInTarget = 0;
        long attributeMismatches = 0;
        List<Discrepancy> discrepancies = new ArrayList<>();
    }
}

