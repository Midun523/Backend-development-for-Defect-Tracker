package com.defecttracker.service.impl;

import com.defecttracker.dto.request.ReleaseCreateRequest;
import com.defecttracker.entity.*;
import com.defecttracker.exception.BadRequestException;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.repository.*;
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
@RequiredArgsConstructor
public class ReleaseServiceImpl implements ReleaseService {

    private final ReleaseRepository releaseRepository;
    private final ProjectRepository projectRepository;
    private final ReleaseTypeRepository releaseTypeRepository;
    private final TestCaseRepository testCaseRepository;
    private final ReleaseTestCaseRepository releaseTestCaseRepository;
    private final EmployeeRepository employeeRepository;
    private final TestCaseAllocationLogRepository testCaseAllocationLogRepository;

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
            releaseType = releaseTypeRepository.findById(request.getReleaseTypeId()).orElse(null);
        }

        long count = releaseRepository.count() + 1;
        String releaseNo = String.format("REL%03d", count);

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
                    testCaseRepository.findById(tcId).ifPresent(tc -> {
                        ReleaseTestCase rtc = ReleaseTestCase.builder()
                                .release(saved)
                                .testCase(tc)
                                .executionStatus("NOT_RUN")
                                .build();
                        releaseTestCaseRepository.save(rtc);
                    });
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
            releaseTypeRepository.findById(request.getReleaseTypeId()).ifPresent(release::setReleaseType);
        }

        return releaseRepository.save(release);
    }

    @Override
    public Release getReleaseById(Long id) {
        if (id == null) {
            throw new BadRequestException("Release ID must not be null");
        }
        return releaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Release", "id", id));
    }

    @Override
    public List<Release> getAllReleases() {
        return releaseRepository.findAll();
    }

    @Override
    public List<Release> getReleasesByProject(Long projectId) {
        if (projectId == null) {
            return releaseRepository.findAll();
        }
        return releaseRepository.findByProjectId(projectId);
    }

    @Override
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
    public Release updateReleaseStatus(Long releaseId, String status) {
        Release release = getReleaseById(releaseId);
        release.setStatus(status.toUpperCase());
        return releaseRepository.save(release);
    }

    @Override
    public Release updateReleaseKloc(Long releaseId, Double kloc) {
        Release release = getReleaseById(releaseId);
        release.setKloc(kloc != null ? kloc : 0.0);
        return releaseRepository.save(release);
    }

    @Override
    public void deleteRelease(Long id) {
        Release release = getReleaseById(id);
        releaseRepository.delete(release);
    }

    @Override
    public Map<String, Object> getReleaseCounts() {
        Map<String, Object> counts = new HashMap<>();
        counts.put("total", releaseRepository.count());
        counts.put("planned", releaseRepository.findByProjectIdAndStatus(1L, "PLANNED").size());
        counts.put("inProgress", releaseRepository.findByProjectIdAndStatus(1L, "IN_PROGRESS").size());
        counts.put("completed", releaseRepository.findByProjectIdAndStatus(1L, "COMPLETED").size());
        return counts;
    }

    @Override
    public List<ReleaseTestCase> getReleaseTestCases(Long releaseId) {
        return releaseTestCaseRepository.findByReleaseId(releaseId);
    }

    @Override
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
    public ReleaseTestCase updateReleaseTestCaseStatus(Long releaseId, Long testCaseId, String status, String comment) {
        ReleaseTestCase rtc = releaseTestCaseRepository.findByReleaseIdAndTestCaseId(releaseId, testCaseId)
                .orElseThrow(() -> new ResourceNotFoundException("ReleaseTestCase not found"));

        rtc.setExecutionStatus(status.toUpperCase());
        rtc.setExecutionComment(comment);
        rtc.setExecutedAt(LocalDateTime.now());
        return releaseTestCaseRepository.save(rtc);
    }

    @Override
    public List<TestCaseAllocationLog> getTestCaseAllocationLogs() {
        return testCaseAllocationLogRepository.findAll();
    }
}
