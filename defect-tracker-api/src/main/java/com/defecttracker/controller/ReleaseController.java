package com.defecttracker.controller;

import com.defecttracker.dto.request.ReleaseCreateRequest;
import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.dto.response.ReleaseCountsResponse;
import com.defecttracker.dto.response.ReleaseResponse;
import com.defecttracker.dto.response.ReleaseTestCaseResponse;
import com.defecttracker.dto.response.TestCaseAllocationLogResponse;
import com.defecttracker.entity.Release;
import com.defecttracker.entity.ReleaseTestCase;
import com.defecttracker.entity.TestCaseAllocationLog;
import com.defecttracker.mapper.ReleaseMapper;
import com.defecttracker.service.ReleaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Release & QA Allocation", description = "Endpoints for project releases, release test cases, QA allocations and execution logs")
public class ReleaseController {

    private final ReleaseService releaseService;
    private final ReleaseMapper releaseMapper;

    @PostMapping("/release")
    @PreAuthorize("@access.has('RELEASE_CREATE')")
    @Operation(summary = "Create release")
    public ResponseEntity<ApiResponse<ReleaseResponse>> createRelease(@Valid @RequestBody ReleaseCreateRequest request) {
        Release release = releaseService.createRelease(request);
        return ResponseEntity.ok(ApiResponse.created(releaseMapper.toResponse(release), "Release created successfully"));
    }

    @GetMapping("/release")
    @PreAuthorize("@access.has('RELEASE_READ')")
    @Operation(summary = "Get all releases")
    public ResponseEntity<ApiResponse<List<ReleaseResponse>>> getAllReleases(@RequestParam(required = false) Long projectId) {
        if (projectId != null) {
            return ResponseEntity.ok(ApiResponse.success(releaseMapper.toResponseList(releaseService.getReleasesByProject(projectId)), "Project releases retrieved"));
        }
        return ResponseEntity.ok(ApiResponse.success(releaseMapper.toResponseList(releaseService.getAllReleases()), "Releases retrieved"));
    }

    @GetMapping("/release/counts")
    @PreAuthorize("@access.has('RELEASE_READ')")
    @Operation(summary = "Get counts of releases by state")
    public ResponseEntity<ApiResponse<ReleaseCountsResponse>> getReleaseCounts() {
        Map<String, Object> counts = releaseService.getReleaseCounts();
        ReleaseCountsResponse dto = new ReleaseCountsResponse(
                ((Number) counts.getOrDefault("total", 0)).longValue(),
                ((Number) counts.getOrDefault("inProgress", 0)).intValue(),
                ((Number) counts.getOrDefault("planned", 0)).intValue(),
                ((Number) counts.getOrDefault("completed", 0)).intValue()
        );
        return ResponseEntity.ok(ApiResponse.success(dto, "Release counts retrieved"));
    }

    @GetMapping("/release/{id}")
    @PreAuthorize("@access.hasReleaseAccess('RELEASE_READ', #id)")
    @Operation(summary = "Get release by ID")
    public ResponseEntity<ApiResponse<ReleaseResponse>> getReleaseById(@PathVariable Long id) {
        Release release = releaseService.getReleaseById(id);
        return ResponseEntity.ok(ApiResponse.success(releaseMapper.toResponse(release), "Release found"));
    }

    @PutMapping("/release/{id}")
    @PreAuthorize("@access.hasReleaseAccess('RELEASE_UPDATE', #id)")
    @Operation(summary = "Update release")
    public ResponseEntity<ApiResponse<ReleaseResponse>> updateRelease(
            @PathVariable Long id,
            @Valid @RequestBody ReleaseCreateRequest request
    ) {
        Release release = releaseService.updateRelease(id, request);
        return ResponseEntity.ok(ApiResponse.success(releaseMapper.toResponse(release), "Release updated successfully"));
    }

    @DeleteMapping("/release/{id}")
    @PreAuthorize("@access.hasReleaseAccess('RELEASE_DELETE', #id)")
    @Operation(summary = "Delete release")
    public ResponseEntity<ApiResponse<Void>> deleteRelease(@PathVariable Long id) {
        releaseService.deleteRelease(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Release deleted successfully"));
    }

    @PatchMapping("/release/{releaseId}/status")
    @PreAuthorize("@access.hasReleaseAccess('RELEASE_UPDATE', #releaseId)")
    @Operation(summary = "Update release status")
    public ResponseEntity<ApiResponse<ReleaseResponse>> updateReleaseStatus(
            @PathVariable Long releaseId,
            @Valid @RequestBody Map<String, String> body
    ) {
        String status = body.getOrDefault("status", "PLANNED");
        Release release = releaseService.updateReleaseStatus(releaseId, status);
        return ResponseEntity.ok(ApiResponse.success(releaseMapper.toResponse(release), "Release status updated"));
    }

    @GetMapping("/project/{projectId}/release/active")
    @PreAuthorize("@access.hasProjectAccess('RELEASE_READ', #projectId)")
    @Operation(summary = "Get active release for a project")
    public ResponseEntity<ApiResponse<ReleaseResponse>> getActiveRelease(@PathVariable Long projectId) {
        Release release = releaseService.getActiveReleaseByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(releaseMapper.toResponse(release), "Active release retrieved"));
    }

    @PatchMapping("/release/{releaseId}/kloc")
    @PreAuthorize("@access.hasReleaseAccess('RELEASE_UPDATE', #releaseId)")
    @Operation(summary = "Update release KLOC")
    public ResponseEntity<ApiResponse<ReleaseResponse>> updateReleaseKloc(
            @PathVariable Long releaseId,
            @Valid @RequestBody Map<String, Double> body
    ) {
        Double kloc = body.getOrDefault("kloc", 0.0);
        Release release = releaseService.updateReleaseKloc(releaseId, kloc);
        return ResponseEntity.ok(ApiResponse.success(releaseMapper.toResponse(release), "Release KLOC updated"));
    }

    @GetMapping("/release/{releaseId}/test-case")
    @PreAuthorize("@access.hasReleaseAccess('RELEASE_READ', #releaseId)")
    @Operation(summary = "Get test cases in a release")
    public ResponseEntity<ApiResponse<List<ReleaseTestCaseResponse>>> getReleaseTestCases(@PathVariable Long releaseId) {
        List<ReleaseTestCase> list = releaseService.getReleaseTestCases(releaseId);
        return ResponseEntity.ok(ApiResponse.success(releaseMapper.toReleaseTestCaseResponseList(list), "Release test cases retrieved"));
    }

    @GetMapping("/release/{releaseId}/test-case/{id}")
    @PreAuthorize("@access.hasReleaseAccess('RELEASE_READ', #releaseId)")
    @Operation(summary = "Get release test case by testcase ID")
    public ResponseEntity<ApiResponse<ReleaseTestCaseResponse>> getReleaseTestCase(
            @PathVariable Long releaseId,
            @PathVariable Long id
    ) {
        ReleaseTestCase rtc = releaseService.getReleaseTestCase(releaseId, id);
        return ResponseEntity.ok(ApiResponse.success(releaseMapper.toReleaseTestCaseResponse(rtc), "Release test case found"));
    }

    @PostMapping("/release/{releaseId}/test-case/{testcaseId}/employee")
    @PreAuthorize("@access.hasReleaseAccess('RELEASE_UPDATE', #releaseId)")
    @Operation(summary = "Assign QA employee to release test case")
    public ResponseEntity<ApiResponse<ReleaseTestCaseResponse>> assignQaToTestCase(
            @PathVariable Long releaseId,
            @PathVariable Long testcaseId,
            @Valid @RequestBody Map<String, Long> body
    ) {
        Long employeeId = body.get("employeeId");
        ReleaseTestCase rtc = releaseService.assignQaToReleaseTestCase(releaseId, testcaseId, employeeId);
        return ResponseEntity.ok(ApiResponse.success(releaseMapper.toReleaseTestCaseResponse(rtc), "QA assigned to release test case"));
    }

    @PatchMapping("/release/{releaseId}/test-case/employee/{employeeId}")
    @PreAuthorize("@access.hasReleaseAccess('RELEASE_UPDATE', #releaseId)")
    @Operation(summary = "Update employee assignment for release test cases")
    public ResponseEntity<ApiResponse<Void>> patchReleaseTestCaseEmployee(
            @PathVariable Long releaseId,
            @PathVariable Long employeeId,
            @Valid @RequestBody(required = false) Map<String, Object> body
    ) {
        releaseService.updateReleaseTestCaseEmployee(releaseId, employeeId, body);
        return ResponseEntity.ok(ApiResponse.success(null, "Release test case employee updated successfully"));
    }

    @GetMapping("/release/{releaseId}/test-case-qa-allocation")
    @PreAuthorize("@access.hasReleaseAccess('RELEASE_READ', #releaseId)")
    @Operation(summary = "Get QA allocations for release test cases")
    public ResponseEntity<ApiResponse<List<ReleaseTestCaseResponse>>> getReleaseTestCaseQaAllocation(@PathVariable Long releaseId) {
        List<ReleaseTestCase> list = releaseService.getReleaseTestCases(releaseId);
        return ResponseEntity.ok(ApiResponse.success(releaseMapper.toReleaseTestCaseResponseList(list), "QA allocations retrieved"));
    }

    @PatchMapping("/release/{releaseId}/test-case/{id}/status")
    @PreAuthorize("@access.hasReleaseAccess('RELEASE_UPDATE', #releaseId)")
    @Operation(summary = "Update release test case execution status")
    public ResponseEntity<ApiResponse<ReleaseTestCaseResponse>> updateTestCaseStatus(
            @PathVariable Long releaseId,
            @PathVariable Long id,
            @Valid @RequestBody Map<String, Object> body
    ) {
        String status = body.containsKey("status") ? String.valueOf(body.get("status")) : "NOT_RUN";
        String comment = body.containsKey("comment") && body.get("comment") != null ? String.valueOf(body.get("comment")) : null;

        Long priorityId = null;
        if (body.containsKey("priorityId") && body.get("priorityId") != null) {
            try {
                priorityId = Long.valueOf(String.valueOf(body.get("priorityId")));
            } catch (Exception ignored) {}
        }
        Long assignedToId = null;
        Object assignVal = body.get("assignedToId") != null ? body.get("assignedToId") : body.get("assignedTo");
        if (assignVal != null) {
            try {
                assignedToId = Long.valueOf(String.valueOf(assignVal));
            } catch (Exception ignored) {}
        }

        ReleaseTestCase rtc = releaseService.updateReleaseTestCaseStatus(releaseId, id, status, comment, priorityId, assignedToId);
        return ResponseEntity.ok(ApiResponse.success(releaseMapper.toReleaseTestCaseResponse(rtc), "Test case execution status updated"));
    }

    @GetMapping("/testcase/allocation-log")
    @PreAuthorize("@access.has('RELEASE_READ')")
    @Operation(summary = "Get test case allocation logs")
    public ResponseEntity<ApiResponse<List<TestCaseAllocationLogResponse>>> getTestCaseAllocationLogs() {
        List<TestCaseAllocationLog> list = releaseService.getTestCaseAllocationLogs();
        return ResponseEntity.ok(ApiResponse.success(releaseMapper.toTestCaseAllocationLogResponseList(list), "Allocation logs retrieved"));
    }
}
