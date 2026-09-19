package com.defecttracker.modules.testcase.controller;

import com.defecttracker.common.dto.ApiResponse;
import com.defecttracker.common.dto.PageResponse;
import com.defecttracker.modules.testcase.dto.TestCaseDto;
import com.defecttracker.modules.testcase.service.TestCaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Test Case Management", description = "Endpoints for test cases scoped to SubModules, search, pagination, bulk import, and export")
public class TestCaseController {

    private final TestCaseService testCaseService;

    @GetMapping("/projects/{projectId}/test-cases")
    @PreAuthorize("hasAuthority('TEST_CASE:READ') or hasRole('ADMIN')")
    @Operation(summary = "Get paginated test cases for a project with optional search filter")
    public ResponseEntity<ApiResponse<PageResponse<TestCaseDto.TestCaseResponse>>> getTestCasesByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PageResponse<TestCaseDto.TestCaseResponse> response = testCaseService.getTestCasesByProject(projectId, query, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/submodules/{subModuleId}/test-cases")
    @PreAuthorize("hasAuthority('TEST_CASE:READ') or hasRole('ADMIN')")
    @Operation(summary = "Get test cases scoped to a specific submodule")
    public ResponseEntity<ApiResponse<List<TestCaseDto.TestCaseResponse>>> getTestCasesBySubModule(
            @PathVariable Long subModuleId) {
        return ResponseEntity.ok(ApiResponse.success(testCaseService.getTestCasesBySubModule(subModuleId)));
    }

    @GetMapping("/test-cases/{id}")
    @PreAuthorize("hasAuthority('TEST_CASE:READ') or hasRole('ADMIN')")
    @Operation(summary = "Get test case by ID")
    public ResponseEntity<ApiResponse<TestCaseDto.TestCaseResponse>> getTestCaseById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(testCaseService.getTestCaseById(id)));
    }

    @PostMapping("/test-cases")
    @PreAuthorize("hasAuthority('TEST_CASE:CREATE') or hasRole('ADMIN')")
    @Operation(summary = "Create a test case under a submodule")
    public ResponseEntity<ApiResponse<TestCaseDto.TestCaseResponse>> createTestCase(
            @Valid @RequestBody TestCaseDto.TestCaseCreateRequest request) {
        TestCaseDto.TestCaseResponse response = testCaseService.createTestCase(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Test case created successfully", response));
    }

    @PutMapping("/test-cases/{id}")
    @PreAuthorize("hasAuthority('TEST_CASE:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Update test case details")
    public ResponseEntity<ApiResponse<TestCaseDto.TestCaseResponse>> updateTestCase(
            @PathVariable Long id,
            @Valid @RequestBody TestCaseDto.TestCaseUpdateRequest request) {
        TestCaseDto.TestCaseResponse response = testCaseService.updateTestCase(id, request);
        return ResponseEntity.ok(ApiResponse.success("Test case updated successfully", response));
    }

    @DeleteMapping("/test-cases/{id}")
    @PreAuthorize("hasAuthority('TEST_CASE:DELETE') or hasRole('ADMIN')")
    @Operation(summary = "Deactivate test case")
    public ResponseEntity<ApiResponse<Void>> deleteTestCase(@PathVariable Long id) {
        testCaseService.deleteTestCase(id);
        return ResponseEntity.ok(ApiResponse.success("Test case deactivated successfully", null));
    }

    @PostMapping("/test-cases/bulk-import")
    @PreAuthorize("hasAuthority('TEST_CASE:CREATE') or hasRole('ADMIN')")
    @Operation(summary = "Bulk import test cases with per-row validation and reporting")
    public ResponseEntity<ApiResponse<TestCaseDto.BulkImportResult>> bulkImport(
            @Valid @RequestBody List<TestCaseDto.TestCaseCreateRequest> requests) {
        TestCaseDto.BulkImportResult result = testCaseService.bulkCreateTestCases(requests);
        return ResponseEntity.ok(ApiResponse.success("Bulk import processed", result));
    }

    @GetMapping("/projects/{projectId}/test-cases/export")
    @PreAuthorize("hasAuthority('TEST_CASE:READ') or hasRole('ADMIN')")
    @Operation(summary = "Export all project test cases as CSV")
    public ResponseEntity<byte[]> exportTestCases(@PathVariable Long projectId) {
        byte[] csvData = testCaseService.exportTestCasesAsCsv(projectId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=test_cases_project_" + projectId + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}
