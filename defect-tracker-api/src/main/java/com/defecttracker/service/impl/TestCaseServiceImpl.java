package com.defecttracker.service.impl;

import com.defecttracker.dto.request.TestCaseCreateRequest;
import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.entity.*;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.repository.*;
import com.defecttracker.service.TestCaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestCaseServiceImpl implements TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final SubModuleRepository subModuleRepository;
    private final SeverityRepository severityRepository;
    private final DefectTypeRepository defectTypeRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public TestCase createTestCase(Long subModuleId, TestCaseCreateRequest request) {
        Long targetSubModuleId = subModuleId != null ? subModuleId : request.getSubModuleId();
        SubModule subModule = subModuleRepository.findById(targetSubModuleId)
                .orElseThrow(() -> new ResourceNotFoundException("SubModule", "id", targetSubModuleId));

        Severity severity = null;
        if (request.getSeverityId() != null) {
            severity = severityRepository.findById(request.getSeverityId()).orElse(null);
        }

        DefectType defectType = null;
        if (request.getDefectTypeId() != null) {
            defectType = defectTypeRepository.findById(request.getDefectTypeId()).orElse(null);
        }

        Employee qa = null;
        if (request.getAssignedQaId() != null) {
            qa = employeeRepository.findById(request.getAssignedQaId()).orElse(null);
        }

        long count = testCaseRepository.count() + 1;
        String testcaseNo = (request.getTestcaseNo() != null && !request.getTestcaseNo().trim().isEmpty())
                ? request.getTestcaseNo()
                : String.format("TC%03d", count);

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
            severityRepository.findById(request.getSeverityId()).ifPresent(testCase::setSeverity);
        }
        if (request.getDefectTypeId() != null) {
            defectTypeRepository.findById(request.getDefectTypeId()).ifPresent(testCase::setDefectType);
        }
        if (request.getAssignedQaId() != null) {
            employeeRepository.findById(request.getAssignedQaId()).ifPresent(testCase::setAssignedQa);
        }

        return testCaseRepository.save(testCase);
    }

    @Override
    public TestCase getTestCaseById(Long id) {
        return testCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestCase", "id", id));
    }

    @Override
    public PaginatedResponse<TestCase> getTestCasesBySubModule(Long subModuleId, String description, Long defectTypeId, Long severityId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by("id").descending());
        Page<TestCase> testCasePage = testCaseRepository.filterTestCases(subModuleId, description, defectTypeId, severityId, pageable);

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
    public void deleteTestCase(Long id) {
        TestCase tc = getTestCaseById(id);
        testCaseRepository.delete(tc);
    }
}
