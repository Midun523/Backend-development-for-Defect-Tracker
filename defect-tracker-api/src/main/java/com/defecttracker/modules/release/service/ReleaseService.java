package com.defecttracker.modules.release.service;

import com.defecttracker.common.dto.PageResponse;
import com.defecttracker.common.exception.BusinessRuleException;
import com.defecttracker.common.exception.ResourceNotFoundException;
import com.defecttracker.modules.access.entity.Employee;
import com.defecttracker.modules.access.repository.EmployeeRepository;
import com.defecttracker.modules.masterdata.entity.ReleaseType;
import com.defecttracker.modules.masterdata.repository.ReleaseTypeRepository;
import com.defecttracker.modules.project.entity.Project;
import com.defecttracker.modules.project.repository.ProjectRepository;
import com.defecttracker.modules.release.dto.ReleaseDto;
import com.defecttracker.modules.release.entity.Release;
import com.defecttracker.modules.release.entity.ReleaseTestCase;
import com.defecttracker.modules.release.repository.ReleaseRepository;
import com.defecttracker.modules.release.repository.ReleaseTestCaseRepository;
import com.defecttracker.modules.testcase.entity.TestCase;
import com.defecttracker.modules.testcase.repository.TestCaseRepository;
import com.defecttracker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReleaseService {

    private final ReleaseRepository releaseRepository;
    private final ReleaseTestCaseRepository releaseTestCaseRepository;
    private final ProjectRepository projectRepository;
    private final ReleaseTypeRepository releaseTypeRepository;
    private final TestCaseRepository testCaseRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<ReleaseDto.ReleaseResponse> getReleasesByProject(Long projectId) {
        return releaseRepository.findByProjectIdOrderByReleaseDateDesc(projectId).stream()
                .map(this::mapRelease)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReleaseDto.ReleaseResponse getReleaseById(Long id) {
        Release release = releaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Release", "id", id));
        return mapRelease(release);
    }

    @Transactional
    public ReleaseDto.ReleaseResponse createRelease(ReleaseDto.ReleaseCreateRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        if (releaseRepository.existsByProjectIdAndVersion(project.getId(), request.getVersion())) {
            throw new BusinessRuleException(String.format("Release version '%s' already exists for project '%s'",
                    request.getVersion(), project.getName()));
        }

        ReleaseType releaseType = null;
        if (request.getReleaseTypeId() != null) {
            releaseType = releaseTypeRepository.findById(request.getReleaseTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("ReleaseType", "id", request.getReleaseTypeId()));
        }

        Release release = Release.builder()
                .name(request.getName())
                .version(request.getVersion())
                .releaseDate(request.getReleaseDate())
                .releaseType(releaseType)
                .project(project)
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .kloc(request.getKloc() != null ? request.getKloc() : BigDecimal.ZERO)
                .description(request.getDescription())
                .build();

        return mapRelease(releaseRepository.save(release));
    }

    @Transactional
    public ReleaseDto.ReleaseResponse updateRelease(Long id, ReleaseDto.ReleaseUpdateRequest request) {
        Release release = releaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Release", "id", id));

        ReleaseType releaseType = null;
        if (request.getReleaseTypeId() != null) {
            releaseType = releaseTypeRepository.findById(request.getReleaseTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("ReleaseType", "id", request.getReleaseTypeId()));
        }

        release.setName(request.getName());
        release.setReleaseDate(request.getReleaseDate());
        release.setReleaseType(releaseType);
        if (request.getStatus() != null) release.setStatus(request.getStatus());
        if (request.getKloc() != null) release.setKloc(request.getKloc());
        release.setDescription(request.getDescription());

        return mapRelease(releaseRepository.save(release));
    }

    @Transactional
    public void deleteRelease(Long id) {
        releaseRepository.deleteById(id);
    }

    /**
     * Link test cases to a release for test execution run.
     */
    @Transactional
    public List<ReleaseDto.ReleaseTestCaseResponse> linkTestCases(Long releaseId, ReleaseDto.LinkTestCasesRequest request) {
        Release release = releaseRepository.findById(releaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Release", "id", releaseId));

        Employee defaultAssigned = null;
        if (request.getDefaultAssignedToId() != null) {
            defaultAssigned = employeeRepository.findById(request.getDefaultAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getDefaultAssignedToId()));
        }

        for (Long tcId : request.getTestCaseIds()) {
            if (!releaseTestCaseRepository.existsByReleaseIdAndTestCaseId(release.getId(), tcId)) {
                TestCase testCase = testCaseRepository.findById(tcId)
                        .orElseThrow(() -> new ResourceNotFoundException("TestCase", "id", tcId));

                ReleaseTestCase rtc = ReleaseTestCase.builder()
                        .release(release)
                        .testCase(testCase)
                        .assignedTo(defaultAssigned)
                        .executionStatus("PENDING")
                        .build();
                releaseTestCaseRepository.save(rtc);
            }
        }

        return releaseTestCaseRepository.findByReleaseId(releaseId).stream()
                .map(this::mapReleaseTestCase)
                .collect(Collectors.toList());
    }

    /**
     * QA Allocation: assign responsible employee to execute test case in release.
     */
    @Transactional
    public ReleaseDto.ReleaseTestCaseResponse assignQAToTestCase(Long releaseTestCaseId, Long assignedToId) {
        ReleaseTestCase rtc = releaseTestCaseRepository.findById(releaseTestCaseId)
                .orElseThrow(() -> new ResourceNotFoundException("ReleaseTestCase", "id", releaseTestCaseId));

        Employee qa = employeeRepository.findById(assignedToId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", assignedToId));

        rtc.setAssignedTo(qa);
        return mapReleaseTestCase(releaseTestCaseRepository.save(rtc));
    }

    /**
     * Record execution results (pass/fail/blocked/skipped) with executing employee attribution.
     */
    @Transactional
    public ReleaseDto.ReleaseTestCaseResponse recordExecution(Long releaseTestCaseId, ReleaseDto.RecordExecutionRequest request) {
        ReleaseTestCase rtc = releaseTestCaseRepository.findById(releaseTestCaseId)
                .orElseThrow(() -> new ResourceNotFoundException("ReleaseTestCase", "id", releaseTestCaseId));

        // Get currently logged-in user as executing employee
        Employee executingEmployee = getCurrentEmployee();

        rtc.setExecutionStatus(request.getExecutionStatus().toUpperCase());
        rtc.setExecutedBy(executingEmployee);
        rtc.setExecutedAt(LocalDateTime.now());
        if (request.getExecutionNotes() != null) {
            rtc.setExecutionNotes(request.getExecutionNotes());
        }

        return mapReleaseTestCase(releaseTestCaseRepository.save(rtc));
    }

    @Transactional(readOnly = true)
    public PageResponse<ReleaseDto.ReleaseTestCaseResponse> getReleaseTestCases(Long releaseId, Pageable pageable) {
        Page<ReleaseTestCase> page = releaseTestCaseRepository.findByReleaseId(releaseId, pageable);
        return PageResponse.of(page.map(this::mapReleaseTestCase));
    }

    /**
     * Track and report count of active/total releases and test cases per release.
     */
    @Transactional(readOnly = true)
    public ReleaseDto.ReleaseSummaryReport getReleaseReport(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        long totalReleases = releaseRepository.countByProjectId(projectId);
        long activeReleases = releaseRepository.countByProjectIdAndStatus(projectId, "ACTIVE");
        long closedReleases = releaseRepository.countByProjectIdAndStatus(projectId, "CLOSED");

        List<Release> releases = releaseRepository.findByProjectIdOrderByReleaseDateDesc(projectId);
        long totalExecuted = 0;
        long totalPassed = 0;
        long totalFailed = 0;

        for (Release r : releases) {
            long passed = releaseTestCaseRepository.countByReleaseIdAndExecutionStatus(r.getId(), "PASSED");
            long failed = releaseTestCaseRepository.countByReleaseIdAndExecutionStatus(r.getId(), "FAILED");
            long blocked = releaseTestCaseRepository.countByReleaseIdAndExecutionStatus(r.getId(), "BLOCKED");

            totalPassed += passed;
            totalFailed += failed;
            totalExecuted += (passed + failed + blocked);
        }

        double passRate = totalExecuted > 0 ? ((double) totalPassed / totalExecuted) * 100.0 : 0.0;

        return ReleaseDto.ReleaseSummaryReport.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .totalReleases(totalReleases)
                .activeReleases(activeReleases)
                .closedReleases(closedReleases)
                .totalTestCasesExecuted(totalExecuted)
                .totalTestCasesPassed(totalPassed)
                .totalTestCasesFailed(totalFailed)
                .passRatePercentage(Math.round(passRate * 100.0) / 100.0)
                .build();
    }

    private Employee getCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return employeeRepository.findById(principal.getId()).orElse(null);
        }
        return null;
    }

    private ReleaseDto.ReleaseResponse mapRelease(Release r) {
        long total = releaseTestCaseRepository.countByReleaseId(r.getId());
        long passed = releaseTestCaseRepository.countByReleaseIdAndExecutionStatus(r.getId(), "PASSED");
        long failed = releaseTestCaseRepository.countByReleaseIdAndExecutionStatus(r.getId(), "FAILED");
        long pending = releaseTestCaseRepository.countByReleaseIdAndExecutionStatus(r.getId(), "PENDING");

        return ReleaseDto.ReleaseResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .version(r.getVersion())
                .releaseDate(r.getReleaseDate())
                .releaseTypeId(r.getReleaseType() != null ? r.getReleaseType().getId() : null)
                .releaseTypeName(r.getReleaseType() != null ? r.getReleaseType().getName() : null)
                .projectId(r.getProject().getId())
                .projectName(r.getProject().getName())
                .status(r.getStatus())
                .kloc(r.getKloc())
                .description(r.getDescription())
                .totalTestCases(total)
                .passedTestCases(passed)
                .failedTestCases(failed)
                .pendingTestCases(pending)
                .createdAt(r.getCreatedAt())
                .build();
    }

    private ReleaseDto.ReleaseTestCaseResponse mapReleaseTestCase(ReleaseTestCase rtc) {
        return ReleaseDto.ReleaseTestCaseResponse.builder()
                .id(rtc.getId())
                .releaseId(rtc.getRelease().getId())
                .releaseVersion(rtc.getRelease().getVersion())
                .testCaseId(rtc.getTestCase().getId())
                .testCaseCode(rtc.getTestCase().getTestCaseCode())
                .testCaseTitle(rtc.getTestCase().getTitle())
                .assignedToId(rtc.getAssignedTo() != null ? rtc.getAssignedTo().getId() : null)
                .assignedToName(rtc.getAssignedTo() != null ? rtc.getAssignedTo().getFullName() : null)
                .executionStatus(rtc.getExecutionStatus())
                .executedById(rtc.getExecutedBy() != null ? rtc.getExecutedBy().getId() : null)
                .executedByName(rtc.getExecutedBy() != null ? rtc.getExecutedBy().getFullName() : null)
                .executedAt(rtc.getExecutedAt())
                .executionNotes(rtc.getExecutionNotes())
                .build();
    }
}
