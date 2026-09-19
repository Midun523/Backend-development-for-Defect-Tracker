package com.defecttracker.modules.workflow.service;

import com.defecttracker.common.exception.BusinessRuleException;
import com.defecttracker.common.exception.ResourceNotFoundException;
import com.defecttracker.modules.workflow.dto.WorkflowDto;
import com.defecttracker.modules.workflow.entity.StatusType;
import com.defecttracker.modules.workflow.entity.WorkflowTransition;
import com.defecttracker.modules.workflow.repository.StatusTypeRepository;
import com.defecttracker.modules.workflow.repository.WorkflowTransitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final StatusTypeRepository statusTypeRepository;
    private final WorkflowTransitionRepository transitionRepository;

    /**
     * Get the full configurable workflow graph with nodes and directed edges.
     */
    @Transactional(readOnly = true)
    public WorkflowDto.WorkflowGraphResponse getWorkflowGraph() {
        List<StatusType> statuses = statusTypeRepository.findAll();
        List<WorkflowTransition> transitions = transitionRepository.findAll();

        List<WorkflowDto.StatusTypeDto> nodes = statuses.stream()
                .map(this::mapStatusType)
                .collect(Collectors.toList());

        List<WorkflowDto.WorkflowTransitionDto> edges = transitions.stream()
                .map(this::mapTransition)
                .collect(Collectors.toList());

        return WorkflowDto.WorkflowGraphResponse.builder()
                .nodes(nodes)
                .edges(edges)
                .build();
    }

    /**
     * Get all statuses.
     */
    @Transactional(readOnly = true)
    public List<WorkflowDto.StatusTypeDto> getAllStatuses() {
        return statusTypeRepository.findAll().stream()
                .map(this::mapStatusType)
                .collect(Collectors.toList());
    }

    /**
     * Get next valid statuses that can be transitioned to from currentStatusId.
     */
    @Transactional(readOnly = true)
    public List<WorkflowDto.StatusTypeDto> getNextValidStatuses(Long currentStatusId) {
        StatusType current = statusTypeRepository.findById(currentStatusId)
                .orElseThrow(() -> new ResourceNotFoundException("StatusType", "id", currentStatusId));

        List<WorkflowTransition> transitions = transitionRepository.findByFromStatusId(current.getId());
        return transitions.stream()
                .map(t -> mapStatusType(t.getToStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Validate whether a transition from fromStatusId to toStatusId is allowed by the workflow graph.
     * Throws BusinessRuleException if invalid.
     */
    @Transactional(readOnly = true)
    public void validateStatusTransition(Long fromStatusId, Long toStatusId) {
        if (fromStatusId.equals(toStatusId)) {
            return; // No change
        }

        boolean validEdge = transitionRepository.existsByFromStatusIdAndToStatusId(fromStatusId, toStatusId);
        if (!validEdge) {
            StatusType from = statusTypeRepository.findById(fromStatusId).orElse(null);
            StatusType to = statusTypeRepository.findById(toStatusId).orElse(null);
            String fromName = from != null ? from.getName() : String.valueOf(fromStatusId);
            String toName = to != null ? to.getName() : String.valueOf(toStatusId);

            throw new BusinessRuleException(String.format(
                    "Invalid status transition from '%s' to '%s'. No transition edge exists in the workflow configuration.",
                    fromName, toName
            ));
        }
    }

    /**
     * Check if this transition represents a "reopen":
     * Reopening is defined as transitioning back to an open-equivalent status
     * after having reached a resolved-equivalent one.
     */
    @Transactional(readOnly = true)
    public boolean isReopenTransition(Long fromStatusId, Long toStatusId) {
        StatusType from = statusTypeRepository.findById(fromStatusId)
                .orElseThrow(() -> new ResourceNotFoundException("StatusType", "id", fromStatusId));
        StatusType to = statusTypeRepository.findById(toStatusId)
                .orElseThrow(() -> new ResourceNotFoundException("StatusType", "id", toStatusId));

        return from.isResolvedStage() && to.isOpenStage();
    }

    /**
     * Add a new allowed transition edge.
     */
    @Transactional
    public WorkflowDto.WorkflowTransitionDto addTransition(WorkflowDto.WorkflowTransitionRequest request) {
        if (request.getFromStatusId().equals(request.getToStatusId())) {
            throw new BusinessRuleException("A status transition cannot point to itself");
        }

        StatusType from = statusTypeRepository.findById(request.getFromStatusId())
                .orElseThrow(() -> new ResourceNotFoundException("StatusType", "id", request.getFromStatusId()));
        StatusType to = statusTypeRepository.findById(request.getToStatusId())
                .orElseThrow(() -> new ResourceNotFoundException("StatusType", "id", request.getToStatusId()));

        if (transitionRepository.existsByFromStatusIdAndToStatusId(from.getId(), to.getId())) {
            throw new BusinessRuleException(String.format("Transition from '%s' to '%s' already exists", from.getName(), to.getName()));
        }

        WorkflowTransition transition = WorkflowTransition.builder()
                .fromStatus(from)
                .toStatus(to)
                .name(request.getName())
                .description(request.getDescription())
                .requiresComment(request.isRequiresComment())
                .build();

        return mapTransition(transitionRepository.save(transition));
    }

    /**
     * Delete an allowed transition edge.
     */
    @Transactional
    public void deleteTransition(Long fromStatusId, Long toStatusId) {
        transitionRepository.deleteByFromStatusIdAndToStatusId(fromStatusId, toStatusId);
    }

    /**
     * Update node positions on the visual workflow canvas.
     */
    @Transactional
    public void updateNodePositions(WorkflowDto.UpdateNodePositionsRequest request) {
        for (WorkflowDto.NodePositionItem item : request.getPositions()) {
            statusTypeRepository.findById(item.getStatusId()).ifPresent(status -> {
                status.setPositionX(item.getPositionX());
                status.setPositionY(item.getPositionY());
                statusTypeRepository.save(status);
            });
        }
    }

    /**
     * Create a new status type node.
     */
    @Transactional
    public WorkflowDto.StatusTypeDto createStatus(WorkflowDto.StatusTypeDto dto) {
        if (statusTypeRepository.existsByName(dto.getName())) {
            throw new BusinessRuleException("Status with name '" + dto.getName() + "' already exists");
        }

        StatusType status = StatusType.builder()
                .name(dto.getName())
                .category(dto.getCategory() != null ? dto.getCategory() : "OPEN")
                .displayColor(dto.getDisplayColor() != null ? dto.getDisplayColor() : "#3B82F6")
                .defaultInitial(dto.isDefaultInitial())
                .openStage(dto.isOpenStage())
                .resolvedStage(dto.isResolvedStage())
                .positionX(dto.getPositionX() != null ? dto.getPositionX() : 100.0)
                .positionY(dto.getPositionY() != null ? dto.getPositionY() : 100.0)
                .build();

        return mapStatusType(statusTypeRepository.save(status));
    }

    private WorkflowDto.StatusTypeDto mapStatusType(StatusType s) {
        return WorkflowDto.StatusTypeDto.builder()
                .id(s.getId())
                .name(s.getName())
                .category(s.getCategory())
                .displayColor(s.getDisplayColor())
                .defaultInitial(s.isDefaultInitial())
                .openStage(s.isOpenStage())
                .resolvedStage(s.isResolvedStage())
                .positionX(s.getPositionX())
                .positionY(s.getPositionY())
                .build();
    }

    private WorkflowDto.WorkflowTransitionDto mapTransition(WorkflowTransition t) {
        return WorkflowDto.WorkflowTransitionDto.builder()
                .id(t.getId())
                .fromStatusId(t.getFromStatus().getId())
                .fromStatusName(t.getFromStatus().getName())
                .toStatusId(t.getToStatus().getId())
                .toStatusName(t.getToStatus().getName())
                .name(t.getName())
                .description(t.getDescription())
                .requiresComment(t.isRequiresComment())
                .build();
    }
}
