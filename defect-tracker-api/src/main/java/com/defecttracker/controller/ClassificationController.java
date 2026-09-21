package com.defecttracker.controller;

import com.defecttracker.dto.request.*;
import com.defecttracker.dto.response.*;
import com.defecttracker.entity.*;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.mapper.ClassificationMapper;
import com.defecttracker.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.defecttracker.util.PageableUtils;

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
    private final ClassificationMapper classificationMapper;

    // --- Priority ---
    @GetMapping("/priority")
    @PreAuthorize("@access.has('PRIORITY_READ')")
    public ResponseEntity<ApiResponse<PaginatedResponse<PrioritySummary>>> getPriorities(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null && size != null) {
            Page<Priority> p = priorityRepository.findAll(PageableUtils.of(page, size, Sort.by("id")));
            PaginatedResponse<PrioritySummary> res = PaginatedResponse.<PrioritySummary>builder()
                    .content(classificationMapper.toPrioritySummaryList(p.getContent())).pageNumber(p.getNumber()).pageSize(p.getSize()).totalElements(p.getTotalElements()).totalPages(p.getTotalPages()).build();
            return ResponseEntity.ok(ApiResponse.success(res, "Priorities retrieved"));
        }
        List<PrioritySummary> all = classificationMapper.toPrioritySummaryList(priorityRepository.findAll());
        PaginatedResponse<PrioritySummary> res = PaginatedResponse.<PrioritySummary>builder()
                .content(all).pageNumber(0).pageSize(all.size()).totalElements((long) all.size()).totalPages(1).build();
        return ResponseEntity.ok(ApiResponse.success(res, "Priorities retrieved"));
    }

    @GetMapping("/priority/{id}")
    @PreAuthorize("@access.has('PRIORITY_READ')")
    public ResponseEntity<ApiResponse<PrioritySummary>> getPriorityById(@PathVariable Long id) {
        Priority p = priorityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Priority", "id", id));
        return ResponseEntity.ok(ApiResponse.success(classificationMapper.toPrioritySummary(p)));
    }

    @PostMapping("/priority")
    @PreAuthorize("@access.has('PRIORITY_CREATE')")
    public ResponseEntity<ApiResponse<PrioritySummary>> createPriority(@Valid @RequestBody PriorityRequest request) {
        Priority priority = Priority.builder()
                .name(request.getName())
                .color(request.getColor())
                .description(request.getDescription())
                .build();
        return ResponseEntity.ok(ApiResponse.created(classificationMapper.toPrioritySummary(priorityRepository.save(priority)), "Priority created"));
    }

    @PutMapping("/priority/{id}")
    @PreAuthorize("@access.has('PRIORITY_UPDATE')")
    public ResponseEntity<ApiResponse<PrioritySummary>> updatePriority(@PathVariable Long id, @Valid @RequestBody PriorityRequest req) {
        Priority p = priorityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Priority", "id", id));
        p.setName(req.getName());
        p.setColor(req.getColor());
        p.setDescription(req.getDescription());
        return ResponseEntity.ok(ApiResponse.success(classificationMapper.toPrioritySummary(priorityRepository.save(p)), "Priority updated"));
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
    public ResponseEntity<ApiResponse<PaginatedResponse<SeveritySummary>>> getSeverities(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null && size != null) {
            Page<Severity> p = severityRepository.findAll(PageableUtils.of(page, size, Sort.by("id")));
            PaginatedResponse<SeveritySummary> res = PaginatedResponse.<SeveritySummary>builder()
                    .content(classificationMapper.toSeveritySummaryList(p.getContent())).pageNumber(p.getNumber()).pageSize(p.getSize()).totalElements(p.getTotalElements()).totalPages(p.getTotalPages()).build();
            return ResponseEntity.ok(ApiResponse.success(res, "Severities retrieved"));
        }
        List<SeveritySummary> all = classificationMapper.toSeveritySummaryList(severityRepository.findAll());
        PaginatedResponse<SeveritySummary> res = PaginatedResponse.<SeveritySummary>builder()
                .content(all).pageNumber(0).pageSize(all.size()).totalElements((long) all.size()).totalPages(1).build();
        return ResponseEntity.ok(ApiResponse.success(res, "Severities retrieved"));
    }

    @GetMapping("/severity/{id}")
    @PreAuthorize("@access.has('SEVERITY_READ')")
    public ResponseEntity<ApiResponse<SeveritySummary>> getSeverityById(@PathVariable Long id) {
        Severity s = severityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Severity", "id", id));
        return ResponseEntity.ok(ApiResponse.success(classificationMapper.toSeveritySummary(s)));
    }

    @PostMapping("/severity")
    @PreAuthorize("@access.has('SEVERITY_CREATE')")
    public ResponseEntity<ApiResponse<SeveritySummary>> createSeverity(@Valid @RequestBody SeverityRequest request) {
        Severity severity = Severity.builder()
                .name(request.getName())
                .color(request.getColor())
                .description(request.getDescription())
                .build();
        return ResponseEntity.ok(ApiResponse.created(classificationMapper.toSeveritySummary(severityRepository.save(severity)), "Severity created"));
    }

    @PutMapping("/severity/{id}")
    @PreAuthorize("@access.has('SEVERITY_UPDATE')")
    public ResponseEntity<ApiResponse<SeveritySummary>> updateSeverity(@PathVariable Long id, @Valid @RequestBody SeverityRequest req) {
        Severity s = severityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Severity", "id", id));
        s.setName(req.getName());
        s.setColor(req.getColor());
        s.setDescription(req.getDescription());
        return ResponseEntity.ok(ApiResponse.success(classificationMapper.toSeveritySummary(severityRepository.save(s)), "Severity updated"));
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
    public ResponseEntity<ApiResponse<PaginatedResponse<DefectTypeSummary>>> getDefectTypes(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null && size != null) {
            Page<DefectType> p = defectTypeRepository.findAll(PageableUtils.of(page, size, Sort.by("id")));
            PaginatedResponse<DefectTypeSummary> res = PaginatedResponse.<DefectTypeSummary>builder()
                    .content(classificationMapper.toDefectTypeSummaryList(p.getContent())).pageNumber(p.getNumber()).pageSize(p.getSize()).totalElements(p.getTotalElements()).totalPages(p.getTotalPages()).build();
            return ResponseEntity.ok(ApiResponse.success(res, "Defect types retrieved"));
        }
        List<DefectType> allEntities = defectTypeRepository.findAll(Sort.by("id"));
        List<DefectTypeSummary> all = classificationMapper.toDefectTypeSummaryList(allEntities);
        PaginatedResponse<DefectTypeSummary> res = PaginatedResponse.<DefectTypeSummary>builder()
                .content(all).pageNumber(0).pageSize(all.size()).totalElements((long) all.size()).totalPages(1).build();
        return ResponseEntity.ok(ApiResponse.success(res, "Defect types retrieved"));
    }

    @GetMapping("/defect-type/{id}")
    @PreAuthorize("@access.has('DEFECT_TYPE_READ')")
    public ResponseEntity<ApiResponse<DefectTypeSummary>> getDefectTypeById(@PathVariable Long id) {
        DefectType dt = defectTypeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("DefectType", "id", id));
        return ResponseEntity.ok(ApiResponse.success(classificationMapper.toDefectTypeSummary(dt)));
    }

    @PostMapping("/defect-type")
    @PreAuthorize("@access.has('DEFECT_TYPE_CREATE')")
    public ResponseEntity<ApiResponse<DefectTypeSummary>> createDefectType(@Valid @RequestBody DefectTypeRequest request) {
        String name = request.getName() != null && !request.getName().trim().isEmpty()
                ? request.getName().trim()
                : (request.getDefectTypeName() != null ? request.getDefectTypeName().trim() : null);
        DefectType defectType = DefectType.builder()
                .name(name)
                .description(request.getDescription())
                .build();
        return ResponseEntity.ok(ApiResponse.created(classificationMapper.toDefectTypeSummary(defectTypeRepository.save(defectType)), "Defect type created"));
    }

    @PutMapping("/defect-type/{id}")
    @PreAuthorize("@access.has('DEFECT_TYPE_UPDATE')")
    public ResponseEntity<ApiResponse<DefectTypeSummary>> updateDefectType(@PathVariable Long id, @Valid @RequestBody DefectTypeRequest req) {
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
        return ResponseEntity.ok(ApiResponse.success(classificationMapper.toDefectTypeSummary(defectTypeRepository.save(dt)), "Defect type updated"));
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
    public ResponseEntity<ApiResponse<PaginatedResponse<ReleaseTypeSummary>>> getReleaseTypes(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null && size != null) {
            Page<ReleaseType> p = releaseTypeRepository.findAll(PageableUtils.of(page, size, Sort.by("id")));
            PaginatedResponse<ReleaseTypeSummary> res = PaginatedResponse.<ReleaseTypeSummary>builder()
                    .content(classificationMapper.toReleaseTypeSummaryList(p.getContent())).pageNumber(p.getNumber()).pageSize(p.getSize()).totalElements(p.getTotalElements()).totalPages(p.getTotalPages()).build();
            return ResponseEntity.ok(ApiResponse.success(res, "Release types retrieved"));
        }
        List<ReleaseTypeSummary> all = classificationMapper.toReleaseTypeSummaryList(releaseTypeRepository.findAll());
        PaginatedResponse<ReleaseTypeSummary> res = PaginatedResponse.<ReleaseTypeSummary>builder()
                .content(all).pageNumber(0).pageSize(all.size()).totalElements((long) all.size()).totalPages(1).build();
        return ResponseEntity.ok(ApiResponse.success(res, "Release types retrieved"));
    }

    @GetMapping("/release-type/{id}")
    @PreAuthorize("@access.has('RELEASE_TYPE_READ')")
    public ResponseEntity<ApiResponse<ReleaseTypeSummary>> getReleaseTypeById(@PathVariable Long id) {
        ReleaseType rt = releaseTypeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ReleaseType", "id", id));
        return ResponseEntity.ok(ApiResponse.success(classificationMapper.toReleaseTypeSummary(rt)));
    }

    @PostMapping("/release-type")
    @PreAuthorize("@access.has('RELEASE_TYPE_CREATE')")
    public ResponseEntity<ApiResponse<ReleaseTypeSummary>> createReleaseType(@Valid @RequestBody ReleaseTypeRequest request) {
        ReleaseType releaseType = ReleaseType.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return ResponseEntity.ok(ApiResponse.created(classificationMapper.toReleaseTypeSummary(releaseTypeRepository.save(releaseType)), "Release type created"));
    }

    @PutMapping("/release-type/{id}")
    @PreAuthorize("@access.has('RELEASE_TYPE_UPDATE')")
    public ResponseEntity<ApiResponse<ReleaseTypeSummary>> updateReleaseType(@PathVariable Long id, @Valid @RequestBody ReleaseTypeRequest req) {
        ReleaseType rt = releaseTypeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ReleaseType", "id", id));
        rt.setName(req.getName());
        rt.setDescription(req.getDescription());
        return ResponseEntity.ok(ApiResponse.success(classificationMapper.toReleaseTypeSummary(releaseTypeRepository.save(rt)), "Release type updated"));
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
    public ResponseEntity<ApiResponse<PaginatedResponse<DefectStatusSummary>>> getStatusTypes(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null && size != null) {
            Page<StatusType> p = statusTypeRepository.findAll(PageableUtils.of(page, size, Sort.by("orderIndex").and(Sort.by("id"))));
            PaginatedResponse<DefectStatusSummary> res = PaginatedResponse.<DefectStatusSummary>builder()
                    .content(classificationMapper.toDefectStatusSummaryList(p.getContent())).pageNumber(p.getNumber()).pageSize(p.getSize()).totalElements(p.getTotalElements()).totalPages(p.getTotalPages()).build();
            return ResponseEntity.ok(ApiResponse.success(res, "Status types retrieved"));
        }
        List<DefectStatusSummary> all = classificationMapper.toDefectStatusSummaryList(
                statusTypeRepository.findAll(Sort.by("orderIndex").and(Sort.by("id"))));
        PaginatedResponse<DefectStatusSummary> res = PaginatedResponse.<DefectStatusSummary>builder()
                .content(all).pageNumber(0).pageSize(all.size()).totalElements((long) all.size()).totalPages(1).build();
        return ResponseEntity.ok(ApiResponse.success(res, "Status types retrieved"));
    }

    @GetMapping("/status-type/{id}")
    @PreAuthorize("@access.has('STATUS_TYPE_READ')")
    public ResponseEntity<ApiResponse<DefectStatusSummary>> getStatusTypeById(@PathVariable Long id) {
        StatusType st = statusTypeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("StatusType", "id", id));
        return ResponseEntity.ok(ApiResponse.success(classificationMapper.toDefectStatusSummary(st)));
    }

    @PostMapping("/status-type")
    @PreAuthorize("@access.has('STATUS_TYPE_CREATE')")
    public ResponseEntity<ApiResponse<DefectStatusSummary>> createStatusType(@Valid @RequestBody StatusTypeRequest request) {
        StatusType statusType = StatusType.builder()
                .name(request.resolvedName())
                .color(request.resolvedColor())
                .type(request.resolvedType())
                .description(request.getDescription())
                .isDefault(request.isDefault())
                .orderIndex(request.getOrderIndex())
                .build();
        return ResponseEntity.ok(ApiResponse.created(classificationMapper.toDefectStatusSummary(statusTypeRepository.save(statusType)), "Status type created successfully"));
    }

    @PutMapping("/status-type/{id}")
    @PreAuthorize("@access.has('STATUS_TYPE_UPDATE')")
    public ResponseEntity<ApiResponse<DefectStatusSummary>> updateStatusType(@PathVariable Long id, @Valid @RequestBody StatusTypeRequest req) {
        StatusType st = statusTypeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("StatusType", "id", id));
        String resolvedName = req.resolvedName();
        if (resolvedName != null) {
            st.setName(resolvedName);
        }
        String resolvedColor = req.resolvedColor();
        if (resolvedColor != null) {
            st.setColor(resolvedColor);
        }
        if (req.getDescription() != null) {
            st.setDescription(req.getDescription());
        }
        st.setDefault(req.isDefault());
        st.setOrderIndex(req.getOrderIndex());
        String resolvedType = req.resolvedType();
        if (resolvedType != null) {
            st.setType(resolvedType);
        }
        return ResponseEntity.ok(ApiResponse.success(classificationMapper.toDefectStatusSummary(statusTypeRepository.save(st)), "Status type updated successfully"));
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
    public ResponseEntity<ApiResponse<List<DefectStatusSummary>>> getNextStatuses(@PathVariable Long id) {
        List<StatusTransition> transitions = statusTransitionRepository.findByFromStatusId(id);
        List<StatusType> nextStatuses = transitions.stream().map(StatusTransition::getToStatus).collect(Collectors.toList());
        if (nextStatuses.isEmpty()) {
            nextStatuses = statusTypeRepository.findAll();
        }
        return ResponseEntity.ok(ApiResponse.success(classificationMapper.toDefectStatusSummaryList(nextStatuses), "Next statuses retrieved"));
    }

    @GetMapping("/status/workflow")
    @PreAuthorize("@access.has('WORKFLOW_READ')")
    @Operation(summary = "Get all workflow status transitions with canvas positions")
    public ResponseEntity<ApiResponse<List<StatusTransitionResponse>>> getWorkflow() {
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
        return ResponseEntity.ok(ApiResponse.success(classificationMapper.toStatusTransitionResponseList(list), "Workflow retrieved"));
    }

    @PostMapping("/status/workflow")
    @PreAuthorize("@access.has('WORKFLOW_CREATE') or @access.has('WORKFLOW_UPDATE')")
    @Operation(summary = "Save workflow status transitions and canvas positions")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<List<StatusTransitionResponse>>> saveWorkflow(@Valid @RequestBody WorkflowSaveRequest req) {
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

        return ResponseEntity.ok(ApiResponse.success(classificationMapper.toStatusTransitionResponseList(savedTransitions), "Workflow saved successfully"));
    }
}
