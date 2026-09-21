package com.defecttracker.service.impl;

import com.defecttracker.dto.request.ReleaseCreateRequest;
import com.defecttracker.entity.*;
import com.defecttracker.exception.BadRequestException;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.repository.*;
import com.defecttracker.service.ProjectSequenceService;
import com.defecttracker.service.ReleaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class ReleaseServiceImpl implements ReleaseService {

    private final ReleaseRepository releaseRepository;
    private final ProjectRepository projectRepository;
    private final ReleaseTypeRepository releaseTypeRepository;
    private final TestCaseRepository testCaseRepository;
    private final ReleaseTestCaseRepository releaseTestCaseRepository;
    private final EmployeeRepository employeeRepository;
    private final TestCaseAllocationLogRepository testCaseAllocationLogRepository;
    private final DefectRepository defectRepository;
    private final PriorityRepository priorityRepository;
    private final StatusTypeRepository statusTypeRepository;
    private final ProjectSequenceService projectSequenceService;

    @Override
    @Transactional
    public Release createRelease(ReleaseCreateRequest request) {
        Long projectId = request.getProjectId();
        if (projectId == null) {
            throw new BadRequestException("Project ID is required to create a release");
        }
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        ReleaseType releaseType = null;
        if (request.getReleaseTypeId() != null) {
            releaseType = releaseTypeRepository.findById(request.getReleaseTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("ReleaseType", "id", request.getReleaseTypeId()));
        }

        String releaseNo = projectSequenceService.getNextReleaseNumber(project.getId());

        Release release = Release.builder()
                .releaseNo(releaseNo)
                .name(request.getName())
                .version(request.getVersion() != null ? request.getVersion() : "v1.0.0")
                .description(request.getDescription())
                .project(project)
                .releaseType(releaseType)
                .status(request.getStatus() != null ? request.getStatus() : "PLANNED")
                .startDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now())
                .releaseDate(request.getReleaseDate())
                .endDate(request.getEndDate())
                .kloc(request.getKloc() != null ? request.getKloc() : 0.0)
                .build();

        Release saved = releaseRepository.save(release);

        if (request.getTestCases() != null) {
            for (Long tcId : request.getTestCases()) {
                if (tcId != null) {
                    TestCase tc = testCaseRepository.findById(tcId)
                            .orElseThrow(() -> new ResourceNotFoundException("TestCase", "id", tcId));
                    ReleaseTestCase rtc = ReleaseTestCase.builder()
                            .release(saved)
                            .testCase(tc)
                            .executionStatus("NOT_RUN")
                            .build();
                    releaseTestCaseRepository.save(rtc);
                }
            }
        }

        return saved;
    }

    @Override
    @Transactional
    public Release updateRelease(Long id, ReleaseCreateRequest request) {
        Release release = getReleaseById(id);
        release.setName(request.getName());
        if (request.getVersion() != null) release.setVersion(request.getVersion());
        if (request.getDescription() != null) release.setDescription(request.getDescription());
        if (request.getStatus() != null) release.setStatus(request.getStatus());
        if (request.getStartDate() != null) release.setStartDate(request.getStartDate());
        if (request.getReleaseDate() != null) release.setReleaseDate(request.getReleaseDate());
        if (request.getEndDate() != null) release.setEndDate(request.getEndDate());
        if (request.getKloc() != null) release.setKloc(request.getKloc());

        if (request.getReleaseTypeId() != null) {
            ReleaseType rt = releaseTypeRepository.findById(request.getReleaseTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("ReleaseType", "id", request.getReleaseTypeId()));
            release.setReleaseType(rt);
        }

        return releaseRepository.save(release);
    }

    @Override
    @Transactional(readOnly = true)
    public Release getReleaseById(Long id) {
        if (id == null) {
            throw new BadRequestException("Release ID must not be null");
        }
        return releaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Release", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Release> getAllReleases() {
        return releaseRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Release> getReleasesByProject(Long projectId) {
        if (projectId == null) {
            return releaseRepository.findAll();
        }
        return releaseRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public Release getActiveReleaseByProject(Long projectId) {
        if (projectId == null) {
            return null;
        }
        List<Release> inProgress = releaseRepository.findByProjectIdAndStatusOrderByCreatedAtDesc(projectId, "IN_PROGRESS");
        if (!inProgress.isEmpty()) return inProgress.get(0);
        List<Release> testing = releaseRepository.findByProjectIdAndStatusOrderByCreatedAtDesc(projectId, "TESTING");
        if (!testing.isEmpty()) return testing.get(0);
        List<Release> planned = releaseRepository.findByProjectIdAndStatusOrderByCreatedAtDesc(projectId, "PLANNED");
        if (!planned.isEmpty()) return planned.get(0);
        return null;
    }

    @Override
    @Transactional
    public Release updateReleaseStatus(Long releaseId, String status) {
        Release release = getReleaseById(releaseId);
        release.setStatus(status.toUpperCase());
        return releaseRepository.save(release);
    }

    @Override
    @Transactional
    public Release updateReleaseKloc(Long releaseId, Double kloc) {
        Release release = getReleaseById(releaseId);
        release.setKloc(kloc != null ? kloc : 0.0);
        return releaseRepository.save(release);
    }

    @Override
    @Transactional
    public void deleteRelease(Long id) {
        Release release = getReleaseById(id);
        releaseRepository.delete(release);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getReleaseCounts() {
        Map<String, Object> counts = new HashMap<>();
        counts.put("total", releaseRepository.count());
        counts.put("planned", releaseRepository.findByProjectIdAndStatus(1L, "PLANNED").size());
        counts.put("inProgress", releaseRepository.findByProjectIdAndStatus(1L, "IN_PROGRESS").size());
        counts.put("completed", releaseRepository.findByProjectIdAndStatus(1L, "COMPLETED").size());
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReleaseTestCase> getReleaseTestCases(Long releaseId) {
        return releaseTestCaseRepository.findByReleaseId(releaseId);
    }

    @Override
    @Transactional(readOnly = true)
    public ReleaseTestCase getReleaseTestCase(Long releaseId, Long testCaseId) {
        return releaseTestCaseRepository.findByReleaseIdAndTestCaseId(releaseId, testCaseId)
                .orElseThrow(() -> new ResourceNotFoundException("ReleaseTestCase not found for release " + releaseId + " and testcase " + testCaseId));
    }

    @Override
    @Transactional
    public ReleaseTestCase assignQaToReleaseTestCase(Long releaseId, Long testCaseId, Long employeeId) {
        ReleaseTestCase rtc = releaseTestCaseRepository.findByReleaseIdAndTestCaseId(releaseId, testCaseId)
                .orElseGet(() -> {
                    Release release = getReleaseById(releaseId);
                    TestCase testCase = testCaseRepository.findById(testCaseId)
                            .orElseThrow(() -> new ResourceNotFoundException("TestCase", "id", testCaseId));
                    return ReleaseTestCase.builder().release(release).testCase(testCase).build();
                });

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        rtc.setAssignedQa(employee);
        ReleaseTestCase saved = releaseTestCaseRepository.save(rtc);

        TestCaseAllocationLog log = TestCaseAllocationLog.builder()
                .release(rtc.getRelease())
                .testCase(rtc.getTestCase())
                .employee(employee)
                .action("ASSIGNED")
                .build();
        testCaseAllocationLogRepository.save(log);

        return saved;
    }

    @Override
    @Transactional
    public void updateReleaseTestCaseEmployee(Long releaseId, Long employeeId, Map<String, Object> body) {
        if (body != null && body.containsKey("testCaseId") && body.get("testCaseId") != null) {
            Long testCaseId = Long.valueOf(String.valueOf(body.get("testCaseId")));
            assignQaToReleaseTestCase(releaseId, testCaseId, employeeId);
            return;
        }
        if (body != null && body.containsKey("testCaseIds") && body.get("testCaseIds") instanceof List) {
            List<?> ids = (List<?>) body.get("testCaseIds");
            for (Object idObj : ids) {
                Long testCaseId = Long.valueOf(String.valueOf(idObj));
                assignQaToReleaseTestCase(releaseId, testCaseId, employeeId);
            }
            return;
        }
        // If neither, allocate employee to all existing test cases in the release
        List<ReleaseTestCase> existing = releaseTestCaseRepository.findByReleaseId(releaseId);
        for (ReleaseTestCase rtc : existing) {
            if (rtc.getTestCase() != null) {
                assignQaToReleaseTestCase(releaseId, rtc.getTestCase().getId(), employeeId);
            }
        }
    }

    @Override
    @Transactional
    public ReleaseTestCase updateReleaseTestCaseStatus(Long releaseId, Long testCaseId, String status, String comment) {
        return updateReleaseTestCaseStatus(releaseId, testCaseId, status, comment, null, null);
    }

    @Override
    @Transactional
    public ReleaseTestCase updateReleaseTestCaseStatus(Long releaseId, Long testCaseId, String status, String comment, Long priorityId, Long assignedToId) {
        ReleaseTestCase rtc = releaseTestCaseRepository.findByReleaseIdAndTestCaseId(releaseId, testCaseId)
                .orElseThrow(() -> new ResourceNotFoundException("ReleaseTestCase not found"));

        rtc.setExecutionStatus(status.toUpperCase());
        rtc.setExecutionComment(comment);
        rtc.setExecutedAt(LocalDateTime.now());

        // Auto-create defect when test case is FAILED
        if ("FAIL".equalsIgnoreCase(status) || "FAILED".equalsIgnoreCase(status)) {
            TestCase tc = rtc.getTestCase();
            Release release = rtc.getRelease();

            if (tc != null && release != null && release.getProject() != null) {
                Priority priority = null;
                if (priorityId != null) {
                    priority = priorityRepository.findById(priorityId)
                            .orElseThrow(() -> new ResourceNotFoundException("Priority", "id", priorityId));
                }
                if (priority == null) {
                    priority = priorityRepository.findTopByOrderByIdAsc()
                            .orElseThrow(() -> new ResourceNotFoundException("Priority", "default", "none found"));
                }

                Employee assignedTo = null;
                if (assignedToId != null) {
                    assignedTo = employeeRepository.findById(assignedToId)
                            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", assignedToId));
                }

                StatusType newStatus = statusTypeRepository.findFirstByIsDefaultTrue()
                        .or(statusTypeRepository::findTopByOrderByIdAsc)
                        .orElseThrow(() -> new ResourceNotFoundException("StatusType", "default", "none found"));

                String defectId = projectSequenceService.getNextDefectNumber(release.getProject().getId());

                Defect defect = Defect.builder()
                        .defectId(defectId)
                        .title(tc.getDescription())
                        .description(tc.getDescription())
                        .steps(tc.getDetailsSteps())
                        .priority(priority)
                        .severity(tc.getSeverity())
                        .defectStatus(newStatus)
                        .defectType(tc.getDefectType())
                        .project(release.getProject())
                        .release(release)
                        .module(tc.getSubModule() != null ? tc.getSubModule().getModule() : null)
                        .subModule(tc.getSubModule())
                        .testCase(tc)
                        .assignedTo(assignedTo)
                        .reportedBy(rtc.getAssignedQa() != null
                                ? rtc.getAssignedQa().getFirstName() + " " + rtc.getAssignedQa().getLastName()
                                : "QA")
                        .build();

                Defect savedDefect = defectRepository.save(defect);
                rtc.setLinkedDefect(savedDefect);
            }
        }

        return releaseTestCaseRepository.save(rtc);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestCaseAllocationLog> getTestCaseAllocationLogs() {
        return testCaseAllocationLogRepository.findAll();
    }
}
