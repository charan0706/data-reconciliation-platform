package com.reconciliation.scheduler;

import com.reconciliation.entity.ReconciliationConfig;
import com.reconciliation.enums.ScheduleFrequency;
import com.reconciliation.repository.ReconciliationConfigRepository;
import com.reconciliation.service.ReconciliationEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing scheduled reconciliation jobs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReconciliationSchedulerService {
    
    private final ReconciliationConfigRepository configRepository;
    private final ReconciliationEngineService reconciliationEngine;
    
    /**
     * Run hourly reconciliations.
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void runHourlyReconciliations() {
        log.info("Starting hourly scheduled reconciliations");
        executeScheduledReconciliations(ScheduleFrequency.HOURLY);
    }
    
    /**
     * Run daily reconciliations.
     */
    @Scheduled(cron = "0 0 1 * * *") // Every day at 1 AM
    public void runDailyReconciliations() {
        log.info("Starting daily scheduled reconciliations");
        executeScheduledReconciliations(ScheduleFrequency.DAILY);
    }
    
    /**
     * Run weekly reconciliations.
     */
    @Scheduled(cron = "0 0 2 * * SUN") // Every Sunday at 2 AM
    public void runWeeklyReconciliations() {
        log.info("Starting weekly scheduled reconciliations");
        executeScheduledReconciliations(ScheduleFrequency.WEEKLY);
    }
    
    /**
     * Run monthly reconciliations.
     */
    @Scheduled(cron = "0 0 3 1 * *") // First day of month at 3 AM
    public void runMonthlyReconciliations() {
        log.info("Starting monthly scheduled reconciliations");
        executeScheduledReconciliations(ScheduleFrequency.MONTHLY);
    }
    
    /**
     * Check for SLA breaches every 15 minutes.
     */
    @Scheduled(fixedRate = 900000) // Every 15 minutes
    public void checkSlABreaches() {
        log.debug("Checking for SLA breaches");
        // Implement SLA breach checking logic
    }
    
    /**
     * Clean up old run data weekly.
     */
    @Scheduled(cron = "0 0 4 * * SUN") // Every Sunday at 4 AM
    public void cleanupOldData() {
        log.info("Starting cleanup of old reconciliation data");
        // Implement cleanup logic based on retention policy
    }
    
    /**
     * Execute reconciliations for a specific frequency.
     */
    private void executeScheduledReconciliations(ScheduleFrequency frequency) {
        try {
            List<ReconciliationConfig> configs = configRepository.findByScheduleFrequency(frequency);
            
            configs.stream()
                    .filter(config -> config.getIsActive() && config.getScheduleEnabled())
                    .forEach(config -> {
                        try {
                            log.info("Executing scheduled reconciliation: {}", config.getConfigCode());
                            reconciliationEngine.executeReconciliation(config.getId(), "SCHEDULER", true);
                        } catch (Exception e) {
                            log.error("Failed to execute scheduled reconciliation {}: {}", 
                                    config.getConfigCode(), e.getMessage());
                        }
                    });
            
            log.info("Completed {} scheduled reconciliations for frequency {}", 
                    configs.size(), frequency);
            
        } catch (Exception e) {
            log.error("Error executing scheduled reconciliations for {}: {}", frequency, e.getMessage());
        }
    }
    
    /**
     * Trigger on-demand reconciliation.
     */
    public void triggerOnDemand(Long configId, String triggeredBy) {
        log.info("Triggering on-demand reconciliation for config {} by {}", configId, triggeredBy);
        reconciliationEngine.executeReconciliation(configId, triggeredBy, false);
    }
    
    /**
     * Get next scheduled run time for a config.
     */
    public LocalDateTime getNextRunTime(ReconciliationConfig config) {
        if (!config.getIsScheduled() || !config.getScheduleEnabled()) {
            return null;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        switch (config.getScheduleFrequency()) {
            case HOURLY:
                return now.plusHours(1).withMinute(0).withSecond(0);
            case DAILY:
                return now.plusDays(1).withHour(1).withMinute(0).withSecond(0);
            case WEEKLY:
                return now.plusWeeks(1).withHour(2).withMinute(0).withSecond(0);
            case MONTHLY:
                return now.plusMonths(1).withDayOfMonth(1).withHour(3).withMinute(0).withSecond(0);
            default:
                return null;
        }
    }
}

