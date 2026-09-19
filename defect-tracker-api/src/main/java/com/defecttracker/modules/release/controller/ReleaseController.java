package com.defecttracker.modules.release.controller;

import com.defecttracker.common.dto.ApiResponse;
import com.defecttracker.common.dto.PageResponse;
import com.defecttracker.modules.release.dto.ReleaseDto;
import com.defecttracker.modules.release.service.ReleaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Release Management", description = "Endpoints for project releases, test case execution runs, QA allocation, and release reports")
public class ReleaseController {

    private final ReleaseService releaseService;

    @GetMapping("/projects/{projectId}/releases")
    @PreAuthorize("hasAuthority('RELEASE:READ') or hasRole('ADMIN')")
    @Operation(summary = "Get releases for a project")
    public ResponseEntity<ApiResponse<List<ReleaseDto.ReleaseResponse>>> getReleasesByProject(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.success(releaseService.getReleasesByProject(projectId)));
    }

    @GetMapping("/releases/{id}")
    @PreAuthorize("hasAuthority('RELEASE:READ') or hasRole('ADMIN')")
    @Operation(summary = "Get release details by ID")
    public ResponseEntity<ApiResponse<ReleaseDto.ReleaseResponse>> getReleaseById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(releaseService.getReleaseById(id)));
    }

    @PostMapping("/releases")
    @PreAuthorize("hasAuthority('RELEASE:CREATE') or hasRole('ADMIN')")
    @Operation(summary = "Create a release under a project with KLOC tracking")
    public ResponseEntity<ApiResponse<ReleaseDto.ReleaseResponse>> createRelease(
            @Valid @RequestBody ReleaseDto.ReleaseCreateRequest request) {
        ReleaseDto.ReleaseResponse response = releaseService.createRelease(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Release created successfully", response));
    }

    @PutMapping("/releases/{id}")
    @PreAuthorize("hasAuthority('RELEASE:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Update release details, status, or KLOC")
    public ResponseEntity<ApiResponse<ReleaseDto.ReleaseResponse>> updateRelease(
            @PathVariable Long id,
            @Valid @RequestBody ReleaseDto.ReleaseUpdateRequest request) {
        ReleaseDto.ReleaseResponse response = releaseService.updateRelease(id, request);
        return ResponseEntity.ok(ApiResponse.success("Release updated successfully", response));
    }

    @DeleteMapping("/releases/{id}")
    @PreAuthorize("hasAuthority('RELEASE:DELETE') or hasRole('ADMIN')")
    @Operation(summary = "Delete release")
    public ResponseEntity<ApiResponse<Void>> deleteRelease(@PathVariable Long id) {
        releaseService.deleteRelease(id);
        return ResponseEntity.ok(ApiResponse.success("Release deleted successfully", null));
    }

    @PostMapping("/releases/{id}/test-cases")
    @PreAuthorize("hasAuthority('RELEASE:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Link a set of test cases to a release for execution run")
    public ResponseEntity<ApiResponse<List<ReleaseDto.ReleaseTestCaseResponse>>> linkTestCases(
            @PathVariable Long id,
            @Valid @RequestBody ReleaseDto.LinkTestCasesRequest request) {
        List<ReleaseDto.ReleaseTestCaseResponse> list = releaseService.linkTestCases(id, request);
        return ResponseEntity.ok(ApiResponse.success("Test cases linked to release", list));
    }

    @GetMapping("/releases/{id}/test-cases")
    @PreAuthorize("hasAuthority('RELEASE:READ') or hasRole('ADMIN')")
    @Operation(summary = "Get paginated test cases in release execution run")
    public ResponseEntity<ApiResponse<PageResponse<ReleaseDto.ReleaseTestCaseResponse>>> getReleaseTestCases(
            @PathVariable Long id,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(releaseService.getReleaseTestCases(id, pageable)));
    }

    @PutMapping("/releases/test-cases/{id}/assign-qa")
    @PreAuthorize("hasAuthority('RELEASE:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "QA-Allocation: assign employee responsible for executing test case")
    public ResponseEntity<ApiResponse<ReleaseDto.ReleaseTestCaseResponse>> assignQA(
            @PathVariable Long id,
            @Valid @RequestBody ReleaseDto.QAAllocationRequest request) {
        ReleaseDto.ReleaseTestCaseResponse response = releaseService.assignQAToTestCase(id, request.getAssignedToId());
        return ResponseEntity.ok(ApiResponse.success("QA allocated successfully", response));
    }

    @PutMapping("/releases/test-cases/{id}/execute")
    @PreAuthorize("hasAuthority('RELEASE:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Record execution result (PASSED, FAILED, BLOCKED, SKIPPED) with executing user attribution")
    public ResponseEntity<ApiResponse<ReleaseDto.ReleaseTestCaseResponse>> recordExecution(
            @PathVariable Long id,
            @Valid @RequestBody ReleaseDto.RecordExecutionRequest request) {
        ReleaseDto.ReleaseTestCaseResponse response = releaseService.recordExecution(id, request);
        return ResponseEntity.ok(ApiResponse.success("Execution result recorded", response));
    }

    @GetMapping("/projects/{projectId}/releases/report")
    @PreAuthorize("hasAuthority('RELEASE:READ') or hasRole('ADMIN')")
    @Operation(summary = "Get report of active/total releases and test case execution pass rates")
    public ResponseEntity<ApiResponse<ReleaseDto.ReleaseSummaryReport>> getReleaseReport(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.success(releaseService.getReleaseReport(projectId)));
    }
}
