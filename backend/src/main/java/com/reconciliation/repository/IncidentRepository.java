package com.reconciliation.repository;

import com.reconciliation.entity.Incident;
import com.reconciliation.enums.DiscrepancySeverity;
import com.reconciliation.enums.IncidentStatus;
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
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    
    Optional<Incident> findByIncidentNumber(String incidentNumber);
    
    List<Incident> findByStatus(IncidentStatus status);
    
    Page<Incident> findByStatus(IncidentStatus status, Pageable pageable);
    
    @Query("SELECT i FROM Incident i WHERE i.status IN :statuses ORDER BY i.createdAt DESC")
    Page<Incident> findByStatusIn(@Param("statuses") List<IncidentStatus> statuses, Pageable pageable);
    
    List<Incident> findByAssignedToId(Long userId);
    
    @Query("SELECT i FROM Incident i WHERE i.assignedTo.id = :userId AND i.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED')")
    List<Incident> findOpenIncidentsByAssignee(@Param("userId") Long userId);
    
    @Query("SELECT i FROM Incident i WHERE i.status = 'PENDING_CHECKER_REVIEW' AND i.checker.id = :checkerId")
    List<Incident> findPendingReviewForChecker(@Param("checkerId") Long checkerId);
    
    @Query("SELECT i FROM Incident i WHERE i.reconciliationConfig.id = :configId ORDER BY i.createdAt DESC")
    Page<Incident> findByConfigId(@Param("configId") Long configId, Pageable pageable);
    
    @Query("SELECT i FROM Incident i WHERE i.reconciliationRun.id = :runId")
    List<Incident> findByRunId(@Param("runId") Long runId);
    
    @Query("SELECT i FROM Incident i WHERE i.severity = :severity AND i.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED')")
    List<Incident> findOpenBySeverity(@Param("severity") DiscrepancySeverity severity);
    
    @Query("SELECT i FROM Incident i WHERE i.dueDate < :now AND i.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED')")
    List<Incident> findOverdueIncidents(@Param("now") LocalDateTime now);
    
    @Query("SELECT i FROM Incident i WHERE i.slaBreach = true AND i.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED')")
    List<Incident> findSlaBreachedIncidents();
    
    @Query("SELECT i.status, COUNT(i) FROM Incident i GROUP BY i.status")
    List<Object[]> countByStatus();
    
    @Query("SELECT i.severity, COUNT(i) FROM Incident i WHERE i.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED') GROUP BY i.severity")
    List<Object[]> countOpenBySeverity();
    
    @Query("SELECT COUNT(i) FROM Incident i WHERE i.createdAt >= :since AND i.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED')")
    Long countOpenSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT i FROM Incident i WHERE i.createdAt BETWEEN :startDate AND :endDate ORDER BY i.createdAt DESC")
    List<Incident> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(i) FROM Incident i WHERE i.status = 'RESOLVED' AND i.resolutionApprovedAt BETWEEN :startDate AND :endDate")
    Long countResolvedInPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query(value = "SELECT * FROM incidents ORDER BY created_at DESC FETCH FIRST 1 ROWS ONLY", nativeQuery = true)
    Optional<Incident> findLatest();
}

