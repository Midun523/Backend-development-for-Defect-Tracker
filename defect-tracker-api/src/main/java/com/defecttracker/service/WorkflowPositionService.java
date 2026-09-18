package com.defecttracker.service;

import com.defecttracker.dto.request.WorkflowPositionRequest;
import com.defecttracker.entity.WorkflowPosition;

import java.util.List;

public interface WorkflowPositionService {
    WorkflowPosition savePosition(WorkflowPositionRequest request);
    List<WorkflowPosition> saveBulkPositions(List<WorkflowPositionRequest> requests);
    List<WorkflowPosition> getPositionsByProject(Long projectId);
    List<WorkflowPosition> getAllPositions();
}
