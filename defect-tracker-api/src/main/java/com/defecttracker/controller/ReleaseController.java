package com.defecttracker.controller;

import com.defecttracker.dto.request.ReleaseCreateRequest;
import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.entity.Release;
import com.defecttracker.entity.ReleaseTestCase;
import com.defecttracker.entity.TestCaseAllocationLog;
import com.defecttracker.service.ReleaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Release & QA Allocation", description = "Endpoints for project releases, release test cases, QA allocations and execution logs")
public class ReleaseController {

    private final ReleaseService releaseService;

    @PostMapping("/release")
    @Operation(summary = "Create release")
    public ResponseEntity<ApiResponse<Release>> createRelease(@Valid @RequestBody ReleaseCreateRequest request) {
        Release release = releaseService.createRelease(request);
        return ResponseEntity.ok(ApiResponse.created(release, "Release created successfully"));
    }

    @GetMapping("/release")
    @Operation(summary = "Get all releases")
    public ResponseEntity<ApiResponse<List<Release>>> getAllReleases(@RequestParam(required = false) Long projectId) {
        if (projectId != null) {
            return ResponseEntity.ok(ApiResponse.success(releaseService.getReleasesByProject(projectId), "Project releases retrieved"));
        }
        return ResponseEntity.ok(ApiResponse.success(releaseService.getAllReleases(), "Releases retrieved"));
    }

    @GetMapping("/release/counts")
    @Operation(summary = "Get counts of releases by state")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReleaseCounts() {
        Map<String, Object> counts = releaseService.getReleaseCounts();
        return ResponseEntity.ok(ApiResponse.success(counts, "Release counts retrieved"));
    }

    @GetMapping("/release/{id}")
    @Operation(summary = "Get release by ID")
    public ResponseEntity<ApiResponse<Release>> getReleaseById(@PathVariable Long id) {
        Release release = releaseService.getReleaseById(id);
        return ResponseEntity.ok(ApiResponse.success(release, "Release found"));
    }

    @PutMapping("/release/{id}")
    @Operation(summary = "Update release")
    public ResponseEntity<ApiResponse<Release>> updateRelease(
            @PathVariable Long id,
            @Valid @RequestBody ReleaseCreateRequest request
    ) {
        Release release = releaseService.updateRelease(id, request);
        return ResponseEntity.ok(ApiResponse.success(release, "Release updated successfully"));
    }

    @DeleteMapping("/release/{id}")
    @Operation(summary = "Delete release")
    public ResponseEntity<ApiResponse<Void>> deleteRelease(@PathVariable Long id) {
        releaseService.deleteRelease(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Release deleted successfully"));
    }

    @PatchMapping("/release/{releaseId}/status")
    @Operation(summary = "Update release status")
    public ResponseEntity<ApiResponse<Release>> updateReleaseStatus(
            @PathVariable Long releaseId,
            @RequestBody Map<String, String> body
    ) {
        String status = body.getOrDefault("status", "PLANNED");
        Release release = releaseService.updateReleaseStatus(releaseId, status);
        return ResponseEntity.ok(ApiResponse.success(release, "Release status updated"));
    }

    @GetMapping("/project/{projectId}/release/active")
    @Operation(summary = "Get active release for a project")
    public ResponseEntity<ApiResponse<Release>> getActiveRelease(@PathVariable Long projectId) {
        Release release = releaseService.getActiveReleaseByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(release, "Active release retrieved"));
    }

    @PatchMapping("/release/{releaseId}/kloc")
    @Operation(summary = "Update release KLOC")
    public ResponseEntity<ApiResponse<Release>> updateReleaseKloc(
            @PathVariable Long releaseId,
            @RequestBody Map<String, Double> body
    ) {
        Double kloc = body.getOrDefault("kloc", 0.0);
        Release release = releaseService.updateReleaseKloc(releaseId, kloc);
        return ResponseEntity.ok(ApiResponse.success(release, "Release KLOC updated"));
    }

    @GetMapping("/release/{releaseId}/test-case")
    @Operation(summary = "Get test cases in a release")
    public ResponseEntity<ApiResponse<List<ReleaseTestCase>>> getReleaseTestCases(@PathVariable Long releaseId) {
        List<ReleaseTestCase> list = releaseService.getReleaseTestCases(releaseId);
        return ResponseEntity.ok(ApiResponse.success(list, "Release test cases retrieved"));
    }

    @GetMapping("/release/{releaseId}/test-case/{id}")
    @Operation(summary = "Get release test case by testcase ID")
    public ResponseEntity<ApiResponse<ReleaseTestCase>> getReleaseTestCase(
            @PathVariable Long releaseId,
            @PathVariable Long id
    ) {
        ReleaseTestCase rtc = releaseService.getReleaseTestCase(releaseId, id);
        return ResponseEntity.ok(ApiResponse.success(rtc, "Release test case found"));
    }

    @PostMapping("/release/{releaseId}/test-case/{testcaseId}/employee")
    @Operation(summary = "Assign QA employee to release test case")
    public ResponseEntity<ApiResponse<ReleaseTestCase>> assignQaToTestCase(
            @PathVariable Long releaseId,
            @PathVariable Long testcaseId,
            @RequestBody Map<String, Long> body
    ) {
        Long employeeId = body.get("employeeId");
        ReleaseTestCase rtc = releaseService.assignQaToReleaseTestCase(releaseId, testcaseId, employeeId);
        return ResponseEntity.ok(ApiResponse.success(rtc, "QA assigned to release test case"));
    }

    @GetMapping("/release/{releaseId}/test-case-qa-allocation")
    @Operation(summary = "Get QA allocations for release test cases")
    public ResponseEntity<ApiResponse<List<ReleaseTestCase>>> getReleaseTestCaseQaAllocation(@PathVariable Long releaseId) {
        List<ReleaseTestCase> list = releaseService.getReleaseTestCases(releaseId);
        return ResponseEntity.ok(ApiResponse.success(list, "QA allocations retrieved"));
    }

    @PatchMapping("/release/{releaseId}/test-case/{id}/status")
    @Operation(summary = "Update release test case execution status")
    public ResponseEntity<ApiResponse<ReleaseTestCase>> updateTestCaseStatus(
            @PathVariable Long releaseId,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {
        String status = body.containsKey("status") ? String.valueOf(body.get("status")) : "NOT_RUN";
        String comment = body.containsKey("comment") && body.get("comment") != null ? String.valueOf(body.get("comment")) : null;
        ReleaseTestCase rtc = releaseService.updateReleaseTestCaseStatus(releaseId, id, status, comment);
        return ResponseEntity.ok(ApiResponse.success(rtc, "Test case execution status updated"));
    }

    @GetMapping("/testcase/allocation-log")
    @Operation(summary = "Get test case allocation logs")
    public ResponseEntity<ApiResponse<List<TestCaseAllocationLog>>> getTestCaseAllocationLogs() {
        List<TestCaseAllocationLog> list = releaseService.getTestCaseAllocationLogs();
        return ResponseEntity.ok(ApiResponse.success(list, "Allocation logs retrieved"));
    }
}
