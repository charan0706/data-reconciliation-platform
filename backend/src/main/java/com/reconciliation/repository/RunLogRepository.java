package com.reconciliation.repository;

import com.reconciliation.entity.RunLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RunLogRepository extends JpaRepository<RunLog, Long> {
    
    List<RunLog> findByReconciliationRunIdOrderByTimestamp(Long runId);
    
    @Query("SELECT r FROM RunLog r WHERE r.reconciliationRun.id = :runId AND r.logLevel = 'ERROR'")
    List<RunLog> findErrorLogs(@Param("runId") Long runId);
    
    @Query("SELECT r FROM RunLog r WHERE r.reconciliationRun.id = :runId AND r.stepName = :stepName ORDER BY r.timestamp")
    List<RunLog> findByStep(@Param("runId") Long runId, @Param("stepName") String stepName);
    
    void deleteByReconciliationRunId(Long runId);
}

