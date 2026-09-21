package com.defecttracker.service.impl;

import com.defecttracker.dto.request.DefectBulkReassignRequest;
import com.defecttracker.dto.request.DefectCommentRequest;
import com.defecttracker.dto.request.DefectCreateRequest;
import com.defecttracker.dto.request.DefectStatusChangeRequest;
import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.entity.*;
import com.defecttracker.entity.Module;
import com.defecttracker.exception.BadRequestException;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.repository.*;
import com.defecttracker.service.DefectService;
import com.defecttracker.service.EmailService;
import com.defecttracker.service.ProjectSequenceService;
import com.defecttracker.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@Transactional
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
    private final ProjectAllocationRepository projectAllocationRepository;
    private final ProjectSequenceService projectSequenceService;
    private final EmailService emailService;

    @Override
    @Transactional
    public Defect createDefect(DefectCreateRequest request) {
        Project project = null;
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));
        }

        Release release = null;
        if (request.getEffectiveReleaseId() != null) {
            release = releaseRepository.findById(request.getEffectiveReleaseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Release", "id", request.getEffectiveReleaseId()));
        }

        Module module = null;
        if (request.getEffectiveModuleId() != null) {
            module = moduleRepository.findById(request.getEffectiveModuleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Module", "id", request.getEffectiveModuleId()));
        }

        SubModule subModule = null;
        if (request.getSubModuleId() != null) {
            subModule = subModuleRepository.findById(request.getSubModuleId())
                    .orElseThrow(() -> new ResourceNotFoundException("SubModule", "id", request.getSubModuleId()));
        }

        // Validate subModule in module
        if (subModule != null) {
            if (module != null && subModule.getModule() != null && !subModule.getModule().getId().equals(module.getId())) {
                throw new BadRequestException("SubModule " + subModule.getId() + " does not belong to Module " + module.getId());
            }
            if (module == null && subModule.getModule() != null) {
                module = subModule.getModule();
            }
        }

        // ProjectId required or derived only if module/subModule/release agree
        if (project == null) {
            Set<Long> candidateProjectIds = new HashSet<>();
            if (release != null && release.getProject() != null) {
                candidateProjectIds.add(release.getProject().getId());
            }
            if (module != null && module.getProject() != null) {
                candidateProjectIds.add(module.getProject().getId());
            }
            if (subModule != null && subModule.getModule() != null && subModule.getModule().getProject() != null) {
                candidateProjectIds.add(subModule.getModule().getProject().getId());
            }

            if (candidateProjectIds.isEmpty()) {
                throw new BadRequestException("Project ID is required");
            }
            if (candidateProjectIds.size() > 1) {
                throw new BadRequestException("Conflicting project associations among release, module, and subModule");
            }
            Long derivedProjectId = candidateProjectIds.iterator().next();
            project = projectRepository.findById(derivedProjectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", derivedProjectId));
        }

        // Validate module in project
        if (module != null && module.getProject() != null && !module.getProject().getId().equals(project.getId())) {
            throw new BadRequestException("Module does not belong to project " + project.getId());
        }

        // Validate subModule in project
        if (subModule != null && subModule.getModule() != null && subModule.getModule().getProject() != null
                && !subModule.getModule().getProject().getId().equals(project.getId())) {
            throw new BadRequestException("SubModule does not belong to project " + project.getId());
        }

        // Validate release in project
        if (release != null && release.getProject() != null && !release.getProject().getId().equals(project.getId())) {
            throw new BadRequestException("Release does not belong to project " + project.getId());
        }

        // Validate testCase in project
        TestCase testCase = null;
        if (request.getTestCaseId() != null) {
            testCase = testCaseRepository.findById(request.getTestCaseId())
                    .orElseThrow(() -> new ResourceNotFoundException("TestCase", "id", request.getTestCaseId()));
            Project tcProject = (testCase.getSubModule() != null && testCase.getSubModule().getModule() != null)
                    ? testCase.getSubModule().getModule().getProject()
                    : null;
            if (tcProject != null && !tcProject.getId().equals(project.getId())) {
                throw new BadRequestException("TestCase does not belong to project " + project.getId());
            }
        }

        // Validate assignee ACTIVE and allocated to the project
        Employee assignedTo = null;
        if (request.getEffectiveAssignedToId() != null) {
            Long empId = request.getEffectiveAssignedToId();
            assignedTo = employeeRepository.findById(empId)
                    .orElseGet(() -> employeeRepository.findByUserId(empId)
                            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", empId)));
            if (assignedTo.getStatus() == null || !assignedTo.getStatus().equalsIgnoreCase("ACTIVE")) {
                throw new BadRequestException("Assignee is not ACTIVE");
            }
            boolean allocated = projectAllocationRepository.findByProjectIdAndEmployeeIdAndStatus(project.getId(), assignedTo.getId(), "ACTIVE").isPresent();
            if (!allocated) {
                throw new BadRequestException("Assignee is not allocated to project " + project.getId());
            }
        }

        Employee assignedBy = null;
        if (request.getEffectiveAssignedById() != null) {
            Long empId = request.getEffectiveAssignedById();
            assignedBy = employeeRepository.findById(empId)
                    .orElseGet(() -> employeeRepository.findByUserId(empId)
                            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", empId)));
        }

        // Default Priority = lowest id
        Priority priority;
        if (request.getPriorityId() != null) {
            priority = priorityRepository.findById(request.getPriorityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Priority", "id", request.getPriorityId()));
        } else {
            priority = priorityRepository.findTopByOrderByIdAsc()
                    .orElseThrow(() -> new ResourceNotFoundException("Priority", "default", "none found"));
        }

        // Default Severity = lowest weight (else lowest id)
        Severity severity;
        if (request.getSeverityId() != null) {
            severity = severityRepository.findById(request.getSeverityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Severity", "id", request.getSeverityId()));
        } else {
            severity = severityRepository.findTopByOrderByWeightAscIdAsc()
                    .or(severityRepository::findTopByOrderByIdAsc)
                    .orElseThrow(() -> new ResourceNotFoundException("Severity", "default", "none found"));
        }

        // Default Status = workflow start status (else lowest id)
        StatusType status;
        if (request.getEffectiveStatusId() != null) {
            status = statusTypeRepository.findById(request.getEffectiveStatusId())
                    .orElseThrow(() -> new ResourceNotFoundException("StatusType", "id", request.getEffectiveStatusId()));
        } else {
            status = statusTypeRepository.findFirstByIsDefaultTrue()
                    .or(statusTypeRepository::findTopByOrderByIdAsc)
                    .orElseThrow(() -> new ResourceNotFoundException("StatusType", "default", "none found"));
        }

        DefectType defectType = null;
        Long effectiveTypeId = request.getEffectiveTypeId();
        if (effectiveTypeId != null) {
            defectType = defectTypeRepository.findById(effectiveTypeId)
                    .orElseThrow(() -> new ResourceNotFoundException("DefectType", "id", effectiveTypeId));
        }

        // Per-project sequences (pessimistic lock)
        String defectId = projectSequenceService.getNextDefectNumber(project.getId());

        Defect defect = Defect.builder()
                .defectId(defectId)
                .title(request.getEffectiveTitle())
                .description(request.getDescription())
                .steps(request.getEffectiveSteps())
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
                .testCaseRequired(request.getEffectiveTestCaseRequired() != null ? request.getEffectiveTestCaseRequired() : false)
                .attachment(request.getAttachment())
                .assignedTo(assignedTo)
                .assignedBy(assignedBy)
                .reportedBy(assignedBy != null ? assignedBy.getFirstName() + " " + assignedBy.getLastName() : "System")
                .build();

        Defect saved = defectRepository.save(defect);

        // Auto-create TestCase if testCaseRequired is true
        Boolean tcRequired = request.getEffectiveTestCaseRequired();
        if (Boolean.TRUE.equals(tcRequired) && subModule != null) {
            String tcNo = projectSequenceService.getNextTestCaseNumber(project.getId());
            TestCase autoTestCase = TestCase.builder()
                    .testcaseNo(tcNo)
                    .description(request.getDescription() != null ? request.getDescription() : saved.getTitle())
                    .detailsSteps(request.getEffectiveSteps())
                    .expectedResult("")
                    .subModule(subModule)
                    .severity(severity)
                    .defectType(defectType)
                    .executionStatus("NOT_RUN")
                    .createdBy(saved.getReportedBy())
                    .build();
            TestCase savedTc = testCaseRepository.save(autoTestCase);
            saved.setTestCase(savedTc);
            saved = defectRepository.save(saved);
            log.info("Auto-created TestCase {} for Defect {}", savedTc.getTestcaseNo(), saved.getDefectId());
        }

        // Initial history entry
        DefectHistory history = DefectHistory.builder()
                .defect(saved)
                .fromStatus("NONE")
                .toStatus(status.getName())
                .changedBy(saved.getReportedBy())
                .comment("Defect created")
                .build();
        defectHistoryRepository.save(history);

        // Initial status log
        DefectStatusLog statusLog = DefectStatusLog.builder()
                .project(project)
                .release(release)
                .defect(saved)
                .status(status.getName())
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
            defect.setPriority(priorityRepository.findById(request.getPriorityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Priority", "id", request.getPriorityId())));
        }
        if (request.getSeverityId() != null) {
            defect.setSeverity(severityRepository.findById(request.getSeverityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Severity", "id", request.getSeverityId())));
        }
        if (request.getTypeId() != null) {
            defect.setDefectType(defectTypeRepository.findById(request.getTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("DefectType", "id", request.getTypeId())));
        }
        if (request.getEffectiveAssignedToId() != null) {
            Long empId = request.getEffectiveAssignedToId();
            Employee assignedTo = employeeRepository.findById(empId)
                    .orElseGet(() -> employeeRepository.findByUserId(empId)
                            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", empId)));
            if (assignedTo.getStatus() == null || !assignedTo.getStatus().equalsIgnoreCase("ACTIVE")) {
                throw new BadRequestException("Assignee is not ACTIVE");
            }
            boolean allocated = projectAllocationRepository.findByProjectIdAndEmployeeIdAndStatus(defect.getProject().getId(), assignedTo.getId(), "ACTIVE").isPresent();
            if (!allocated) {
                throw new BadRequestException("Assignee is not allocated to project " + defect.getProject().getId());
            }
            defect.setAssignedTo(assignedTo);
        }

        return defectRepository.save(defect);
    }

    @Override
    @Transactional(readOnly = true)
    public Defect getDefectById(Long id) {
        return defectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Defect", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Defect> getDefectsByProject(Long projectId) {
        return defectRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<Defect> filterDefects(Long projectId, Long releaseId, Long severityId, Long priorityId, Long statusId, Long typeId, Long moduleId, Long subModuleId, Long assignedToId, int page, int size) {
        Pageable pageable = PageableUtils.of(page, size, Sort.by("id").descending());
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

        StatusType newStatus;
        if (request.getDefectStatusId() != null) {
            newStatus = statusTypeRepository.findById(request.getDefectStatusId())
                    .orElseThrow(() -> new ResourceNotFoundException("StatusType", "id", request.getDefectStatusId()));
        } else if (request.getEffectiveStatus() != null) {
            newStatus = statusTypeRepository.findByNameIgnoreCase(request.getEffectiveStatus())
                    .orElseThrow(() -> new ResourceNotFoundException("StatusType", "name", request.getEffectiveStatus()));
        } else {
            throw new BadRequestException("Defect status is required");
        }

        defect.setDefectStatus(newStatus);
        if ("Reopened".equalsIgnoreCase(newStatus.getName()) || "reopen".equalsIgnoreCase(newStatus.getName())) {
            defect.setReOpenCount(defect.getReOpenCount() + 1);
        }

        Defect saved = defectRepository.save(defect);

        DefectHistory history = DefectHistory.builder()
                .defect(saved)
                .fromStatus(fromStatus)
                .toStatus(newStatus.getName())
                .changedBy(changedByUser != null ? changedByUser : "User")
                .comment(request.getComment())
                .build();
        defectHistoryRepository.save(history);

        DefectStatusLog logEntry = DefectStatusLog.builder()
                .project(defect.getProject())
                .release(defect.getRelease())
                .defect(saved)
                .status(newStatus.getName())
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
        if (dev.getStatus() == null || !dev.getStatus().equalsIgnoreCase("ACTIVE")) {
            throw new BadRequestException("Assignee is not ACTIVE");
        }
        boolean allocated = projectAllocationRepository.findByProjectIdAndEmployeeIdAndStatus(defect.getProject().getId(), dev.getId(), "ACTIVE").isPresent();
        if (!allocated) {
            throw new BadRequestException("Assignee is not allocated to project " + defect.getProject().getId());
        }

        defect.setAssignedTo(dev);
        return defectRepository.save(defect);
    }

    @Override
    @Transactional
    public void bulkReassign(DefectBulkReassignRequest request) {
        Employee dev = employeeRepository.findById(request.getAssignedToId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getAssignedToId()));

        for (Long defectId : request.getDefectIds()) {
            Defect d = getDefectById(defectId);
            d.setAssignedTo(dev);
            defectRepository.save(d);
        }
    }

    @Override
    @Transactional
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
    @Transactional(readOnly = true)
    public List<DefectComment> getCommentsByDefect(Long defectId) {
        return defectCommentRepository.findByDefectIdOrderByCreatedAtAsc(defectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DefectHistory> getDefectHistory(Long defectId) {
        return defectHistoryRepository.findByDefectIdOrderByChangedAtDesc(defectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DefectStatusLog> getDefectStatusLogs(Long projectId, Long releaseId) {
        return defectStatusLogRepository.findByProjectIdAndReleaseId(projectId, releaseId);
    }
}
