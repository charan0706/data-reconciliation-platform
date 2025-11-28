package com.reconciliation.service;

import com.reconciliation.entity.AuditLog;
import com.reconciliation.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    
    private final AuditLogRepository auditLogRepository;
    
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String action, String entityType, Long entityId, String oldValue, String newValue) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .eventType(action + "_" + entityType)
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .username(getCurrentUsername())
                    .userRole(getCurrentUserRole())
                    .ipAddress(getClientIpAddress())
                    .userAgent(getUserAgent())
                    .oldValue(truncateValue(oldValue))
                    .newValue(truncateValue(newValue))
                    .timestamp(LocalDateTime.now())
                    .requestId(UUID.randomUUID().toString())
                    .module(entityType)
                    .success(true)
                    .build();
            
            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} - {} - {}", action, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage());
        }
    }
    
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logError(String action, String entityType, Long entityId, String errorMessage) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .eventType(action + "_" + entityType + "_ERROR")
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .username(getCurrentUsername())
                    .ipAddress(getClientIpAddress())
                    .timestamp(LocalDateTime.now())
                    .requestId(UUID.randomUUID().toString())
                    .module(entityType)
                    .success(false)
                    .errorMessage(errorMessage)
                    .build();
            
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to create error audit log: {}", e.getMessage());
        }
    }
    
    public Page<AuditLog> getAuditLogs(String entityType, Long entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
    }
    
    public Page<AuditLog> getAuditLogsByUser(String username, Pageable pageable) {
        return auditLogRepository.findByUsername(username, pageable);
    }
    
    public Page<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByDateRange(startDate, endDate, pageable);
    }
    
    private String getCurrentUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null ? auth.getName() : "SYSTEM";
        } catch (Exception e) {
            return "SYSTEM";
        }
    }
    
    private String getCurrentUserRole() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && !auth.getAuthorities().isEmpty()) {
                return auth.getAuthorities().iterator().next().getAuthority();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Could not get client IP address");
        }
        return null;
    }
    
    private String getUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest().getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.debug("Could not get user agent");
        }
        return null;
    }
    
    private String truncateValue(String value) {
        if (value == null) return null;
        return value.length() > 4000 ? value.substring(0, 4000) : value;
    }
}

