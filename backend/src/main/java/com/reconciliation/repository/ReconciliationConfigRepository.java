package com.reconciliation.repository;

import com.reconciliation.entity.ReconciliationConfig;
import com.reconciliation.enums.ScheduleFrequency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReconciliationConfigRepository extends JpaRepository<ReconciliationConfig, Long> {
    
    Optional<ReconciliationConfig> findByConfigCode(String configCode);
    
    boolean existsByConfigCode(String configCode);
    
    List<ReconciliationConfig> findByIsActiveTrue();
    
    @Query("SELECT c FROM ReconciliationConfig c WHERE c.isScheduled = true AND c.scheduleEnabled = true AND c.isActive = true")
    List<ReconciliationConfig> findScheduledConfigs();
    
    @Query("SELECT c FROM ReconciliationConfig c WHERE c.scheduleFrequency = :frequency AND c.isActive = true")
    List<ReconciliationConfig> findByScheduleFrequency(@Param("frequency") ScheduleFrequency frequency);
    
    @Query("SELECT c FROM ReconciliationConfig c WHERE c.sourceSystem.id = :systemId OR c.targetSystem.id = :systemId")
    List<ReconciliationConfig> findBySystem(@Param("systemId") Long systemId);
    
    @Query("SELECT c FROM ReconciliationConfig c WHERE c.owner.id = :ownerId AND c.isActive = true")
    List<ReconciliationConfig> findByOwnerId(@Param("ownerId") Long ownerId);
    
    @Query("SELECT c FROM ReconciliationConfig c WHERE LOWER(c.configName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(c.configCode) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<ReconciliationConfig> search(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT c FROM ReconciliationConfig c LEFT JOIN FETCH c.attributeMappings WHERE c.id = :id")
    Optional<ReconciliationConfig> findByIdWithMappings(@Param("id") Long id);
}

