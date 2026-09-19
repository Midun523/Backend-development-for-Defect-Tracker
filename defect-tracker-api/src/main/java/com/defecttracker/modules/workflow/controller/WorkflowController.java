package com.defecttracker.modules.workflow.controller;

import com.defecttracker.common.dto.ApiResponse;
import com.defecttracker.modules.workflow.dto.WorkflowDto;
import com.defecttracker.modules.workflow.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workflow")
@RequiredArgsConstructor
@Tag(name = "Configurable Workflow", description = "Admin-editable status transition graph (nodes & edges) and next valid status resolution")
public class WorkflowController {

    private final WorkflowService workflowService;

    @GetMapping("/graph")
    @Operation(summary = "Get full workflow graph (status nodes with canvas coordinates & transition edges)")
    public ResponseEntity<ApiResponse<WorkflowDto.WorkflowGraphResponse>> getWorkflowGraph() {
        return ResponseEntity.ok(ApiResponse.success(workflowService.getWorkflowGraph()));
    }

    @GetMapping("/statuses")
    @Operation(summary = "Get all status types")
    public ResponseEntity<ApiResponse<List<WorkflowDto.StatusTypeDto>>> getAllStatuses() {
        return ResponseEntity.ok(ApiResponse.success(workflowService.getAllStatuses()));
    }

    @PostMapping("/statuses")
    @PreAuthorize("hasAuthority('WORKFLOW:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Create a new status type node")
    public ResponseEntity<ApiResponse<WorkflowDto.StatusTypeDto>> createStatus(
            @Valid @RequestBody WorkflowDto.StatusTypeDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Status type created successfully", workflowService.createStatus(dto)));
    }

    @GetMapping("/transitions/{statusId}/next")
    @Operation(summary = "Report which next statuses are valid from a given current status")
    public ResponseEntity<ApiResponse<List<WorkflowDto.StatusTypeDto>>> getNextValidStatuses(
            @PathVariable Long statusId) {
        List<WorkflowDto.StatusTypeDto> nextStatuses = workflowService.getNextValidStatuses(statusId);
        return ResponseEntity.ok(ApiResponse.success(nextStatuses));
    }

    @PostMapping("/transitions")
    @PreAuthorize("hasAuthority('WORKFLOW:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Add an allowed status transition edge")
    public ResponseEntity<ApiResponse<WorkflowDto.WorkflowTransitionDto>> addTransition(
            @Valid @RequestBody WorkflowDto.WorkflowTransitionRequest request) {
        WorkflowDto.WorkflowTransitionDto transition = workflowService.addTransition(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Workflow transition edge added successfully", transition));
    }

    @DeleteMapping("/transitions")
    @PreAuthorize("hasAuthority('WORKFLOW:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Delete an allowed status transition edge")
    public ResponseEntity<ApiResponse<Void>> deleteTransition(
            @RequestParam Long fromStatusId,
            @RequestParam Long toStatusId) {
        workflowService.deleteTransition(fromStatusId, toStatusId);
        return ResponseEntity.ok(ApiResponse.success("Workflow transition edge removed", null));
    }

    @PutMapping("/positions")
    @PreAuthorize("hasAuthority('WORKFLOW:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Persist visual node positions on the workflow canvas")
    public ResponseEntity<ApiResponse<Void>> updateNodePositions(
            @Valid @RequestBody WorkflowDto.UpdateNodePositionsRequest request) {
        workflowService.updateNodePositions(request);
        return ResponseEntity.ok(ApiResponse.success("Workflow node positions saved", null));
    }
}
