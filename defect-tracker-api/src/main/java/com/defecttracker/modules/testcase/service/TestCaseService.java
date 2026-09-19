package com.defecttracker.modules.testcase.service;

import com.defecttracker.common.dto.PageResponse;
import com.defecttracker.common.exception.ResourceNotFoundException;
import com.defecttracker.modules.masterdata.entity.DefectType;
import com.defecttracker.modules.masterdata.entity.Severity;
import com.defecttracker.modules.masterdata.repository.DefectTypeRepository;
import com.defecttracker.modules.masterdata.repository.SeverityRepository;
import com.defecttracker.modules.project.entity.SubModule;
import com.defecttracker.modules.project.repository.SubModuleRepository;
import com.defecttracker.modules.testcase.dto.TestCaseDto;
import com.defecttracker.modules.testcase.entity.TestCase;
import com.defecttracker.modules.testcase.repository.TestCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final SubModuleRepository subModuleRepository;
    private final SeverityRepository severityRepository;
    private final DefectTypeRepository defectTypeRepository;

    @Transactional(readOnly = true)
    public PageResponse<TestCaseDto.TestCaseResponse> getTestCasesByProject(
            Long projectId, String query, Pageable pageable) {

        Page<TestCase> page;
        if (query != null && !query.trim().isEmpty()) {
            page = testCaseRepository.searchTestCasesByProject(projectId, query.trim(), pageable);
        } else {
            page = testCaseRepository.findByProjectId(projectId, pageable);
        }

        return PageResponse.of(page.map(this::mapTestCase));
    }

    @Transactional(readOnly = true)
    public List<TestCaseDto.TestCaseResponse> getTestCasesBySubModule(Long subModuleId) {
        return testCaseRepository.findBySubModuleIdAndActiveTrue(subModuleId).stream()
                .map(this::mapTestCase)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TestCaseDto.TestCaseResponse getTestCaseById(Long id) {
        TestCase tc = testCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestCase", "id", id));
        return mapTestCase(tc);
    }

    @Transactional
    public TestCaseDto.TestCaseResponse createTestCase(TestCaseDto.TestCaseCreateRequest request) {
        SubModule subModule = subModuleRepository.findById(request.getSubModuleId())
                .orElseThrow(() -> new ResourceNotFoundException("SubModule", "id", request.getSubModuleId()));

        Severity severity = null;
        if (request.getSeverityId() != null) {
            severity = severityRepository.findById(request.getSeverityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Severity", "id", request.getSeverityId()));
        }

        DefectType defectType = null;
        if (request.getDefectTypeId() != null) {
            defectType = defectTypeRepository.findById(request.getDefectTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("DefectType", "id", request.getDefectTypeId()));
        }

        String code = request.getTestCaseCode();
        if (code == null || code.trim().isEmpty()) {
            long count = testCaseRepository.count() + 1;
            code = String.format("TC-%04d", count);
        }

        TestCase testCase = TestCase.builder()
                .testCaseCode(code)
                .title(request.getTitle())
                .description(request.getDescription())
                .steps(request.getSteps())
                .expectedResult(request.getExpectedResult())
                .subModule(subModule)
                .severity(severity)
                .defectType(defectType)
                .active(true)
                .build();

        return mapTestCase(testCaseRepository.save(testCase));
    }

    @Transactional
    public TestCaseDto.TestCaseResponse updateTestCase(Long id, TestCaseDto.TestCaseUpdateRequest request) {
        TestCase tc = testCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestCase", "id", id));

        Severity severity = null;
        if (request.getSeverityId() != null) {
            severity = severityRepository.findById(request.getSeverityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Severity", "id", request.getSeverityId()));
        }

        DefectType defectType = null;
        if (request.getDefectTypeId() != null) {
            defectType = defectTypeRepository.findById(request.getDefectTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("DefectType", "id", request.getDefectTypeId()));
        }

        tc.setTitle(request.getTitle());
        tc.setDescription(request.getDescription());
        tc.setSteps(request.getSteps());
        tc.setExpectedResult(request.getExpectedResult());
        tc.setSeverity(severity);
        tc.setDefectType(defectType);
        if (request.getActive() != null) {
            tc.setActive(request.getActive());
        }

        return mapTestCase(testCaseRepository.save(tc));
    }

    @Transactional
    public void deleteTestCase(Long id) {
        TestCase tc = testCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestCase", "id", id));
        tc.setActive(false);
        testCaseRepository.save(tc);
    }

    /**
     * Bulk create / import test cases with per-row validation and error reporting.
     */
    @Transactional
    public TestCaseDto.BulkImportResult bulkCreateTestCases(List<TestCaseDto.TestCaseCreateRequest> requests) {
        int success = 0;
        int failure = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            TestCaseDto.TestCaseCreateRequest req = requests.get(i);
            try {
                createTestCase(req);
                success++;
            } catch (Exception ex) {
                failure++;
                errors.add("Row " + (i + 1) + " ('" + req.getTitle() + "'): " + ex.getMessage());
            }
        }

        return TestCaseDto.BulkImportResult.builder()
                .totalProcessed(requests.size())
                .successCount(success)
                .failureCount(failure)
                .errors(errors)
                .build();
    }

    /**
     * Bulk export project test cases as CSV byte content.
     */
    @Transactional(readOnly = true)
    public byte[] exportTestCasesAsCsv(Long projectId) {
        List<TestCase> list = testCaseRepository.findAllByProjectId(projectId);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        // Header
        writer.println("Test Case Code,Title,Description,Steps,Expected Result,SubModule,Module,Severity,Defect Type");

        for (TestCase tc : list) {
            String code = escapeCsv(tc.getTestCaseCode());
            String title = escapeCsv(tc.getTitle());
            String desc = escapeCsv(tc.getDescription());
            String steps = escapeCsv(tc.getSteps());
            String expected = escapeCsv(tc.getExpectedResult());
            String subModule = escapeCsv(tc.getSubModule().getName());
            String module = escapeCsv(tc.getSubModule().getModule().getName());
            String severity = tc.getSeverity() != null ? escapeCsv(tc.getSeverity().getName()) : "";
            String defectType = tc.getDefectType() != null ? escapeCsv(tc.getDefectType().getName()) : "";

            writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                    code, title, desc, steps, expected, subModule, module, severity, defectType);
        }

        writer.flush();
        return out.toByteArray();
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        String clean = val.replace("\"", "\"\"");
        return "\"" + clean + "\"";
    }

    private TestCaseDto.TestCaseResponse mapTestCase(TestCase tc) {
        return TestCaseDto.TestCaseResponse.builder()
                .id(tc.getId())
                .testCaseCode(tc.getTestCaseCode())
                .title(tc.getTitle())
                .description(tc.getDescription())
                .steps(tc.getSteps())
                .expectedResult(tc.getExpectedResult())
                .subModuleId(tc.getSubModule().getId())
                .subModuleName(tc.getSubModule().getName())
                .moduleId(tc.getSubModule().getModule().getId())
                .moduleName(tc.getSubModule().getModule().getName())
                .projectId(tc.getSubModule().getModule().getProject().getId())
                .projectName(tc.getSubModule().getModule().getProject().getName())
                .severityId(tc.getSeverity() != null ? tc.getSeverity().getId() : null)
                .severityName(tc.getSeverity() != null ? tc.getSeverity().getName() : null)
                .defectTypeId(tc.getDefectType() != null ? tc.getDefectType().getId() : null)
                .defectTypeName(tc.getDefectType() != null ? tc.getDefectType().getName() : null)
                .active(tc.isActive())
                .createdAt(tc.getCreatedAt())
                .build();
    }
}
