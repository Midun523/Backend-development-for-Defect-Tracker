package com.defecttracker.modules.defect.service;

import com.defecttracker.common.dto.PageResponse;
import com.defecttracker.common.exception.BusinessRuleException;
import com.defecttracker.common.exception.ResourceNotFoundException;
import com.defecttracker.modules.access.entity.Employee;
import com.defecttracker.modules.access.repository.EmployeeRepository;
import com.defecttracker.modules.defect.dto.DefectDto;
import com.defecttracker.modules.defect.entity.Defect;
import com.defecttracker.modules.defect.entity.DefectComment;
import com.defecttracker.modules.defect.entity.DefectHistory;
import com.defecttracker.modules.defect.entity.ProjectDefectSequence;
import com.defecttracker.modules.defect.repository.DefectCommentRepository;
import com.defecttracker.modules.defect.repository.DefectHistoryRepository;
import com.defecttracker.modules.defect.repository.DefectRepository;
import com.defecttracker.modules.defect.repository.ProjectDefectSequenceRepository;
import com.defecttracker.modules.masterdata.entity.DefectType;
import com.defecttracker.modules.masterdata.entity.Priority;
import com.defecttracker.modules.masterdata.entity.Severity;
import com.defecttracker.modules.masterdata.repository.DefectTypeRepository;
import com.defecttracker.modules.masterdata.repository.PriorityRepository;
import com.defecttracker.modules.masterdata.repository.SeverityRepository;
import com.defecttracker.modules.project.entity.Project;
import com.defecttracker.modules.project.entity.ProjectModule;
import com.defecttracker.modules.project.entity.SubModule;
import com.defecttracker.modules.project.repository.ProjectModuleRepository;
import com.defecttracker.modules.project.repository.ProjectRepository;
import com.defecttracker.modules.project.repository.SubModuleRepository;
import com.defecttracker.modules.release.entity.Release;
import com.defecttracker.modules.release.repository.ReleaseRepository;
import com.defecttracker.modules.testcase.entity.TestCase;
import com.defecttracker.modules.testcase.repository.TestCaseRepository;
import com.defecttracker.modules.workflow.entity.StatusType;
import com.defecttracker.modules.workflow.repository.StatusTypeRepository;
import com.defecttracker.modules.workflow.service.WorkflowService;
import com.defecttracker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefectService {

    private final DefectRepository defectRepository;
    private final ProjectDefectSequenceRepository sequenceRepository;
    private final DefectHistoryRepository historyRepository;
    private final DefectCommentRepository commentRepository;
    private final ProjectRepository projectRepository;
    private final ProjectModuleRepository moduleRepository;
    private final SubModuleRepository subModuleRepository;
    private final SeverityRepository severityRepository;
    private final PriorityRepository priorityRepository;
    private final DefectTypeRepository defectTypeRepository;
    private final StatusTypeRepository statusTypeRepository;
    private final ReleaseRepository releaseRepository;
    private final TestCaseRepository testCaseRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkflowService workflowService;

    /**
     * Create a defect with thread-safe project-scoped sequential numbering (Project A's #1, #2).
     */
    @Transactional
    public DefectDto.DefectResponse createDefect(DefectDto.DefectCreateRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        // Resolve Project-Scoped Sequence Number
        ProjectDefectSequence seq = sequenceRepository.findByProjectIdForUpdate(project.getId())
                .orElseGet(() -> {
                    ProjectDefectSequence newSeq = ProjectDefectSequence.builder()
                            .projectId(project.getId())
                            .currentSeq(0L)
                            .build();
                    return sequenceRepository.saveAndFlush(newSeq);
                });

        long nextSeq = seq.getCurrentSeq() + 1;
        seq.setCurrentSeq(nextSeq);
        sequenceRepository.save(seq);

        String prefix = project.getName().replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (prefix.length() > 5) prefix = prefix.substring(0, 5);
        if (prefix.isEmpty()) prefix = "DEF";
        String defectCode = String.format("%s-%d", prefix, nextSeq);

        // Status resolution (initial default status if not provided)
        StatusType status;
        if (request.getStatusId() != null) {
            status = statusTypeRepository.findById(request.getStatusId())
                    .orElseThrow(() -> new ResourceNotFoundException("StatusType", "id", request.getStatusId()));
        } else {
            status = statusTypeRepository.findByDefaultInitialTrue()
                    .orElseGet(() -> statusTypeRepository.findAll().stream().findFirst()
                            .orElseThrow(() -> new BusinessRuleException("No statuses configured in system")));
        }

        Employee reporter = getCurrentEmployee();
        Employee assignedTo = null;
        LocalDateTime firstAssignedAt = null;
        if (request.getAssignedToId() != null) {
            assignedTo = employeeRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getAssignedToId()));
            firstAssignedAt = LocalDateTime.now();
        }

        ProjectModule module = null;
        if (request.getModuleId() != null) {
            module = moduleRepository.findById(request.getModuleId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProjectModule", "id", request.getModuleId()));
        }

        SubModule subModule = null;
        if (request.getSubModuleId() != null) {
            subModule = subModuleRepository.findById(request.getSubModuleId())
                    .orElseThrow(() -> new ResourceNotFoundException("SubModule", "id", request.getSubModuleId()));
        }

        Severity severity = null;
        if (request.getSeverityId() != null) {
            severity = severityRepository.findById(request.getSeverityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Severity", "id", request.getSeverityId()));
        }

        Priority priority = null;
        if (request.getPriorityId() != null) {
            priority = priorityRepository.findById(request.getPriorityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Priority", "id", request.getPriorityId()));
        }

        DefectType defectType = null;
        if (request.getDefectTypeId() != null) {
            defectType = defectTypeRepository.findById(request.getDefectTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("DefectType", "id", request.getDefectTypeId()));
        }

        Release release = null;
        if (request.getReleaseId() != null) {
            release = releaseRepository.findById(request.getReleaseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Release", "id", request.getReleaseId()));
        }

        TestCase testCase = null;
        if (request.getTestCaseId() != null) {
            testCase = testCaseRepository.findById(request.getTestCaseId())
                    .orElseThrow(() -> new ResourceNotFoundException("TestCase", "id", request.getTestCaseId()));
        }

        Defect defect = Defect.builder()
                .project(project)
                .projectSeqNum(nextSeq)
                .defectCode(defectCode)
                .title(request.getTitle())
                .description(request.getDescription())
                .stepsToReproduce(request.getStepsToReproduce())
                .attachmentUrl(request.getAttachmentUrl())
                .module(module)
                .subModule(subModule)
                .severity(severity)
                .priority(priority)
                .defectType(defectType)
                .status(status)
                .assignedTo(assignedTo)
                .reporter(reporter)
                .release(release)
                .testCase(testCase)
                .reopenCounter(0)
                .firstAssignedAt(firstAssignedAt)
                .active(true)
                .build();

        Defect saved = defectRepository.save(defect);

        // Initial immutable history entry
        DefectHistory initialHistory = DefectHistory.builder()
                .defect(saved)
                .fromStatus(null)
                .fromStatusName(null)
                .toStatus(status)
                .toStatusName(status.getName())
                .changedBy(reporter)
                .changedByName(reporter != null ? reporter.getFullName() : "SYSTEM")
                .note("Defect created with initial status: " + status.getName())
                .build();
        historyRepository.save(initialHistory);

        log.info("Created defect {} (#{}) for project {}", defectCode, nextSeq, project.getName());
        return mapDefect(saved);
    }

    /**
     * Transition Defect Status:
     * - Validates transition against the configurable workflow transition graph
     * - Increments reopen counter if moving from resolved stage to open stage
     * - Appends immutable history log
     * - Records resolvedAt timestamp if entering resolved stage
     */
    @Transactional
    public DefectDto.DefectResponse changeStatus(Long defectId, DefectDto.StatusChangeRequest request) {
        Defect defect = defectRepository.findById(defectId)
                .orElseThrow(() -> new ResourceNotFoundException("Defect", "id", defectId));

        StatusType currentStatus = defect.getStatus();
        StatusType newStatus = statusTypeRepository.findById(request.getNewStatusId())
                .orElseThrow(() -> new ResourceNotFoundException("StatusType", "id", request.getNewStatusId()));

        if (currentStatus.getId().equals(newStatus.getId())) {
            return mapDefect(defect);
        }

        // Validate against configurable workflow transition graph
        workflowService.validateStatusTransition(currentStatus.getId(), newStatus.getId());

        // Check for reopen
        boolean isReopen = workflowService.isReopenTransition(currentStatus.getId(), newStatus.getId());
        if (isReopen) {
            defect.setReopenCounter(defect.getReopenCounter() + 1);
            log.info("Defect {} reopened! Reopen counter now: {}", defect.getDefectCode(), defect.getReopenCounter());
        }

        // Record resolution timestamp
        if (newStatus.isResolvedStage() && defect.getResolvedAt() == null) {
            defect.setResolvedAt(LocalDateTime.now());
        }

        defect.setStatus(newStatus);
        Defect updated = defectRepository.save(defect);

        // Append to immutable history log
        Employee actor = getCurrentEmployee();
        DefectHistory history = DefectHistory.builder()
                .defect(updated)
                .fromStatus(currentStatus)
                .fromStatusName(currentStatus.getName())
                .toStatus(newStatus)
                .toStatusName(newStatus.getName())
                .changedBy(actor)
                .changedByName(actor != null ? actor.getFullName() : "SYSTEM")
                .note(request.getNote() != null ? request.getNote() : (isReopen ? "Defect Reopened" : "Status changed to " + newStatus.getName()))
                .build();
        historyRepository.save(history);

        return mapDefect(updated);
    }

    @Transactional(readOnly = true)
    public PageResponse<DefectDto.DefectResponse> getFilteredDefects(
            Long projectId, Long statusId, Long severityId, Long priorityId,
            Long assignedToId, Long releaseId, String query, Pageable pageable) {

        Page<Defect> page = defectRepository.findFilteredDefects(
                projectId, statusId, severityId, priorityId, assignedToId, releaseId,
                (query != null && !query.trim().isEmpty()) ? query.trim() : null,
                pageable
        );
        return PageResponse.of(page.map(this::mapDefect));
    }

    @Transactional(readOnly = true)
    public DefectDto.DefectResponse getDefectById(Long id) {
        Defect defect = defectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Defect", "id", id));
        return mapDefect(defect);
    }

    @Transactional
    public DefectDto.DefectResponse updateDefect(Long id, DefectDto.DefectUpdateRequest request) {
        Defect defect = defectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Defect", "id", id));

        defect.setTitle(request.getTitle());
        defect.setDescription(request.getDescription());
        defect.setStepsToReproduce(request.getStepsToReproduce());
        if (request.getAttachmentUrl() != null) defect.setAttachmentUrl(request.getAttachmentUrl());

        if (request.getModuleId() != null) {
            defect.setModule(moduleRepository.findById(request.getModuleId()).orElse(null));
        }
        if (request.getSubModuleId() != null) {
            defect.setSubModule(subModuleRepository.findById(request.getSubModuleId()).orElse(null));
        }
        if (request.getSeverityId() != null) {
            defect.setSeverity(severityRepository.findById(request.getSeverityId()).orElse(null));
        }
        if (request.getPriorityId() != null) {
            defect.setPriority(priorityRepository.findById(request.getPriorityId()).orElse(null));
        }
        if (request.getDefectTypeId() != null) {
            defect.setDefectType(defectTypeRepository.findById(request.getDefectTypeId()).orElse(null));
        }
        if (request.getReleaseId() != null) {
            defect.setRelease(releaseRepository.findById(request.getReleaseId()).orElse(null));
        }
        if (request.getTestCaseId() != null) {
            defect.setTestCase(testCaseRepository.findById(request.getTestCaseId()).orElse(null));
        }

        if (request.getAssignedToId() != null) {
            Employee dev = employeeRepository.findById(request.getAssignedToId()).orElse(null);
            defect.setAssignedTo(dev);
            if (defect.getFirstAssignedAt() == null) {
                defect.setFirstAssignedAt(LocalDateTime.now());
            }
        }

        return mapDefect(defectRepository.save(defect));
    }

    @Transactional(readOnly = true)
    public List<DefectDto.DefectHistoryDto> getDefectHistory(Long defectId) {
        return historyRepository.findByDefectIdOrderByCreatedAtAsc(defectId).stream()
                .map(h -> DefectDto.DefectHistoryDto.builder()
                        .id(h.getId())
                        .fromStatusId(h.getFromStatus() != null ? h.getFromStatus().getId() : null)
                        .fromStatusName(h.getFromStatusName())
                        .toStatusId(h.getToStatus().getId())
                        .toStatusName(h.getToStatusName())
                        .changedById(h.getChangedBy() != null ? h.getChangedBy().getId() : null)
                        .changedByName(h.getChangedByName())
                        .note(h.getNote())
                        .createdAt(h.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // --- Threaded Comments ---
    @Transactional
    public DefectDto.CommentResponse addComment(Long defectId, DefectDto.CommentCreateRequest request) {
        Defect defect = defectRepository.findById(defectId)
                .orElseThrow(() -> new ResourceNotFoundException("Defect", "id", defectId));

        DefectComment parent = null;
        if (request.getParentCommentId() != null) {
            parent = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("DefectComment", "id", request.getParentCommentId()));
        }

        Employee author = getCurrentEmployee();

        DefectComment comment = DefectComment.builder()
                .defect(defect)
                .parentComment(parent)
                .author(author)
                .authorName(author != null ? author.getFullName() : "Anonymous")
                .commentText(request.getCommentText())
                .build();

        return mapComment(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public List<DefectDto.CommentResponse> getDefectComments(Long defectId) {
        // Return top-level comments with hierarchical replies populated
        List<DefectComment> topComments = commentRepository
                .findByDefectIdAndParentCommentIsNullOrderByCreatedAtAsc(defectId);
        return topComments.stream().map(this::mapCommentWithReplies).collect(Collectors.toList());
    }

    // --- Bulk Operations ---
    @Transactional
    public int bulkReassign(DefectDto.BulkReassignRequest request) {
        Employee dev = employeeRepository.findById(request.getNewAssignedToId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getNewAssignedToId()));

        int updatedCount = 0;
        for (Long id : request.getDefectIds()) {
            Defect defect = defectRepository.findById(id).orElse(null);
            if (defect != null) {
                defect.setAssignedTo(dev);
                if (defect.getFirstAssignedAt() == null) {
                    defect.setFirstAssignedAt(LocalDateTime.now());
                }
                defectRepository.save(defect);
                updatedCount++;
            }
        }
        return updatedCount;
    }

    // --- Analytics & Metrics ---
    @Transactional(readOnly = true)
    public DefectDto.DefectAnalyticsReport getProjectAnalytics(Long projectId, LocalDateTime windowStart, LocalDateTime windowEnd) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        long totalDefects = defectRepository.countByProjectIdAndActiveTrue(projectId);

        // Severity Breakdown
        Map<String, Long> severityMap = new LinkedHashMap<>();
        for (Object[] row : defectRepository.countBySeverityForProject(projectId)) {
            if (row[0] != null) severityMap.put((String) row[0], (Long) row[1]);
        }

        // Module Breakdown
        Map<String, Long> moduleMap = new LinkedHashMap<>();
        for (Object[] row : defectRepository.countByModuleForProject(projectId)) {
            if (row[0] != null) moduleMap.put((String) row[0], (Long) row[1]);
        }

        // Defect Type Breakdown
        Map<String, Long> typeMap = new LinkedHashMap<>();
        for (Object[] row : defectRepository.countByTypeForProject(projectId)) {
            if (row[0] != null) typeMap.put((String) row[0], (Long) row[1]);
        }

        // Window created vs fixed
        LocalDateTime start = windowStart != null ? windowStart : LocalDateTime.now().minusDays(30);
        LocalDateTime end = windowEnd != null ? windowEnd : LocalDateTime.now();
        long createdInWindow = defectRepository.countCreatedBetween(projectId, start, end);
        long fixedInWindow = defectRepository.countFixedBetween(projectId, start, end);

        // Time to find metric (creation to first assignment)
        List<Defect> assignedDefects = defectRepository.findAssignedDefectsForProject(projectId);
        double avgTimeToFindHours = 0.0;
        if (!assignedDefects.isEmpty()) {
            double totalHours = 0;
            for (Defect d : assignedDefects) {
                totalHours += Duration.between(d.getCreatedAt(), d.getFirstAssignedAt()).toMinutes() / 60.0;
            }
            avgTimeToFindHours = Math.round((totalHours / assignedDefects.size()) * 100.0) / 100.0;
        }

        // Time to fix metric (creation to resolution)
        List<Defect> resolvedDefects = defectRepository.findResolvedDefectsForProject(projectId);
        double avgTimeToFixHours = 0.0;
        if (!resolvedDefects.isEmpty()) {
            double totalHours = 0;
            for (Defect d : resolvedDefects) {
                totalHours += Duration.between(d.getCreatedAt(), d.getResolvedAt()).toMinutes() / 60.0;
            }
            avgTimeToFixHours = Math.round((totalHours / resolvedDefects.size()) * 100.0) / 100.0;
        }

        // Defect Density: total defects / KLOC
        double klocVal = project.getKloc() != null ? project.getKloc().doubleValue() : 0.0;
        double defectDensity = klocVal > 0 ? Math.round((totalDefects / klocVal) * 100.0) / 100.0 : 0.0;

        return DefectDto.DefectAnalyticsReport.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .totalDefects(totalDefects)
                .severityBreakdown(severityMap)
                .moduleBreakdown(moduleMap)
                .defectTypeBreakdown(typeMap)
                .createdInWindow(createdInWindow)
                .fixedInWindow(fixedInWindow)
                .avgTimeToFindHours(avgTimeToFindHours)
                .avgTimeToFixHours(avgTimeToFixHours)
                .kloc(klocVal)
                .defectDensity(defectDensity)
                .build();
    }

    private Employee getCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return employeeRepository.findById(principal.getId()).orElse(null);
        }
        return null;
    }

    private DefectDto.DefectResponse mapDefect(Defect d) {
        return DefectDto.DefectResponse.builder()
                .id(d.getId())
                .projectId(d.getProject().getId())
                .projectName(d.getProject().getName())
                .projectSeqNum(d.getProjectSeqNum())
                .defectCode(d.getDefectCode())
                .title(d.getTitle())
                .description(d.getDescription())
                .stepsToReproduce(d.getStepsToReproduce())
                .attachmentUrl(d.getAttachmentUrl())
                .moduleId(d.getModule() != null ? d.getModule().getId() : null)
                .moduleName(d.getModule() != null ? d.getModule().getName() : null)
                .subModuleId(d.getSubModule() != null ? d.getSubModule().getId() : null)
                .subModuleName(d.getSubModule() != null ? d.getSubModule().getName() : null)
                .defectTypeId(d.getDefectType() != null ? d.getDefectType().getId() : null)
                .defectTypeName(d.getDefectType() != null ? d.getDefectType().getName() : null)
                .severityId(d.getSeverity() != null ? d.getSeverity().getId() : null)
                .severityName(d.getSeverity() != null ? d.getSeverity().getName() : null)
                .severityColor(d.getSeverity() != null ? d.getSeverity().getColorCode() : null)
                .priorityId(d.getPriority() != null ? d.getPriority().getId() : null)
                .priorityName(d.getPriority() != null ? d.getPriority().getName() : null)
                .priorityColor(d.getPriority() != null ? d.getPriority().getColorCode() : null)
                .statusId(d.getStatus().getId())
                .statusName(d.getStatus().getName())
                .statusColor(d.getStatus().getDisplayColor())
                .statusCategory(d.getStatus().getCategory())
                .assignedToId(d.getAssignedTo() != null ? d.getAssignedTo().getId() : null)
                .assignedToName(d.getAssignedTo() != null ? d.getAssignedTo().getFullName() : null)
                .reporterId(d.getReporter() != null ? d.getReporter().getId() : null)
                .reporterName(d.getReporter() != null ? d.getReporter().getFullName() : null)
                .releaseId(d.getRelease() != null ? d.getRelease().getId() : null)
                .releaseVersion(d.getRelease() != null ? d.getRelease().getVersion() : null)
                .testCaseId(d.getTestCase() != null ? d.getTestCase().getId() : null)
                .testCaseCode(d.getTestCase() != null ? d.getTestCase().getTestCaseCode() : null)
                .reopenCounter(d.getReopenCounter())
                .firstAssignedAt(d.getFirstAssignedAt())
                .resolvedAt(d.getResolvedAt())
                .active(d.isActive())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    private DefectDto.CommentResponse mapComment(DefectComment c) {
        return DefectDto.CommentResponse.builder()
                .id(c.getId())
                .defectId(c.getDefect().getId())
                .authorId(c.getAuthor() != null ? c.getAuthor().getId() : null)
                .authorName(c.getAuthorName())
                .commentText(c.getCommentText())
                .parentCommentId(c.getParentComment() != null ? c.getParentComment().getId() : null)
                .createdAt(c.getCreatedAt())
                .build();
    }

    private DefectDto.CommentResponse mapCommentWithReplies(DefectComment c) {
        DefectDto.CommentResponse resp = mapComment(c);
        if (c.getReplies() != null && !c.getReplies().isEmpty()) {
            resp.setReplies(c.getReplies().stream().map(this::mapCommentWithReplies).collect(Collectors.toList()));
        }
        return resp;
    }
}
