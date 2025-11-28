package com.reconciliation.repository;

import com.reconciliation.entity.AttributeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttributeMappingRepository extends JpaRepository<AttributeMapping, Long> {
    
    List<AttributeMapping> findByReconciliationConfigIdOrderBySortOrder(Long configId);
    
    @Query("SELECT a FROM AttributeMapping a WHERE a.reconciliationConfig.id = :configId AND a.isEnabled = true ORDER BY a.sortOrder")
    List<AttributeMapping> findEnabledMappings(@Param("configId") Long configId);
    
    @Query("SELECT a FROM AttributeMapping a WHERE a.reconciliationConfig.id = :configId AND a.isKeyAttribute = true")
    List<AttributeMapping> findKeyAttributes(@Param("configId") Long configId);
    
    void deleteByReconciliationConfigId(Long configId);
}

