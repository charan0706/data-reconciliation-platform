package com.reconciliation.repository;

import com.reconciliation.entity.Discrepancy;
import com.reconciliation.enums.DiscrepancySeverity;
import com.reconciliation.enums.DiscrepancyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscrepancyRepository extends JpaRepository<Discrepancy, Long> {
    
    Page<Discrepancy> findByReconciliationRunId(Long runId, Pageable pageable);
    
    List<Discrepancy> findByReconciliationRunId(Long runId);
    
    List<Discrepancy> findByReconciliationRunIdAndDiscrepancyType(Long runId, DiscrepancyType type);
    
    List<Discrepancy> findByReconciliationRunIdAndSeverity(Long runId, DiscrepancySeverity severity);
    
    @Query("SELECT d FROM Discrepancy d WHERE d.reconciliationRun.id = :runId AND d.isFalsePositive = false")
    List<Discrepancy> findValidDiscrepancies(@Param("runId") Long runId);
    
    @Query("SELECT d FROM Discrepancy d WHERE d.reconciliationRun.id = :runId AND d.incident IS NULL")
    List<Discrepancy> findUnlinkedDiscrepancies(@Param("runId") Long runId);
    
    @Query("SELECT d.discrepancyType, COUNT(d) FROM Discrepancy d WHERE d.reconciliationRun.id = :runId GROUP BY d.discrepancyType")
    List<Object[]> countByType(@Param("runId") Long runId);
    
    @Query("SELECT d.severity, COUNT(d) FROM Discrepancy d WHERE d.reconciliationRun.id = :runId GROUP BY d.severity")
    List<Object[]> countBySeverity(@Param("runId") Long runId);
    
    @Query("SELECT d.attributeName, COUNT(d) FROM Discrepancy d WHERE d.reconciliationRun.id = :runId AND d.attributeName IS NOT NULL GROUP BY d.attributeName ORDER BY COUNT(d) DESC")
    List<Object[]> countByAttribute(@Param("runId") Long runId);
    
    @Modifying
    @Query("UPDATE Discrepancy d SET d.isFalsePositive = true, d.falsePositiveReason = :reason WHERE d.id = :id")
    void markAsFalsePositive(@Param("id") Long id, @Param("reason") String reason);
    
    @Modifying
    @Query("UPDATE Discrepancy d SET d.incident = :incidentId WHERE d.id IN :discrepancyIds")
    void linkToIncident(@Param("discrepancyIds") List<Long> discrepancyIds, @Param("incidentId") Long incidentId);
    
    @Query("SELECT d FROM Discrepancy d WHERE d.recordKey = :recordKey AND d.reconciliationRun.reconciliationConfig.id = :configId ORDER BY d.reconciliationRun.startedAt DESC")
    List<Discrepancy> findHistoryByRecordKey(@Param("recordKey") String recordKey, @Param("configId") Long configId);
    
    Long countByReconciliationRunId(Long runId);
}

