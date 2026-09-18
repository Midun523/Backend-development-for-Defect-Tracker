package com.defecttracker.service.impl;

import com.defecttracker.dto.request.WorkflowPositionRequest;
import com.defecttracker.entity.Project;
import com.defecttracker.entity.StatusType;
import com.defecttracker.entity.WorkflowPosition;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.repository.ProjectRepository;
import com.defecttracker.repository.StatusTypeRepository;
import com.defecttracker.repository.WorkflowPositionRepository;
import com.defecttracker.service.WorkflowPositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkflowPositionServiceImpl implements WorkflowPositionService {

    private final WorkflowPositionRepository workflowPositionRepository;
    private final StatusTypeRepository statusTypeRepository;
    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public WorkflowPosition savePosition(WorkflowPositionRequest request) {
        StatusType statusType = statusTypeRepository.findById(request.getStatusTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("StatusType", "id", request.getStatusTypeId()));

        Project project = null;
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId()).orElse(null);
        }

        Optional<WorkflowPosition> existingOpt;
        if (project != null) {
            existingOpt = workflowPositionRepository.findByStatusTypeIdAndProjectId(statusType.getId(), project.getId());
        } else {
            existingOpt = workflowPositionRepository.findByStatusTypeIdAndProjectIsNull(statusType.getId());
        }

        WorkflowPosition position;
        if (existingOpt.isPresent()) {
            position = existingOpt.get();
            position.setPositionX(request.getPositionX());
            position.setPositionY(request.getPositionY());
        } else {
            position = WorkflowPosition.builder()
                    .statusType(statusType)
                    .project(project)
                    .positionX(request.getPositionX())
                    .positionY(request.getPositionY())
                    .build();
        }

        return workflowPositionRepository.save(position);
    }

    @Override
    @Transactional
    public List<WorkflowPosition> saveBulkPositions(List<WorkflowPositionRequest> requests) {
        List<WorkflowPosition> results = new ArrayList<>();
        for (WorkflowPositionRequest req : requests) {
            results.add(savePosition(req));
        }
        return results;
    }

    @Override
    public List<WorkflowPosition> getPositionsByProject(Long projectId) {
        if (projectId != null) {
            return workflowPositionRepository.findByProjectId(projectId);
        }
        return workflowPositionRepository.findAll();
    }

    @Override
    public List<WorkflowPosition> getAllPositions() {
        return workflowPositionRepository.findAll();
    }
}
