package com.defecttracker.service.impl;

import com.defecttracker.dto.request.DefectBulkReassignRequest;
import com.defecttracker.dto.request.DefectCommentRequest;
import com.defecttracker.dto.request.DefectCreateRequest;
import com.defecttracker.dto.request.DefectStatusChangeRequest;
import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.entity.*;
import com.defecttracker.entity.Module;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.repository.*;
import com.defecttracker.service.DefectService;
import com.defecttracker.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefectServiceImpl implements DefectService {

    private final DefectRepository defectRepository;
    private final ProjectRepository projectRepository;
    private final ReleaseRepository releaseRepository;
    private final ModuleRepository moduleRepository;
    private final SubModuleRepository subModuleRepository;
    private final TestCaseRepository testCaseRepository;
    private final PriorityRepository priorityRepository;
    private final SeverityRepository severityRepository;
    private final StatusTypeRepository statusTypeRepository;
    private final DefectTypeRepository defectTypeRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DefectHistoryRepository defectHistoryRepository;
    private final DefectCommentRepository defectCommentRepository;
    private final DefectStatusLogRepository defectStatusLogRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public Defect createDefect(DefectCreateRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        Release release = null;
        if (request.getEffectiveReleaseId() != null) {
            release = releaseRepository.findById(request.getEffectiveReleaseId()).orElse(null);
        }

        Module module = null;
        if (request.getEffectiveModuleId() != null) {
            module = moduleRepository.findById(request.getEffectiveModuleId()).orElse(null);
        }

        SubModule subModule = null;
        if (request.getSubModuleId() != null) {
            subModule = subModuleRepository.findById(request.getSubModuleId()).orElse(null);
        }

        TestCase testCase = null;
        if (request.getTestCaseId() != null) {
            testCase = testCaseRepository.findById(request.getTestCaseId()).orElse(null);
        }

        Priority priority = null;
        if (request.getPriorityId() != null) {
            priority = priorityRepository.findById(request.getPriorityId()).orElse(null);
        } else {
            priority = priorityRepository.findByName("Medium").orElse(null);
        }

        Severity severity = null;
        if (request.getSeverityId() != null) {
            severity = severityRepository.findById(request.getSeverityId()).orElse(null);
        } else {
            severity = severityRepository.findByName("Medium").orElse(null);
        }

        StatusType status = null;
        if (request.getDefectStatusId() != null) {
            status = statusTypeRepository.findById(request.getDefectStatusId()).orElse(null);
        } else {
            status = statusTypeRepository.findByName("New").orElseGet(() -> statusTypeRepository.findAll().stream().findFirst().orElse(null));
        }

        DefectType defectType = null;
        if (request.getTypeId() != null) {
            defectType = defectTypeRepository.findById(request.getTypeId()).orElse(null);
        }

        Employee assignedTo = null;
        if (request.getEffectiveAssignedToId() != null) {
            assignedTo = employeeRepository.findById(request.getEffectiveAssignedToId()).orElse(null);
        }

        Employee assignedBy = null;
        if (request.getEffectiveAssignedById() != null) {
            assignedBy = employeeRepository.findById(request.getEffectiveAssignedById()).orElse(null);
        }

        long count = defectRepository.count() + 1;
        String defectId = String.format("DEF%03d", count);

        Defect defect = Defect.builder()
                .defectId(defectId)
                .title(request.getEffectiveTitle())
                .description(request.getDescription())
                .steps(request.getSteps())
                .priority(priority)
                .severity(severity)
                .defectStatus(status)
                .defectType(defectType)
                .project(project)
                .release(release)
                .module(module)
                .subModule(subModule)
                .testCase(testCase)
                .reOpenCount(request.getReOpenCount() != null ? request.getReOpenCount() : 0)
                .attachment(request.getAttachment())
                .assignedTo(assignedTo)
                .assignedBy(assignedBy)
                .reportedBy(assignedBy != null ? assignedBy.getFirstName() + " " + assignedBy.getLastName() : "System")
                .build();

        Defect saved = defectRepository.save(defect);

        // Initial history entry
        DefectHistory history = DefectHistory.builder()
                .defect(saved)
                .fromStatus("NONE")
                .toStatus(status != null ? status.getName() : "New")
                .changedBy(saved.getReportedBy())
                .comment("Defect created")
                .build();
        defectHistoryRepository.save(history);

        // Initial status log
        DefectStatusLog statusLog = DefectStatusLog.builder()
                .project(project)
                .release(release)
                .defect(saved)
                .status(status != null ? status.getName() : "New")
                .build();
        defectStatusLogRepository.save(statusLog);

        if (assignedTo != null && assignedTo.getEmail() != null) {
            Map<String, String> vars = new HashMap<>();
            vars.put("name", assignedTo.getFirstName());
            vars.put("defectId", saved.getDefectId());
            vars.put("projectName", project.getName());
            emailService.sendTemplatedEmail(assignedTo.getEmail(), "DEFECT_ASSIGNED", vars);
        }

        return saved;
    }

    @Override
    @Transactional
    public Defect updateDefect(Long id, DefectCreateRequest request) {
        Defect defect = getDefectById(id);

        if (request.getTitle() != null) defect.setTitle(request.getTitle());
        if (request.getDescription() != null) defect.setDescription(request.getDescription());
        if (request.getSteps() != null) defect.setSteps(request.getSteps());
        if (request.getAttachment() != null) defect.setAttachment(request.getAttachment());

        if (request.getPriorityId() != null) {
            priorityRepository.findById(request.getPriorityId()).ifPresent(defect::setPriority);
        }
        if (request.getSeverityId() != null) {
            severityRepository.findById(request.getSeverityId()).ifPresent(defect::setSeverity);
        }
        if (request.getTypeId() != null) {
            defectTypeRepository.findById(request.getTypeId()).ifPresent(defect::setDefectType);
        }
        if (request.getEffectiveAssignedToId() != null) {
            employeeRepository.findById(request.getEffectiveAssignedToId()).ifPresent(defect::setAssignedTo);
        }

        return defectRepository.save(defect);
    }

    @Override
    public Defect getDefectById(Long id) {
        return defectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Defect", "id", id));
    }

    @Override
    public List<Defect> getDefectsByProject(Long projectId) {
        return defectRepository.findByProjectId(projectId);
    }

    @Override
    public PaginatedResponse<Defect> filterDefects(Long projectId, Long releaseId, Long severityId, Long priorityId, Long statusId, Long typeId, Long moduleId, Long subModuleId, Long assignedToId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by("id").descending());
        Page<Defect> defectPage = defectRepository.filterDefects(projectId, releaseId, severityId, priorityId, statusId, typeId, moduleId, subModuleId, assignedToId, pageable);

        return PaginatedResponse.<Defect>builder()
                .content(defectPage.getContent())
                .pageNumber(defectPage.getNumber())
                .pageSize(defectPage.getSize())
                .totalElements(defectPage.getTotalElements())
                .totalPages(defectPage.getTotalPages())
                .first(defectPage.isFirst())
                .last(defectPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public Defect changeDefectStatus(Long defectId, DefectStatusChangeRequest request, String changedByUser) {
        Defect defect = getDefectById(defectId);
        String fromStatus = defect.getDefectStatus() != null ? defect.getDefectStatus().getName() : "Unknown";

        StatusType newStatus = null;
        if (request.getDefectStatusId() != null) {
            newStatus = statusTypeRepository.findById(request.getDefectStatusId()).orElse(null);
        } else if (request.getEffectiveStatus() != null) {
            newStatus = statusTypeRepository.findByNameIgnoreCase(request.getEffectiveStatus()).orElse(null);
        }

        if (newStatus != null) {
            defect.setDefectStatus(newStatus);
            if ("Reopened".equalsIgnoreCase(newStatus.getName()) || "reopen".equalsIgnoreCase(newStatus.getName())) {
                defect.setReOpenCount(defect.getReOpenCount() + 1);
            }
        }

        Defect saved = defectRepository.save(defect);

        DefectHistory history = DefectHistory.builder()
                .defect(saved)
                .fromStatus(fromStatus)
                .toStatus(newStatus != null ? newStatus.getName() : request.getEffectiveStatus())
                .changedBy(changedByUser != null ? changedByUser : "User")
                .comment(request.getComment())
                .build();
        defectHistoryRepository.save(history);

        DefectStatusLog logEntry = DefectStatusLog.builder()
                .project(defect.getProject())
                .release(defect.getRelease())
                .defect(saved)
                .status(newStatus != null ? newStatus.getName() : request.getEffectiveStatus())
                .build();
        defectStatusLogRepository.save(logEntry);

        return saved;
    }

    @Override
    @Transactional
    public Defect assignDeveloper(Long defectId, Long employeeId) {
        Defect defect = getDefectById(defectId);
        Employee dev = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        defect.setAssignedTo(dev);
        return defectRepository.save(defect);
    }

    @Override
    @Transactional
    public void bulkReassign(DefectBulkReassignRequest request) {
        Employee dev = employeeRepository.findById(request.getAssignedToId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getAssignedToId()));

        for (Long defectId : request.getDefectIds()) {
            defectRepository.findById(defectId).ifPresent(d -> {
                d.setAssignedTo(dev);
                defectRepository.save(d);
            });
        }
    }

    @Override
    public void deleteDefect(Long id) {
        Defect defect = getDefectById(id);
        defectRepository.delete(defect);
    }

    @Override
    @Transactional
    public DefectComment addComment(Long defectId, DefectCommentRequest request, String userEmail) {
        Defect defect = getDefectById(defectId);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        DefectComment comment = DefectComment.builder()
                .defect(defect)
                .user(user)
                .comment(request.getComment())
                .build();

        return defectCommentRepository.save(comment);
    }

    @Override
    public List<DefectComment> getCommentsByDefect(Long defectId) {
        return defectCommentRepository.findByDefectIdOrderByCreatedAtAsc(defectId);
    }

    @Override
    public List<DefectHistory> getDefectHistory(Long defectId) {
        return defectHistoryRepository.findByDefectIdOrderByChangedAtDesc(defectId);
    }

    @Override
    public List<DefectStatusLog> getDefectStatusLogs(Long projectId, Long releaseId) {
        return defectStatusLogRepository.findByProjectIdAndReleaseId(projectId, releaseId);
    }
}
