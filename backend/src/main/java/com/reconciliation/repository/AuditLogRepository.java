package com.reconciliation.repository;

import com.reconciliation.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);
    
    Page<AuditLog> findByUsername(String username, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    Page<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.eventType = :eventType ORDER BY a.timestamp DESC")
    Page<AuditLog> findByEventType(@Param("eventType") String eventType, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.module = :module ORDER BY a.timestamp DESC")
    List<AuditLog> findByModule(@Param("module") String module);
    
    @Query("SELECT a FROM AuditLog a WHERE a.success = false ORDER BY a.timestamp DESC")
    Page<AuditLog> findFailedOperations(Pageable pageable);
}

