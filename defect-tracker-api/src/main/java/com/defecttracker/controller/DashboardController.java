package com.defecttracker.controller;

import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.dto.response.DashboardSummaryResponse;
import com.defecttracker.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Analytics & Dashboard", description = "Endpoints for metrics, KPIs, defect density, time-to-find/fix and breakdowns")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/project/{projectId}/release/{releaseId}/dashboard")
    @PreAuthorize("@access.hasProjectAccess('PROJECT_READ', #projectId)")
    @Operation(summary = "Get full dashboard summary KPIs for project and release")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary(
            @PathVariable Long projectId,
            @PathVariable Long releaseId
    ) {
        DashboardSummaryResponse summary = dashboardService.getDashboardSummary(projectId, releaseId);
        return ResponseEntity.ok(ApiResponse.success(summary, "Dashboard summary retrieved"));
    }

    @GetMapping("/project/{projectId}/dashboard/reopened")
    @PreAuthorize("@access.hasProjectAccess('PROJECT_READ', #projectId)")
    @Operation(summary = "Get reopened defect counts for project")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReopenedDefects(@PathVariable Long projectId) {
        Map<String, Object> res = dashboardService.getReopenedDefects(projectId);
        return ResponseEntity.ok(ApiResponse.success(res, "Reopened counts retrieved"));
    }

    @GetMapping("/project/{projectId}/release/{releaseId}/dashboard/time-to-find")
    @PreAuthorize("@access.hasProjectAccess('PROJECT_READ', #projectId)")
    @Operation(summary = "Get average time to find defects")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTimeToFind(
            @PathVariable Long projectId,
            @PathVariable Long releaseId
    ) {
        Map<String, Object> res = dashboardService.getTimeToFind(projectId, releaseId);
        return ResponseEntity.ok(ApiResponse.success(res, "Time to find retrieved"));
    }

    @GetMapping("/project/{projectId}/release/{releaseId}/dashboard/time-to-fixed")
    @PreAuthorize("@access.hasProjectAccess('PROJECT_READ', #projectId)")
    @Operation(summary = "Get average time to fix defects")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTimeToFix(
            @PathVariable Long projectId,
            @PathVariable Long releaseId
    ) {
        Map<String, Object> res = dashboardService.getTimeToFix(projectId, releaseId);
        return ResponseEntity.ok(ApiResponse.success(res, "Time to fix retrieved"));
    }

    @GetMapping("/project/{projectId}/defect/severity-breakdown")
    @PreAuthorize("@access.hasProjectAccess('PROJECT_READ', #projectId)")
    @Operation(summary = "Get defect severity breakdown for project")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getSeverityBreakdown(@PathVariable Long projectId) {
        Map<String, Long> breakdown = dashboardService.getSeverityBreakdown(projectId);
        return ResponseEntity.ok(ApiResponse.success(breakdown, "Severity breakdown retrieved"));
    }

    @GetMapping("/project/{projectId}/defect-type")
    @PreAuthorize("@access.hasProjectAccess('PROJECT_READ', #projectId)")
    @Operation(summary = "Get defect count grouped by defect type")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getDefectsByType(@PathVariable Long projectId) {
        Map<String, Long> res = dashboardService.getTypeBreakdown(projectId);
        return ResponseEntity.ok(ApiResponse.success(res, "Defect type breakdown retrieved"));
    }

    @GetMapping("/project/{projectId}/defect-module")
    @PreAuthorize("@access.hasProjectAccess('PROJECT_READ', #projectId)")
    @Operation(summary = "Get defect count grouped by module")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getDefectsByModules(@PathVariable Long projectId) {
        Map<String, Long> res = dashboardService.getModuleBreakdown(projectId);
        return ResponseEntity.ok(ApiResponse.success(res, "Defect module breakdown retrieved"));
    }

    @GetMapping("/project/{projectId}/defect-density")
    @PreAuthorize("@access.hasProjectAccess('PROJECT_READ', #projectId)")
    @Operation(summary = "Get defect density for project")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDefectDensity(@PathVariable Long projectId) {
        Map<String, Object> res = dashboardService.getDefectDensity(projectId);
        return ResponseEntity.ok(ApiResponse.success(res, "Defect density retrieved"));
    }
}
