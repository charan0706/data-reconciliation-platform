package com.reconciliation.repository;

import com.reconciliation.entity.ReconciliationRun;
import com.reconciliation.enums.ReconciliationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReconciliationRunRepository extends JpaRepository<ReconciliationRun, Long> {
    
    Optional<ReconciliationRun> findByRunId(String runId);
    
    List<ReconciliationRun> findByReconciliationConfigIdOrderByStartedAtDesc(Long configId);
    
    Page<ReconciliationRun> findByReconciliationConfigId(Long configId, Pageable pageable);
    
    List<ReconciliationRun> findByStatus(ReconciliationStatus status);
    
    @Query("SELECT r FROM ReconciliationRun r WHERE r.status IN :statuses ORDER BY r.startedAt DESC")
    List<ReconciliationRun> findByStatusIn(@Param("statuses") List<ReconciliationStatus> statuses);
    
    @Query("SELECT r FROM ReconciliationRun r WHERE r.startedAt BETWEEN :startDate AND :endDate ORDER BY r.startedAt DESC")
    List<ReconciliationRun> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT r FROM ReconciliationRun r WHERE r.reconciliationConfig.id = :configId AND r.startedAt BETWEEN :startDate AND :endDate")
    List<ReconciliationRun> findByConfigAndDateRange(@Param("configId") Long configId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT r FROM ReconciliationRun r WHERE r.discrepancyCount > 0 ORDER BY r.startedAt DESC")
    Page<ReconciliationRun> findRunsWithDiscrepancies(Pageable pageable);
    
    @Query("SELECT r FROM ReconciliationRun r WHERE r.reconciliationConfig.id = :configId ORDER BY r.startedAt DESC")
    List<ReconciliationRun> findLatestByConfigId(@Param("configId") Long configId, Pageable pageable);
    
    @Query("SELECT COUNT(r) FROM ReconciliationRun r WHERE r.status = :status")
    Long countByStatus(@Param("status") ReconciliationStatus status);
    
    @Query("SELECT SUM(r.discrepancyCount) FROM ReconciliationRun r WHERE r.startedAt >= :since")
    Long countTotalDiscrepanciesSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT r FROM ReconciliationRun r WHERE r.status = 'IN_PROGRESS' AND r.startedAt < :threshold")
    List<ReconciliationRun> findStuckRuns(@Param("threshold") LocalDateTime threshold);
    
    @Query("SELECT AVG(r.executionTimeMs) FROM ReconciliationRun r WHERE r.reconciliationConfig.id = :configId AND r.status = 'COMPLETED'")
    Double getAverageExecutionTime(@Param("configId") Long configId);
}

