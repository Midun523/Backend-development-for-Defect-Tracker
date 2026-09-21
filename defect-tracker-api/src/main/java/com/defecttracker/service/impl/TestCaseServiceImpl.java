package com.defecttracker.service.impl;

import com.defecttracker.dto.request.TestCaseCreateRequest;
import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.entity.*;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.repository.*;
import com.defecttracker.service.ProjectSequenceService;
import com.defecttracker.service.TestCaseService;
import com.defecttracker.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TestCaseServiceImpl implements TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final SubModuleRepository subModuleRepository;
    private final SeverityRepository severityRepository;
    private final DefectTypeRepository defectTypeRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectSequenceService projectSequenceService;

    @Override
    @Transactional
    public TestCase createTestCase(Long subModuleId, TestCaseCreateRequest request) {
        Long targetSubModuleId = subModuleId != null ? subModuleId : request.getSubModuleId();
        SubModule subModule = subModuleRepository.findById(targetSubModuleId)
                .orElseThrow(() -> new ResourceNotFoundException("SubModule", "id", targetSubModuleId));

        Severity severity = null;
        if (request.getSeverityId() != null) {
            severity = severityRepository.findById(request.getSeverityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Severity", "id", request.getSeverityId()));
        } else {
            severity = severityRepository.findTopByOrderByWeightAscIdAsc()
                    .or(severityRepository::findTopByOrderByIdAsc)
                    .orElse(null);
        }

        DefectType defectType = null;
        if (request.getDefectTypeId() != null) {
            defectType = defectTypeRepository.findById(request.getDefectTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("DefectType", "id", request.getDefectTypeId()));
        }

        Employee qa = null;
        if (request.getAssignedQaId() != null) {
            qa = employeeRepository.findById(request.getAssignedQaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getAssignedQaId()));
        }

        Long projectId = (subModule.getModule() != null && subModule.getModule().getProject() != null)
                ? subModule.getModule().getProject().getId()
                : null;

        String testcaseNo = (request.getTestcaseNo() != null && !request.getTestcaseNo().trim().isEmpty())
                ? request.getTestcaseNo()
                : (projectId != null ? projectSequenceService.getNextTestCaseNumber(projectId) : "TC001");

        TestCase testCase = TestCase.builder()
                .testcaseNo(testcaseNo)
                .description(request.getDescription())
                .detailsSteps(request.getEffectiveSteps())
                .expectedResult(request.getExpectedResult())
                .subModule(subModule)
                .severity(severity)
                .defectType(defectType)
                .assignedQa(qa)
                .createdBy(request.getCreatedBy())
                .executionStatus("NOT_RUN")
                .build();

        return testCaseRepository.save(testCase);
    }

    @Override
    @Transactional
    public TestCase updateTestCase(Long subModuleId, Long id, TestCaseCreateRequest request) {
        TestCase testCase = getTestCaseById(id);

        if (request.getDescription() != null) testCase.setDescription(request.getDescription());
        if (request.getEffectiveSteps() != null) testCase.setDetailsSteps(request.getEffectiveSteps());
        if (request.getExpectedResult() != null) testCase.setExpectedResult(request.getExpectedResult());

        if (request.getSeverityId() != null) {
            Severity severity = severityRepository.findById(request.getSeverityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Severity", "id", request.getSeverityId()));
            testCase.setSeverity(severity);
        }
        if (request.getDefectTypeId() != null) {
            DefectType defectType = defectTypeRepository.findById(request.getDefectTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("DefectType", "id", request.getDefectTypeId()));
            testCase.setDefectType(defectType);
        }
        if (request.getAssignedQaId() != null) {
            Employee employee = employeeRepository.findById(request.getAssignedQaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getAssignedQaId()));
            testCase.setAssignedQa(employee);
        }

        return testCaseRepository.save(testCase);
    }

    @Override
    @Transactional(readOnly = true)
    public TestCase getTestCaseById(Long id) {
        return testCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestCase", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<TestCase> getTestCasesBySubModule(Long subModuleId, String description, Long defectTypeId, Long severityId, int page, int size) {
        Pageable pageable = PageableUtils.of(Math.max(0, page), Math.max(1, size), Sort.by("id").descending());
        String search = (description != null && !description.trim().isEmpty()) ? "%" + description.trim().toLowerCase() + "%" : null;

        Page<TestCase> testCasePage;
        if (search == null && defectTypeId == null && severityId == null) {
            testCasePage = testCaseRepository.findBySubModuleId(subModuleId, pageable);
        } else {
            testCasePage = testCaseRepository.filterTestCases(subModuleId, search, defectTypeId, severityId, pageable);
        }

        return PaginatedResponse.<TestCase>builder()
                .content(testCasePage.getContent())
                .pageNumber(testCasePage.getNumber())
                .pageSize(testCasePage.getSize())
                .totalElements(testCasePage.getTotalElements())
                .totalPages(testCasePage.getTotalPages())
                .first(testCasePage.isFirst())
                .last(testCasePage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<TestCase> getTestCasesByModule(Long moduleId, String description, Long defectTypeId, Long severityId, int page, int size) {
        Pageable pageable = PageableUtils.of(Math.max(0, page), Math.max(1, size), Sort.by("id").descending());
        String search = (description != null && !description.trim().isEmpty()) ? "%" + description.trim().toLowerCase() + "%" : null;

        Page<TestCase> testCasePage;
        if (search == null && defectTypeId == null && severityId == null) {
            testCasePage = testCaseRepository.findBySubModuleModuleId(moduleId, pageable);
        } else {
            testCasePage = testCaseRepository.filterTestCasesByModule(moduleId, search, defectTypeId, severityId, pageable);
        }

        return PaginatedResponse.<TestCase>builder()
                .content(testCasePage.getContent())
                .pageNumber(testCasePage.getNumber())
                .pageSize(testCasePage.getSize())
                .totalElements(testCasePage.getTotalElements())
                .totalPages(testCasePage.getTotalPages())
                .first(testCasePage.isFirst())
                .last(testCasePage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<TestCase> getTestCasesByProject(Long projectId, String description, Long defectTypeId, Long severityId, int page, int size) {
        Pageable pageable = PageableUtils.of(Math.max(0, page), Math.max(1, size), Sort.by("id").descending());
        String search = (description != null && !description.trim().isEmpty()) ? "%" + description.trim().toLowerCase() + "%" : null;

        Page<TestCase> testCasePage;
        if (search == null && defectTypeId == null && severityId == null) {
            testCasePage = testCaseRepository.findBySubModuleModuleProjectId(projectId, pageable);
        } else {
            testCasePage = testCaseRepository.filterTestCasesByProject(projectId, search, defectTypeId, severityId, pageable);
        }

        return PaginatedResponse.<TestCase>builder()
                .content(testCasePage.getContent())
                .pageNumber(testCasePage.getNumber())
                .pageSize(testCasePage.getSize())
                .totalElements(testCasePage.getTotalElements())
                .totalPages(testCasePage.getTotalPages())
                .first(testCasePage.isFirst())
                .last(testCasePage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestCase> getAllTestCases() {
        return testCaseRepository.findAll();
    }

    @Override
    @Transactional
    public List<TestCase> createBulkTestCases(List<TestCaseCreateRequest> requests) {
        List<TestCase> created = new ArrayList<>();
        for (TestCaseCreateRequest req : requests) {
            if (req.getSubModuleId() != null) {
                created.add(createTestCase(req.getSubModuleId(), req));
            }
        }
        return created;
    }

    @Override
    @Transactional
    public void deleteTestCase(Long id) {
        TestCase tc = getTestCaseById(id);
        testCaseRepository.delete(tc);
    }
}
