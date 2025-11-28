package com.reconciliation.service;

import com.reconciliation.dto.IncidentCommentDTO;
import com.reconciliation.dto.IncidentDTO;
import com.reconciliation.entity.*;
import com.reconciliation.enums.DiscrepancySeverity;
import com.reconciliation.enums.IncidentStatus;
import com.reconciliation.enums.UserRole;
import com.reconciliation.exception.ReconciliationException;
import com.reconciliation.exception.ResourceNotFoundException;
import com.reconciliation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for incident management with Maker-Checker workflow.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class IncidentService {
    
    private final IncidentRepository incidentRepository;
    private final DiscrepancyRepository discrepancyRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Get all incidents with pagination.
     */
    public Page<IncidentDTO> getAllIncidents(Pageable pageable) {
        return incidentRepository.findAll(pageable).map(this::toDTO);
    }
    
    /**
     * Get incidents by status.
     */
    public Page<IncidentDTO> getIncidentsByStatus(IncidentStatus status, Pageable pageable) {
        return incidentRepository.findByStatus(status, pageable).map(this::toDTO);
    }
    
    /**
     * Get incident by ID.
     */
    public IncidentDTO getIncidentById(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + id));
        return toDTO(incident);
    }
    
    /**
     * Get incident by incident number.
     */
    public IncidentDTO getIncidentByNumber(String incidentNumber) {
        Incident incident = incidentRepository.findByIncidentNumber(incidentNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + incidentNumber));
        return toDTO(incident);
    }
    
    /**
     * Get incidents assigned to a user.
     */
    public List<IncidentDTO> getIncidentsByAssignee(Long userId) {
        return incidentRepository.findOpenIncidentsByAssignee(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get incidents pending checker review.
     */
    public List<IncidentDTO> getPendingReviewIncidents(Long checkerId) {
        return incidentRepository.findPendingReviewForChecker(checkerId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Create incident from reconciliation run discrepancies.
     */
    public IncidentDTO createIncidentFromRun(ReconciliationRun run, List<Discrepancy> discrepancies) {
        if (discrepancies.isEmpty()) {
            return null;
        }
        
        // Determine highest severity
        DiscrepancySeverity highestSeverity = discrepancies.stream()
                .map(Discrepancy::getSeverity)
                .min((s1, s2) -> s1.ordinal() - s2.ordinal())
                .orElse(DiscrepancySeverity.MEDIUM);
        
        String incidentNumber = generateIncidentNumber();
        
        Incident incident = Incident.builder()
                .incidentNumber(incidentNumber)
                .title(String.format("Data Discrepancies - %s - %s", 
                        run.getReconciliationConfig().getConfigName(), 
                        run.getRunId()))
                .description(String.format("Automated incident created for %d discrepancies found in reconciliation run %s",
                        discrepancies.size(), run.getRunId()))
                .status(IncidentStatus.OPEN)
                .severity(highestSeverity)
                .reconciliationRun(run)
                .reconciliationConfig(run.getReconciliationConfig())
                .discrepancyCount(discrepancies.size())
                .affectedRecords((long) discrepancies.size())
                .dueDate(calculateDueDate(highestSeverity))
                .build();
        
        incident = incidentRepository.save(incident);
        
        // Link discrepancies to incident
        for (Discrepancy d : discrepancies) {
            d.setIncident(incident);
        }
        discrepancyRepository.saveAll(discrepancies);
        
        // Add initial history entry
        addHistory(incident, null, IncidentStatus.OPEN, "CREATED", "System", 
                "Incident created automatically from reconciliation run");
        
        auditService.logAction("CREATE", "Incident", incident.getId(), null, incidentNumber);
        log.info("Created incident {} with {} discrepancies", incidentNumber, discrepancies.size());
        
        return toDTO(incident);
    }
    
    /**
     * Assign incident to analyst.
     */
    public IncidentDTO assignIncident(Long incidentId, Long userId, String assignedBy) {
        Incident incident = getIncidentEntity(incidentId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        
        IncidentStatus oldStatus = incident.getStatus();
        incident.setAssignedTo(user);
        incident.setAssignedAt(LocalDateTime.now());
        incident.setStatus(IncidentStatus.ASSIGNED);
        
        incident = incidentRepository.save(incident);
        
        addHistory(incident, oldStatus, IncidentStatus.ASSIGNED, "ASSIGNED", assignedBy,
                "Assigned to " + user.getFullName());
        
        auditService.logAction("ASSIGN", "Incident", incidentId, null, user.getUsername());
        log.info("Incident {} assigned to {}", incident.getIncidentNumber(), user.getUsername());
        
        return toDTO(incident);
    }
    
    /**
     * Start investigation (Maker action).
     */
    public IncidentDTO startInvestigation(Long incidentId, String username) {
        Incident incident = getIncidentEntity(incidentId);
        
        validateMakerAction(incident, username);
        
        IncidentStatus oldStatus = incident.getStatus();
        incident.setStatus(IncidentStatus.UNDER_INVESTIGATION);
        incident.setInvestigationStartedAt(LocalDateTime.now());
        
        User maker = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        incident.setMaker(maker);
        
        incident = incidentRepository.save(incident);
        
        addHistory(incident, oldStatus, IncidentStatus.UNDER_INVESTIGATION, "START_INVESTIGATION", 
                username, "Investigation started");
        
        auditService.logAction("START_INVESTIGATION", "Incident", incidentId, null, username);
        log.info("Investigation started for incident {} by {}", incident.getIncidentNumber(), username);
        
        return toDTO(incident);
    }
    
    /**
     * Submit resolution for checker review (Maker action).
     */
    public IncidentDTO submitResolution(Long incidentId, String rootCause, String proposedResolution, 
                                         String resolutionNotes, String username) {
        Incident incident = getIncidentEntity(incidentId);
        
        validateMakerAction(incident, username);
        
        IncidentStatus oldStatus = incident.getStatus();
        incident.setRootCause(rootCause);
        incident.setProposedResolution(proposedResolution);
        incident.setResolutionNotes(resolutionNotes);
        incident.setResolutionProposedAt(LocalDateTime.now());
        incident.setStatus(IncidentStatus.PENDING_CHECKER_REVIEW);
        
        incident = incidentRepository.save(incident);
        
        addHistory(incident, oldStatus, IncidentStatus.PENDING_CHECKER_REVIEW, "SUBMIT_RESOLUTION",
                username, "Resolution submitted for review");
        
        auditService.logAction("SUBMIT_RESOLUTION", "Incident", incidentId, null, username);
        log.info("Resolution submitted for incident {} by {}", incident.getIncidentNumber(), username);
        
        return toDTO(incident);
    }
    
    /**
     * Approve resolution (Checker action).
     */
    public IncidentDTO approveResolution(Long incidentId, String checkerComments, String username) {
        Incident incident = getIncidentEntity(incidentId);
        
        validateCheckerAction(incident, username);
        
        User checker = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        IncidentStatus oldStatus = incident.getStatus();
        incident.setChecker(checker);
        incident.setCheckerComments(checkerComments);
        incident.setResolutionApprovedAt(LocalDateTime.now());
        incident.setStatus(IncidentStatus.RESOLVED);
        
        incident = incidentRepository.save(incident);
        
        addHistory(incident, oldStatus, IncidentStatus.RESOLVED, "APPROVE_RESOLUTION",
                username, "Resolution approved: " + checkerComments);
        
        auditService.logAction("APPROVE_RESOLUTION", "Incident", incidentId, null, username);
        log.info("Resolution approved for incident {} by {}", incident.getIncidentNumber(), username);
        
        return toDTO(incident);
    }
    
    /**
     * Reject resolution (Checker action).
     */
    public IncidentDTO rejectResolution(Long incidentId, String rejectionReason, String username) {
        Incident incident = getIncidentEntity(incidentId);
        
        validateCheckerAction(incident, username);
        
        User checker = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        IncidentStatus oldStatus = incident.getStatus();
        incident.setChecker(checker);
        incident.setRejectionReason(rejectionReason);
        incident.setRejectionCount(incident.getRejectionCount() + 1);
        incident.setStatus(IncidentStatus.CHECKER_REJECTED);
        
        incident = incidentRepository.save(incident);
        
        addHistory(incident, oldStatus, IncidentStatus.CHECKER_REJECTED, "REJECT_RESOLUTION",
                username, "Resolution rejected: " + rejectionReason);
        
        auditService.logAction("REJECT_RESOLUTION", "Incident", incidentId, null, username);
        log.info("Resolution rejected for incident {} by {}", incident.getIncidentNumber(), username);
        
        return toDTO(incident);
    }
    
    /**
     * Close incident.
     */
    public IncidentDTO closeIncident(Long incidentId, String username) {
        Incident incident = getIncidentEntity(incidentId);
        
        if (incident.getStatus() != IncidentStatus.RESOLVED) {
            throw new ReconciliationException("Only resolved incidents can be closed");
        }
        
        IncidentStatus oldStatus = incident.getStatus();
        incident.setStatus(IncidentStatus.CLOSED);
        incident.setClosedAt(LocalDateTime.now());
        
        incident = incidentRepository.save(incident);
        
        addHistory(incident, oldStatus, IncidentStatus.CLOSED, "CLOSE", username, "Incident closed");
        
        auditService.logAction("CLOSE", "Incident", incidentId, null, username);
        log.info("Incident {} closed by {}", incident.getIncidentNumber(), username);
        
        return toDTO(incident);
    }
    
    /**
     * Escalate incident.
     */
    public IncidentDTO escalateIncident(Long incidentId, String escalatedTo, String reason, String username) {
        Incident incident = getIncidentEntity(incidentId);
        
        IncidentStatus oldStatus = incident.getStatus();
        incident.setEscalationLevel(incident.getEscalationLevel() + 1);
        incident.setEscalatedAt(LocalDateTime.now());
        incident.setEscalatedTo(escalatedTo);
        incident.setStatus(IncidentStatus.ESCALATED);
        
        incident = incidentRepository.save(incident);
        
        addHistory(incident, oldStatus, IncidentStatus.ESCALATED, "ESCALATE", username,
                "Escalated to " + escalatedTo + ": " + reason);
        
        auditService.logAction("ESCALATE", "Incident", incidentId, null, username);
        log.info("Incident {} escalated to {} by {}", incident.getIncidentNumber(), escalatedTo, username);
        
        return toDTO(incident);
    }
    
    /**
     * Add comment to incident.
     */
    public IncidentDTO addComment(Long incidentId, IncidentCommentDTO commentDto, String username) {
        Incident incident = getIncidentEntity(incidentId);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        IncidentComment comment = IncidentComment.builder()
                .incident(incident)
                .commentText(commentDto.getCommentText())
                .user(user)
                .isInternal(commentDto.getIsInternal() != null ? commentDto.getIsInternal() : false)
                .attachmentPath(commentDto.getAttachmentPath())
                .build();
        
        incident.addComment(comment);
        incident = incidentRepository.save(incident);
        
        auditService.logAction("ADD_COMMENT", "Incident", incidentId, null, username);
        log.info("Comment added to incident {} by {}", incident.getIncidentNumber(), username);
        
        return toDTO(incident);
    }
    
    /**
     * Get overdue incidents.
     */
    public List<IncidentDTO> getOverdueIncidents() {
        return incidentRepository.findOverdueIncidents(LocalDateTime.now())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get incident statistics.
     */
    public List<Object[]> getIncidentStatsByStatus() {
        return incidentRepository.countByStatus();
    }
    
    // Helper methods
    
    private Incident getIncidentEntity(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + id));
    }
    
    private void validateMakerAction(Incident incident, String username) {
        if (incident.getStatus() != IncidentStatus.ASSIGNED && 
            incident.getStatus() != IncidentStatus.UNDER_INVESTIGATION &&
            incident.getStatus() != IncidentStatus.CHECKER_REJECTED) {
            throw new ReconciliationException("Invalid incident status for maker action");
        }
        
        // Verify user has maker role or is assigned
        if (incident.getAssignedTo() != null && 
            !incident.getAssignedTo().getUsername().equals(username)) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            if (!user.getRoles().contains(UserRole.MAKER) && !user.getRoles().contains(UserRole.ADMIN)) {
                throw new ReconciliationException("User not authorized for maker action");
            }
        }
    }
    
    private void validateCheckerAction(Incident incident, String username) {
        if (incident.getStatus() != IncidentStatus.PENDING_CHECKER_REVIEW) {
            throw new ReconciliationException("Incident is not pending checker review");
        }
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (!user.getRoles().contains(UserRole.CHECKER) && !user.getRoles().contains(UserRole.ADMIN)) {
            throw new ReconciliationException("User not authorized for checker action");
        }
        
        // Ensure maker != checker
        if (incident.getMaker() != null && incident.getMaker().getUsername().equals(username)) {
            throw new ReconciliationException("Maker cannot be the checker for the same incident");
        }
    }
    
    private void addHistory(Incident incident, IncidentStatus fromStatus, IncidentStatus toStatus,
                            String action, String actionBy, String comments) {
        IncidentHistory history = IncidentHistory.builder()
                .incident(incident)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .action(action)
                .actionBy(actionBy)
                .comments(comments)
                .build();
        incident.addHistory(history);
    }
    
    private String generateIncidentNumber() {
        return String.format("INC-%s-%04d",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                incidentRepository.count() + 1);
    }
    
    private LocalDateTime calculateDueDate(DiscrepancySeverity severity) {
        LocalDateTime now = LocalDateTime.now();
        switch (severity) {
            case CRITICAL:
                return now.plusHours(4);
            case HIGH:
                return now.plusHours(24);
            case MEDIUM:
                return now.plusDays(3);
            case LOW:
                return now.plusDays(7);
            default:
                return now.plusDays(14);
        }
    }
    
    private IncidentDTO toDTO(Incident entity) {
        return IncidentDTO.builder()
                .id(entity.getId())
                .incidentNumber(entity.getIncidentNumber())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .severity(entity.getSeverity())
                .reconciliationRunId(entity.getReconciliationRun() != null ? entity.getReconciliationRun().getId() : null)
                .runId(entity.getReconciliationRun() != null ? entity.getReconciliationRun().getRunId() : null)
                .reconciliationConfigId(entity.getReconciliationConfig() != null ? entity.getReconciliationConfig().getId() : null)
                .configCode(entity.getReconciliationConfig() != null ? entity.getReconciliationConfig().getConfigCode() : null)
                .configName(entity.getReconciliationConfig() != null ? entity.getReconciliationConfig().getConfigName() : null)
                .discrepancyCount(entity.getDiscrepancyCount())
                .affectedRecords(entity.getAffectedRecords())
                .assignedToId(entity.getAssignedTo() != null ? entity.getAssignedTo().getId() : null)
                .assignedToName(entity.getAssignedTo() != null ? entity.getAssignedTo().getFullName() : null)
                .makerId(entity.getMaker() != null ? entity.getMaker().getId() : null)
                .makerName(entity.getMaker() != null ? entity.getMaker().getFullName() : null)
                .checkerId(entity.getChecker() != null ? entity.getChecker().getId() : null)
                .checkerName(entity.getChecker() != null ? entity.getChecker().getFullName() : null)
                .assignedAt(formatDateTime(entity.getAssignedAt()))
                .investigationStartedAt(formatDateTime(entity.getInvestigationStartedAt()))
                .resolutionProposedAt(formatDateTime(entity.getResolutionProposedAt()))
                .resolutionApprovedAt(formatDateTime(entity.getResolutionApprovedAt()))
                .closedAt(formatDateTime(entity.getClosedAt()))
                .dueDate(formatDateTime(entity.getDueDate()))
                .slaBreach(entity.getSlaBreach())
                .rootCause(entity.getRootCause())
                .proposedResolution(entity.getProposedResolution())
                .resolutionNotes(entity.getResolutionNotes())
                .checkerComments(entity.getCheckerComments())
                .rejectionReason(entity.getRejectionReason())
                .rejectionCount(entity.getRejectionCount())
                .escalationLevel(entity.getEscalationLevel())
                .escalatedAt(formatDateTime(entity.getEscalatedAt()))
                .escalatedTo(entity.getEscalatedTo())
                .createdAt(formatDateTime(entity.getCreatedAt()))
                .updatedAt(formatDateTime(entity.getUpdatedAt()))
                .createdBy(entity.getCreatedBy())
                .build();
    }
    
    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_FORMATTER) : null;
    }
}

