package com.defecttracker.modules.defect.controller;

import com.defecttracker.common.dto.ApiResponse;
import com.defecttracker.common.dto.PageResponse;
import com.defecttracker.modules.defect.dto.DefectDto;
import com.defecttracker.modules.defect.service.DefectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Defect Management", description = "Endpoints for project-scoped sequential defects, workflow status changes, history logs, threaded comments, and density analytics")
public class DefectController {

    private final DefectService defectService;

    @GetMapping("/projects/{projectId}/defects")
    @PreAuthorize("hasAuthority('DEFECT:READ') or hasRole('ADMIN')")
    @Operation(summary = "Get paginated, filterable defects for a project")
    public ResponseEntity<ApiResponse<PageResponse<DefectDto.DefectResponse>>> getDefects(
            @PathVariable Long projectId,
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) Long severityId,
            @RequestParam(required = false) Long priorityId,
            @RequestParam(required = false) Long assignedToId,
            @RequestParam(required = false) Long releaseId,
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PageResponse<DefectDto.DefectResponse> response = defectService.getFilteredDefects(
                projectId, statusId, severityId, priorityId, assignedToId, releaseId, query, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/defects/{id}")
    @PreAuthorize("hasAuthority('DEFECT:READ') or hasRole('ADMIN')")
    @Operation(summary = "Get defect by ID")
    public ResponseEntity<ApiResponse<DefectDto.DefectResponse>> getDefectById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(defectService.getDefectById(id)));
    }

    @PostMapping("/defects")
    @PreAuthorize("hasAuthority('DEFECT:CREATE') or hasRole('ADMIN')")
    @Operation(summary = "Create a new defect with project-scoped sequential numbering (e.g. PRJ-1)")
    public ResponseEntity<ApiResponse<DefectDto.DefectResponse>> createDefect(
            @Valid @RequestBody DefectDto.DefectCreateRequest request) {
        DefectDto.DefectResponse response = defectService.createDefect(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Defect logged successfully", response));
    }

    @PutMapping("/defects/{id}")
    @PreAuthorize("hasAuthority('DEFECT:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Update defect details")
    public ResponseEntity<ApiResponse<DefectDto.DefectResponse>> updateDefect(
            @PathVariable Long id,
            @Valid @RequestBody DefectDto.DefectUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Defect updated", defectService.updateDefect(id, request)));
    }

    @PutMapping("/defects/{id}/status")
    @PreAuthorize("hasAuthority('DEFECT:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Transition defect status: validates workflow graph, logs immutable history, and increments reopen counter if applicable")
    public ResponseEntity<ApiResponse<DefectDto.DefectResponse>> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody DefectDto.StatusChangeRequest request) {
        DefectDto.DefectResponse response = defectService.changeStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Defect status transitioned successfully", response));
    }

    @GetMapping("/defects/{id}/history")
    @PreAuthorize("hasAuthority('DEFECT:READ') or hasRole('ADMIN')")
    @Operation(summary = "Get immutable status change history for a defect")
    public ResponseEntity<ApiResponse<List<DefectDto.DefectHistoryDto>>> getDefectHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(defectService.getDefectHistory(id)));
    }

    @GetMapping("/defects/{id}/comments")
    @PreAuthorize("hasAuthority('DEFECT:READ') or hasRole('ADMIN')")
    @Operation(summary = "Get threaded comments for a defect")
    public ResponseEntity<ApiResponse<List<DefectDto.CommentResponse>>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(defectService.getDefectComments(id)));
    }

    @PostMapping("/defects/{id}/comments")
    @PreAuthorize("hasAuthority('DEFECT:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Add a comment or threaded reply to a defect")
    public ResponseEntity<ApiResponse<DefectDto.CommentResponse>> addComment(
            @PathVariable Long id,
            @Valid @RequestBody DefectDto.CommentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added", defectService.addComment(id, request)));
    }

    @PostMapping("/defects/bulk-reassign")
    @PreAuthorize("hasAuthority('DEFECT:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Bulk reassign multiple defects to a developer")
    public ResponseEntity<ApiResponse<Integer>> bulkReassign(
            @Valid @RequestBody DefectDto.BulkReassignRequest request) {
        int count = defectService.bulkReassign(request);
        return ResponseEntity.ok(ApiResponse.success(count + " defects reassigned successfully", count));
    }

    @GetMapping("/projects/{projectId}/defects/analytics")
    @PreAuthorize("hasAuthority('DEFECT:READ') or hasRole('ADMIN')")
    @Operation(summary = "Get comprehensive defect analytics: severity/module breakdown, time-to-find/fix, and defect density relative to KLOC")
    public ResponseEntity<ApiResponse<DefectDto.DefectAnalyticsReport>> getProjectAnalytics(
            @PathVariable Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(ApiResponse.success(defectService.getProjectAnalytics(projectId, startDate, endDate)));
    }
}
