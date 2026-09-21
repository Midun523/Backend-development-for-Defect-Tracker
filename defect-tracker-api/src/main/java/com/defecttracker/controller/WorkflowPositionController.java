package com.defecttracker.controller;

import com.defecttracker.dto.request.WorkflowPositionRequest;
import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.dto.response.WorkflowPositionResponse;
import com.defecttracker.entity.WorkflowPosition;
import com.defecttracker.mapper.ClassificationMapper;
import com.defecttracker.service.WorkflowPositionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workflow/positions")
@RequiredArgsConstructor
@Tag(name = "Workflow Position Management", description = "Endpoints for saving and retrieving canvas node coordinates matching ERD WorkFlowPosition")
public class WorkflowPositionController {

    private final WorkflowPositionService workflowPositionService;
    private final ClassificationMapper classificationMapper;

    @PostMapping
    @PreAuthorize("@access.has('WORKFLOW_CREATE') or @access.has('WORKFLOW_UPDATE')")
    @Operation(summary = "Save single workflow node position")
    public ResponseEntity<ApiResponse<WorkflowPositionResponse>> savePosition(@Valid @RequestBody WorkflowPositionRequest request) {
        WorkflowPosition position = workflowPositionService.savePosition(request);
        return ResponseEntity.ok(ApiResponse.success(classificationMapper.toWorkflowPositionResponse(position), "Position saved successfully"));
    }

    @PostMapping("/bulk")
    @PreAuthorize("@access.has('WORKFLOW_CREATE') or @access.has('WORKFLOW_UPDATE')")
    @Operation(summary = "Save bulk workflow node positions")
    public ResponseEntity<ApiResponse<List<WorkflowPositionResponse>>> saveBulkPositions(@Valid @RequestBody List<WorkflowPositionRequest> requests) {
        List<WorkflowPosition> positions = workflowPositionService.saveBulkPositions(requests);
        return ResponseEntity.ok(ApiResponse.success(classificationMapper.toWorkflowPositionResponseList(positions), "Bulk positions saved successfully"));
    }

    @GetMapping
    @PreAuthorize("@access.has('WORKFLOW_READ')")
    @Operation(summary = "Get workflow positions optionally filtered by project")
    public ResponseEntity<ApiResponse<List<WorkflowPositionResponse>>> getPositions(@RequestParam(required = false) Long projectId) {
        List<WorkflowPosition> positions = workflowPositionService.getPositionsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(classificationMapper.toWorkflowPositionResponseList(positions), "Positions retrieved"));
    }
}
