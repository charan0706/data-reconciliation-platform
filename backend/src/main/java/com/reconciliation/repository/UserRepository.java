package com.reconciliation.repository;

import com.reconciliation.entity.User;
import com.reconciliation.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role AND u.isActive = true")
    List<User> findByRole(@Param("role") UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.department = :department AND u.isActive = true")
    List<User> findByDepartment(@Param("department") String department);
    
    @Query("SELECT u FROM User u JOIN u.accessibleSystems s WHERE s.id = :systemId AND u.isActive = true")
    List<User> findUsersWithAccessToSystem(@Param("systemId") Long systemId);
}

