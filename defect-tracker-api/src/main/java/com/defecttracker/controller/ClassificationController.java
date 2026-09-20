package com.defecttracker.controller;

import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.entity.*;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.defecttracker.dto.request.WorkflowSaveRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Master Classifications & Workflow", description = "Endpoints for priorities, severities, defect types, release types, status types and workflows")
public class ClassificationController {

    private final PriorityRepository priorityRepository;
    private final SeverityRepository severityRepository;
    private final DefectTypeRepository defectTypeRepository;
    private final ReleaseTypeRepository releaseTypeRepository;
    private final StatusTypeRepository statusTypeRepository;
    private final StatusTransitionRepository statusTransitionRepository;
    private final WorkflowPositionRepository workflowPositionRepository;
    private final DefectRepository defectRepository;

    // --- Priority ---
    @GetMapping("/priority")
    @PreAuthorize("@access.has('PRIORITY_READ')")
    public ResponseEntity<ApiResponse<Object>> getPriorities(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null && size != null) {
            Page<Priority> p = priorityRepository.findAll(PageRequest.of(page, size, Sort.by("id")));
            PaginatedResponse<Priority> res = PaginatedResponse.<Priority>builder()
                    .content(p.getContent()).pageNumber(p.getNumber()).pageSize(p.getSize()).totalElements(p.getTotalElements()).totalPages(p.getTotalPages()).build();
            return ResponseEntity.ok(ApiResponse.success(res, "Priorities retrieved"));
        }
        return ResponseEntity.ok(ApiResponse.success(priorityRepository.findAll(), "Priorities retrieved"));
    }

    @GetMapping("/priority/{id}")
    @PreAuthorize("@access.has('PRIORITY_READ')")
    public ResponseEntity<ApiResponse<Priority>> getPriorityById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(priorityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Priority", "id", id))));
    }

    @PostMapping("/priority")
    @PreAuthorize("@access.has('PRIORITY_CREATE')")
    public ResponseEntity<ApiResponse<Priority>> createPriority(@RequestBody Priority priority) {
        return ResponseEntity.ok(ApiResponse.created(priorityRepository.save(priority), "Priority created"));
    }

    @PutMapping("/priority/{id}")
    @PreAuthorize("@access.has('PRIORITY_UPDATE')")
    public ResponseEntity<ApiResponse<Priority>> updatePriority(@PathVariable Long id, @RequestBody Priority req) {
        Priority p = priorityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Priority", "id", id));
        p.setName(req.getName());
        p.setColor(req.getColor());
        p.setDescription(req.getDescription());
        return ResponseEntity.ok(ApiResponse.success(priorityRepository.save(p), "Priority updated"));
    }

    @DeleteMapping("/priority/{id}")
    @PreAuthorize("@access.has('PRIORITY_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deletePriority(@PathVariable Long id) {
        priorityRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Priority deleted"));
    }

    // --- Severity ---
    @GetMapping("/severity")
    @PreAuthorize("@access.has('SEVERITY_READ')")
    public ResponseEntity<ApiResponse<Object>> getSeverities(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null && size != null) {
            Page<Severity> p = severityRepository.findAll(PageRequest.of(page, size, Sort.by("id")));
            PaginatedResponse<Severity> res = PaginatedResponse.<Severity>builder()
                    .content(p.getContent()).pageNumber(p.getNumber()).pageSize(p.getSize()).totalElements(p.getTotalElements()).totalPages(p.getTotalPages()).build();
            return ResponseEntity.ok(ApiResponse.success(res, "Severities retrieved"));
        }
        return ResponseEntity.ok(ApiResponse.success(severityRepository.findAll(), "Severities retrieved"));
    }

    @GetMapping("/severity/{id}")
    @PreAuthorize("@access.has('SEVERITY_READ')")
    public ResponseEntity<ApiResponse<Severity>> getSeverityById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(severityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Severity", "id", id))));
    }

    @PostMapping("/severity")
    @PreAuthorize("@access.has('SEVERITY_CREATE')")
    public ResponseEntity<ApiResponse<Severity>> createSeverity(@RequestBody Severity severity) {
        return ResponseEntity.ok(ApiResponse.created(severityRepository.save(severity), "Severity created"));
    }

    @PutMapping("/severity/{id}")
    @PreAuthorize("@access.has('SEVERITY_UPDATE')")
    public ResponseEntity<ApiResponse<Severity>> updateSeverity(@PathVariable Long id, @RequestBody Severity req) {
        Severity s = severityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Severity", "id", id));
        s.setName(req.getName());
        s.setColor(req.getColor());
        s.setDescription(req.getDescription());
        return ResponseEntity.ok(ApiResponse.success(severityRepository.save(s), "Severity updated"));
    }

    @DeleteMapping("/severity/{id}")
    @PreAuthorize("@access.has('SEVERITY_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteSeverity(@PathVariable Long id) {
        severityRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Severity deleted"));
    }

    // --- Defect Type ---
    @GetMapping("/defect-type")
    @PreAuthorize("@access.has('DEFECT_TYPE_READ')")
    public ResponseEntity<ApiResponse<Object>> getDefectTypes(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null && size != null) {
            Page<DefectType> p = defectTypeRepository.findAll(PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by("id")));
            PaginatedResponse<DefectType> res = PaginatedResponse.<DefectType>builder()
                    .content(p.getContent()).pageNumber(p.getNumber()).pageSize(p.getSize()).totalElements(p.getTotalElements()).totalPages(p.getTotalPages()).build();
            return ResponseEntity.ok(ApiResponse.success(res, "Defect types retrieved"));
        }
        List<DefectType> all = defectTypeRepository.findAll(Sort.by("id"));
        PaginatedResponse<DefectType> res = PaginatedResponse.<DefectType>builder()
                .content(all).pageNumber(0).pageSize(all.size()).totalElements((long) all.size()).totalPages(1).build();
        return ResponseEntity.ok(ApiResponse.success(res, "Defect types retrieved"));
    }

    @GetMapping("/defect-type/{id}")
    @PreAuthorize("@access.has('DEFECT_TYPE_READ')")
    public ResponseEntity<ApiResponse<DefectType>> getDefectTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(defectTypeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("DefectType", "id", id))));
    }

    @PostMapping("/defect-type")
    @PreAuthorize("@access.has('DEFECT_TYPE_CREATE')")
    public ResponseEntity<ApiResponse<DefectType>> createDefectType(@RequestBody DefectType defectType) {
        String name = defectType.getName() != null && !defectType.getName().trim().isEmpty()
                ? defectType.getName().trim()
                : (defectType.getDefectTypeName() != null ? defectType.getDefectTypeName().trim() : null);
        if (name != null) {
            defectType.setName(name);
        }
        return ResponseEntity.ok(ApiResponse.created(defectTypeRepository.save(defectType), "Defect type created"));
    }

    @PutMapping("/defect-type/{id}")
    @PreAuthorize("@access.has('DEFECT_TYPE_UPDATE')")
    public ResponseEntity<ApiResponse<DefectType>> updateDefectType(@PathVariable Long id, @RequestBody DefectType req) {
        DefectType dt = defectTypeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("DefectType", "id", id));
        String name = req.getName() != null && !req.getName().trim().isEmpty()
                ? req.getName().trim()
                : (req.getDefectTypeName() != null ? req.getDefectTypeName().trim() : null);
        if (name != null) {
            dt.setName(name);
        }
        if (req.getDescription() != null) {
            dt.setDescription(req.getDescription());
        }
        return ResponseEntity.ok(ApiResponse.success(defectTypeRepository.save(dt), "Defect type updated"));
    }

    @DeleteMapping("/defect-type/{id}")
    @PreAuthorize("@access.has('DEFECT_TYPE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteDefectType(@PathVariable Long id) {
        defectTypeRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Defect type deleted"));
    }

    // --- Release Type ---
    @GetMapping("/release-type")
    @PreAuthorize("@access.has('RELEASE_TYPE_READ')")
    public ResponseEntity<ApiResponse<Object>> getReleaseTypes(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null && size != null) {
            Page<ReleaseType> p = releaseTypeRepository.findAll(PageRequest.of(page, size, Sort.by("id")));
            PaginatedResponse<ReleaseType> res = PaginatedResponse.<ReleaseType>builder()
                    .content(p.getContent()).pageNumber(p.getNumber()).pageSize(p.getSize()).totalElements(p.getTotalElements()).totalPages(p.getTotalPages()).build();
            return ResponseEntity.ok(ApiResponse.success(res, "Release types retrieved"));
        }
        return ResponseEntity.ok(ApiResponse.success(releaseTypeRepository.findAll(), "Release types retrieved"));
    }

    @GetMapping("/release-type/{id}")
    @PreAuthorize("@access.has('RELEASE_TYPE_READ')")
    public ResponseEntity<ApiResponse<ReleaseType>> getReleaseTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(releaseTypeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ReleaseType", "id", id))));
    }

    @PostMapping("/release-type")
    @PreAuthorize("@access.has('RELEASE_TYPE_CREATE')")
    public ResponseEntity<ApiResponse<ReleaseType>> createReleaseType(@RequestBody ReleaseType releaseType) {
        return ResponseEntity.ok(ApiResponse.created(releaseTypeRepository.save(releaseType), "Release type created"));
    }

    @PutMapping("/release-type/{id}")
    @PreAuthorize("@access.has('RELEASE_TYPE_UPDATE')")
    public ResponseEntity<ApiResponse<ReleaseType>> updateReleaseType(@PathVariable Long id, @RequestBody ReleaseType req) {
        ReleaseType rt = releaseTypeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ReleaseType", "id", id));
        rt.setName(req.getName());
        rt.setDescription(req.getDescription());
        return ResponseEntity.ok(ApiResponse.success(releaseTypeRepository.save(rt), "Release type updated"));
    }

    @DeleteMapping("/release-type/{id}")
    @PreAuthorize("@access.has('RELEASE_TYPE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteReleaseType(@PathVariable Long id) {
        releaseTypeRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Release type deleted"));
    }

    // --- Status Type & Workflow ---
    @GetMapping("/status-type")
    @PreAuthorize("@access.has('STATUS_TYPE_READ')")
    public ResponseEntity<ApiResponse<Object>> getStatusTypes(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null && size != null) {
            Page<StatusType> p = statusTypeRepository.findAll(PageRequest.of(page, size, Sort.by("orderIndex").and(Sort.by("id"))));
            PaginatedResponse<StatusType> res = PaginatedResponse.<StatusType>builder()
                    .content(p.getContent()).pageNumber(p.getNumber()).pageSize(p.getSize()).totalElements(p.getTotalElements()).totalPages(p.getTotalPages()).build();
            return ResponseEntity.ok(ApiResponse.success(res, "Status types retrieved"));
        }
        return ResponseEntity.ok(ApiResponse.success(statusTypeRepository.findAll(Sort.by("orderIndex").and(Sort.by("id"))), "Status types retrieved"));
    }

    @GetMapping("/status-type/{id}")
    @PreAuthorize("@access.has('STATUS_TYPE_READ')")
    public ResponseEntity<ApiResponse<StatusType>> getStatusTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(statusTypeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("StatusType", "id", id))));
    }

    @PostMapping("/status-type")
    @PreAuthorize("@access.has('STATUS_TYPE_CREATE')")
    public ResponseEntity<ApiResponse<StatusType>> createStatusType(@RequestBody StatusType statusType) {
        if (statusType.getName() == null && statusType.getDefectStatusName() != null) {
            statusType.setName(statusType.getDefectStatusName());
        } else if (statusType.getName() == null && statusType.getStatusName() != null) {
            statusType.setName(statusType.getStatusName());
        }
        if (statusType.getColor() == null && statusType.getColorCode() != null) {
            statusType.setColor(statusType.getColorCode());
        }
        if (statusType.getType() == null && statusType.getStatusType() != null) {
            statusType.setType(statusType.getStatusType());
        }
        return ResponseEntity.ok(ApiResponse.created(statusTypeRepository.save(statusType), "Status type created successfully"));
    }

    @PutMapping("/status-type/{id}")
    @PreAuthorize("@access.has('STATUS_TYPE_UPDATE')")
    public ResponseEntity<ApiResponse<StatusType>> updateStatusType(@PathVariable Long id, @RequestBody StatusType req) {
        StatusType st = statusTypeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("StatusType", "id", id));
        if (req.getName() != null) {
            st.setName(req.getName());
        } else if (req.getDefectStatusName() != null) {
            st.setName(req.getDefectStatusName());
        } else if (req.getStatusName() != null) {
            st.setName(req.getStatusName());
        }

        if (req.getColor() != null) {
            st.setColor(req.getColor());
        } else if (req.getColorCode() != null) {
            st.setColor(req.getColorCode());
        }

        if (req.getDescription() != null) {
            st.setDescription(req.getDescription());
        }
        st.setDefault(req.isDefault());
        st.setOrderIndex(req.getOrderIndex());

        if (req.getType() != null) {
            st.setType(req.getType());
        } else if (req.getStatusType() != null) {
            st.setType(req.getStatusType());
        }

        return ResponseEntity.ok(ApiResponse.success(statusTypeRepository.save(st), "Status type updated successfully"));
    }

    @DeleteMapping("/status-type/{id}")
    @PreAuthorize("@access.has('STATUS_TYPE_DELETE')")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<Void>> deleteStatusType(@PathVariable Long id) {
        if (!statusTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("StatusType", "id", id);
        }
        if (defectRepository.countByDefectStatusId(id) > 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Cannot delete status type because it is currently assigned to one or more defects"));
        }
        statusTransitionRepository.deleteAll(statusTransitionRepository.findByFromStatusId(id));
        statusTransitionRepository.deleteAll(statusTransitionRepository.findByToStatusId(id));
        workflowPositionRepository.deleteAll(workflowPositionRepository.findByStatusTypeId(id));
        statusTypeRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Status type deleted successfully"));
    }

    @GetMapping("/status/{id}/next")
    @PreAuthorize("@access.has('WORKFLOW_READ')")
    @Operation(summary = "Get valid next statuses for a status ID in the workflow")
    public ResponseEntity<ApiResponse<List<StatusType>>> getNextStatuses(@PathVariable Long id) {
        List<StatusTransition> transitions = statusTransitionRepository.findByFromStatusId(id);
        List<StatusType> nextStatuses = transitions.stream().map(StatusTransition::getToStatus).collect(Collectors.toList());
        if (nextStatuses.isEmpty()) {
            nextStatuses = statusTypeRepository.findAll();
        }
        return ResponseEntity.ok(ApiResponse.success(nextStatuses, "Next statuses retrieved"));
    }

    @GetMapping("/status/workflow")
    @PreAuthorize("@access.has('WORKFLOW_READ')")
    @Operation(summary = "Get all workflow status transitions with canvas positions")
    public ResponseEntity<ApiResponse<List<StatusTransition>>> getWorkflow() {
        List<StatusTransition> list = statusTransitionRepository.findAll();
        Map<Long, WorkflowPosition> positions = workflowPositionRepository.findAll().stream()
                .filter(p -> p.getProject() == null && p.getStatusType() != null)
                .collect(Collectors.toMap(p -> p.getStatusType().getId(), p -> p, (a, b) -> a));

        for (StatusTransition t : list) {
            if (t.getFromStatus() != null && positions.containsKey(t.getFromStatus().getId())) {
                WorkflowPosition pos = positions.get(t.getFromStatus().getId());
                t.getFromStatus().setPositionX(pos.getPositionX());
                t.getFromStatus().setPositionY(pos.getPositionY());
            }
            if (t.getToStatus() != null && positions.containsKey(t.getToStatus().getId())) {
                WorkflowPosition pos = positions.get(t.getToStatus().getId());
                t.getToStatus().setPositionX(pos.getPositionX());
                t.getToStatus().setPositionY(pos.getPositionY());
            }
        }
        return ResponseEntity.ok(ApiResponse.success(list, "Workflow retrieved"));
    }

    @PostMapping("/status/workflow")
    @PreAuthorize("@access.has('WORKFLOW_CREATE') or @access.has('WORKFLOW_UPDATE')")
    @Operation(summary = "Save workflow status transitions and canvas positions")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<Object>> saveWorkflow(@RequestBody WorkflowSaveRequest req) {
        if (req == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Request body cannot be null"));
        }

        // 1. Save canvas node positions
        if (req.getNodes() != null) {
            for (WorkflowSaveRequest.WorkflowNodeRequest nodeReq : req.getNodes()) {
                Long statusId = nodeReq.getResolvedStatusTypeId();
                if (statusId != null) {
                    StatusType st = statusTypeRepository.findById(statusId).orElse(null);
                    if (st != null) {
                        WorkflowPosition pos = workflowPositionRepository
                                .findByStatusTypeIdAndProjectIsNull(statusId)
                                .orElseGet(() -> WorkflowPosition.builder().statusType(st).build());
                        pos.setPositionX(nodeReq.getPositionX() != null ? nodeReq.getPositionX() : 0.0);
                        pos.setPositionY(nodeReq.getPositionY() != null ? nodeReq.getPositionY() : 0.0);
                        workflowPositionRepository.save(pos);
                    }
                }
            }
        }

        // 2. Save workflow transitions
        statusTransitionRepository.deleteAll();

        List<StatusTransition> savedTransitions = new java.util.ArrayList<>();
        if (req.getConnections() != null) {
            for (WorkflowSaveRequest.WorkflowConnectionRequest conn : req.getConnections()) {
                if (conn.getFromStatusId() != null && conn.getToStatusId() != null) {
                    StatusType fromStatus = statusTypeRepository.findById(conn.getFromStatusId()).orElse(null);
                    StatusType toStatus = statusTypeRepository.findById(conn.getToStatusId()).orElse(null);
                    if (fromStatus != null && toStatus != null) {
                        StatusTransition transition = StatusTransition.builder()
                                .fromStatus(fromStatus)
                                .toStatus(toStatus)
                                .build();
                        savedTransitions.add(statusTransitionRepository.save(transition));
                    }
                }
            }
        }

        return ResponseEntity.ok(ApiResponse.success(savedTransitions, "Workflow saved successfully"));
    }
}

