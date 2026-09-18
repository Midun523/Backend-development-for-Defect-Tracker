package com.defecttracker.controller;

import com.defecttracker.dto.request.TestCaseCreateRequest;
import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.entity.TestCase;
import com.defecttracker.service.TestCaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Test Cases Management", description = "Endpoints for test cases, step definitions, bulk imports and filtering")
public class TestCaseController {

    private final TestCaseService testCaseService;

    @PostMapping("/sub-module/{subModuleId}/test-case")
    @Operation(summary = "Create test case for a submodule")
    public ResponseEntity<ApiResponse<TestCase>> createTestCase(
            @PathVariable Long subModuleId,
            @Valid @RequestBody TestCaseCreateRequest request
    ) {
        TestCase testCase = testCaseService.createTestCase(subModuleId, request);
        return ResponseEntity.ok(ApiResponse.created(testCase, "Test case created successfully"));
    }

    @GetMapping("/sub-module/{subModuleId}/test-case")
    @Operation(summary = "Get test cases for a submodule with optional filters & pagination")
    public ResponseEntity<ApiResponse<PaginatedResponse<TestCase>>> getTestCasesBySubModule(
            @PathVariable Long subModuleId,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long defectTypeId,
            @RequestParam(required = false) Long severityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PaginatedResponse<TestCase> p = testCaseService.getTestCasesBySubModule(subModuleId, description, defectTypeId, severityId, page, size);
        return ResponseEntity.ok(ApiResponse.success(p, "Test cases retrieved"));
    }

    @GetMapping("/sub-module/{subModuleId}/test-case/{id}")
    @Operation(summary = "Get test case by ID")
    public ResponseEntity<ApiResponse<TestCase>> getTestCaseById(
            @PathVariable Long subModuleId,
            @PathVariable Long id
    ) {
        TestCase testCase = testCaseService.getTestCaseById(id);
        return ResponseEntity.ok(ApiResponse.success(testCase, "Test case found"));
    }

    @PutMapping("/sub-module/{subModuleId}/test-case/{id}")
    @Operation(summary = "Update test case")
    public ResponseEntity<ApiResponse<TestCase>> updateTestCase(
            @PathVariable Long subModuleId,
            @PathVariable Long id,
            @Valid @RequestBody TestCaseCreateRequest request
    ) {
        TestCase testCase = testCaseService.updateTestCase(subModuleId, id, request);
        return ResponseEntity.ok(ApiResponse.success(testCase, "Test case updated successfully"));
    }

    @DeleteMapping("/sub-module/{subModuleId}/test-case/{id}")
    @Operation(summary = "Delete test case")
    public ResponseEntity<ApiResponse<Void>> deleteTestCase(
            @PathVariable Long subModuleId,
            @PathVariable Long id
    ) {
        testCaseService.deleteTestCase(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Test case deleted successfully"));
    }

    @GetMapping("/test-case")
    @Operation(summary = "Get all test cases in the system")
    public ResponseEntity<ApiResponse<List<TestCase>>> getAllTestCases() {
        List<TestCase> list = testCaseService.getAllTestCases();
        return ResponseEntity.ok(ApiResponse.success(list, "All test cases retrieved"));
    }

    @PostMapping({"/test-case/bulk", "/sub-module/{subModuleId}/test-case/bulk"})
    @Operation(summary = "Create test cases in bulk")
    public ResponseEntity<ApiResponse<List<TestCase>>> createBulkTestCases(
            @PathVariable(required = false) Long subModuleId,
            @RequestBody List<TestCaseCreateRequest> requests
    ) {
        if (subModuleId != null) {
            requests.forEach(r -> r.setSubModuleId(subModuleId));
        }
        List<TestCase> created = testCaseService.createBulkTestCases(requests);
        return ResponseEntity.ok(ApiResponse.created(created, "Bulk test cases created successfully"));
    }

    @GetMapping("/test-case/bulk")
    @Operation(summary = "Export bulk test cases")
    public ResponseEntity<ApiResponse<List<TestCase>>> exportBulkTestCases() {
        List<TestCase> list = testCaseService.getAllTestCases();
        return ResponseEntity.ok(ApiResponse.success(list, "Test cases exported"));
    }
}
