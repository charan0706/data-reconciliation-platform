package com.reconciliation.repository;

import com.reconciliation.entity.SourceSystem;
import com.reconciliation.enums.SystemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SourceSystemRepository extends JpaRepository<SourceSystem, Long> {
    
    Optional<SourceSystem> findBySystemCode(String systemCode);
    
    boolean existsBySystemCode(String systemCode);
    
    List<SourceSystem> findBySystemType(SystemType systemType);
    
    List<SourceSystem> findByIsActiveTrue();
    
    @Query("SELECT s FROM SourceSystem s WHERE s.isSource = true AND s.isActive = true")
    List<SourceSystem> findActiveSources();
    
    @Query("SELECT s FROM SourceSystem s WHERE s.isTarget = true AND s.isActive = true")
    List<SourceSystem> findActiveTargets();
    
    @Query("SELECT s FROM SourceSystem s WHERE s.dataOwner = :owner AND s.isActive = true")
    List<SourceSystem> findByDataOwner(@Param("owner") String owner);
    
    @Query("SELECT s FROM SourceSystem s WHERE LOWER(s.systemName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND s.isActive = true")
    List<SourceSystem> searchByName(@Param("searchTerm") String searchTerm);
}

