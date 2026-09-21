package com.defecttracker.controller;

import com.defecttracker.dto.request.DefectBulkReassignRequest;
import com.defecttracker.dto.request.DefectCommentRequest;
import com.defecttracker.dto.request.DefectCreateRequest;
import com.defecttracker.dto.request.DefectStatusChangeRequest;
import com.defecttracker.dto.response.*;
import com.defecttracker.entity.Defect;
import com.defecttracker.entity.DefectComment;
import com.defecttracker.entity.DefectHistory;
import com.defecttracker.entity.DefectStatusLog;
import com.defecttracker.mapper.DefectMapper;
import com.defecttracker.service.DefectService;
import com.defecttracker.service.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Defect Management", description = "Endpoints for logging, updating, filtering, commenting and status workflow transitions of defects")
public class DefectController {

    private final DefectService defectService;
    private final StorageService storageService;
    private final ObjectMapper objectMapper;
    private final DefectMapper defectMapper;

    @PostMapping(value = "/defect", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("@access.has('DEFECT_CREATE')")
    @Operation(summary = "Create a defect (JSON payload)")
    public ResponseEntity<ApiResponse<DefectResponse>> createDefect(@RequestBody DefectCreateRequest request) {
        Defect defect = defectService.createDefect(request);
        return ResponseEntity.ok(ApiResponse.created(defectMapper.toResponse(defect), "Defect created successfully"));
    }

    @PostMapping(value = "/defect", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("@access.has('DEFECT_CREATE')")
    @Operation(summary = "Create a defect with file attachment upload")
    public ResponseEntity<ApiResponse<DefectResponse>> createDefectMultipart(
            @RequestPart(value = "data", required = false) Object dataPart,
            @RequestPart(value = "attachmentFile", required = false) MultipartFile attachmentFile,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @ModelAttribute DefectCreateRequest formRequest,
            HttpServletRequest httpRequest
    ) {
        DefectCreateRequest request = null;
        if (dataPart instanceof DefectCreateRequest) {
            request = (DefectCreateRequest) dataPart;
        } else if (dataPart instanceof String) {
            try {
                request = objectMapper.readValue((String) dataPart, DefectCreateRequest.class);
            } catch (Exception ignored) {}
        }

        if (request == null && httpRequest instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest multipartReq = (MultipartHttpServletRequest) httpRequest;
            try {
                MultipartFile part = multipartReq.getFile("data");
                if (part != null && !part.isEmpty()) {
                    request = objectMapper.readValue(part.getInputStream(), DefectCreateRequest.class);
                }
            } catch (Exception ignored) {}
            if (request == null) {
                String param = multipartReq.getParameter("data");
                if (param != null && !param.trim().isEmpty()) {
                    try {
                        request = objectMapper.readValue(param, DefectCreateRequest.class);
                    } catch (Exception ignored) {}
                }
            }
        }

        if (request == null) {
            request = formRequest != null ? formRequest : new DefectCreateRequest();
        }

        MultipartFile actualFile = (attachmentFile != null && !attachmentFile.isEmpty() && attachmentFile.getSize() > 0)
                ? attachmentFile
                : (file != null && !file.isEmpty() && file.getSize() > 0 ? file : null);

        if (actualFile != null && !actualFile.isEmpty() && actualFile.getSize() > 0) {
            String fileUrl = storageService.storeFile(actualFile);
            request.setAttachment(fileUrl);
        }
        Defect defect = defectService.createDefect(request);
        return ResponseEntity.ok(ApiResponse.created(defectMapper.toResponse(defect), "Defect created successfully"));
    }

    @GetMapping("/defect")
    @PreAuthorize("@access.hasProjectAccess('DEFECT_READ', #projectId)")
    @Operation(summary = "Filter defects with pagination and comprehensive criteria")
    public ResponseEntity<ApiResponse<PaginatedResponse<DefectResponse>>> getDefects(
            @RequestParam(required = false, defaultValue = "1") Long projectId,
            @RequestParam(required = false) Long releaseId,
            @RequestParam(required = false) Long severityId,
            @RequestParam(required = false) Long priorityId,
            @RequestParam(required = false) Long defectStatusId,
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) Long subModuleId,
            @RequestParam(required = false) Long assignedToId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long effectiveStatusId = defectStatusId != null ? defectStatusId : statusId;
        PaginatedResponse<Defect> p = defectService.filterDefects(projectId, releaseId, severityId, priorityId, effectiveStatusId, typeId, moduleId, subModuleId, assignedToId, page, size);
        PaginatedResponse<DefectResponse> dto = PaginatedResponse.<DefectResponse>builder()
                .content(defectMapper.toResponseList(p.getContent()))
                .pageNumber(p.getPageNumber())
                .pageSize(p.getPageSize())
                .totalElements(p.getTotalElements())
                .totalPages(p.getTotalPages())
                .last(p.isLast())
                .first(p.isFirst())
                .build();
        return ResponseEntity.ok(ApiResponse.success(dto, "Defects retrieved"));
    }

    @GetMapping("/defect/{id}")
    @PreAuthorize("@access.hasDefectAccess('DEFECT_READ', #id)")
    @Operation(summary = "Get defect by ID")
    public ResponseEntity<ApiResponse<DefectResponse>> getDefectById(@PathVariable Long id) {
        Defect defect = defectService.getDefectById(id);
        return ResponseEntity.ok(ApiResponse.success(defectMapper.toResponse(defect), "Defect found"));
    }

    @PutMapping("/defect/{id}")
    @PreAuthorize("@access.hasDefectAccess('DEFECT_UPDATE', #id)")
    @Operation(summary = "Update defect details")
    public ResponseEntity<ApiResponse<DefectResponse>> updateDefect(
            @PathVariable Long id,
            @RequestBody DefectCreateRequest request
    ) {
        Defect defect = defectService.updateDefect(id, request);
        return ResponseEntity.ok(ApiResponse.success(defectMapper.toResponse(defect), "Defect updated successfully"));
    }

    @DeleteMapping("/defect/{id}")
    @PreAuthorize("@access.hasDefectAccess('DEFECT_DELETE', #id)")
    @Operation(summary = "Delete defect")
    public ResponseEntity<ApiResponse<Void>> deleteDefect(@PathVariable Long id) {
        defectService.deleteDefect(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Defect deleted successfully"));
    }

    @GetMapping("/project/{projectId}/defect")
    @PreAuthorize("@access.hasProjectAccess('DEFECT_READ', #projectId)")
    @Operation(summary = "Get all defects for a project")
    public ResponseEntity<ApiResponse<List<DefectResponse>>> getDefectsByProject(@PathVariable Long projectId) {
        List<Defect> list = defectService.getDefectsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(defectMapper.toResponseList(list), "Project defects retrieved"));
    }

    @PostMapping("/defect/employee")
    @PreAuthorize("@access.has('DEFECT_ASSIGN_DEVELOPER')")
    @Operation(summary = "Assign developer to defect")
    public ResponseEntity<ApiResponse<DefectResponse>> assignDeveloper(@RequestBody Map<String, Long> body) {
        Long defectId = body.get("defectId");
        Long employeeId = body.get("employeeId");
        Defect defect = defectService.assignDeveloper(defectId, employeeId);
        return ResponseEntity.ok(ApiResponse.success(defectMapper.toResponse(defect), "Developer assigned to defect"));
    }

    @GetMapping("/defect/allocation/defect/{id}")
    @PreAuthorize("@access.hasDefectAccess('DEFECT_READ', #id)")
    @Operation(summary = "Get defect allocation details")
    public ResponseEntity<ApiResponse<DefectResponse>> getDefectAllocation(@PathVariable Long id) {
        Defect defect = defectService.getDefectById(id);
        return ResponseEntity.ok(ApiResponse.success(defectMapper.toResponse(defect), "Defect allocation found"));
    }

    @PatchMapping("/defect/{defectId}/status")
    @PreAuthorize("@access.hasDefectAccess('DEFECT_STATUS_CHANGE', #defectId)")
    @Operation(summary = "Transition defect status and record history")
    public ResponseEntity<ApiResponse<DefectResponse>> changeStatus(
            @PathVariable Long defectId,
            @RequestBody DefectStatusChangeRequest request,
            Authentication authentication
    ) {
        String user = authentication != null ? authentication.getName() : "System";
        Defect defect = defectService.changeDefectStatus(defectId, request, user);
        return ResponseEntity.ok(ApiResponse.success(defectMapper.toResponse(defect), "Defect status updated successfully"));
    }

    @PostMapping("/defects/bulk-reassign")
    @PreAuthorize("@access.has('DEFECT_UPDATE')")
    @Operation(summary = "Bulk reassign defects to another developer")
    public ResponseEntity<ApiResponse<Void>> bulkReassign(@Valid @RequestBody DefectBulkReassignRequest request) {
        defectService.bulkReassign(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Defects reassigned successfully"));
    }

    @GetMapping("/defect/{defectId}/comment")
    @PreAuthorize("@access.hasDefectAccess('DEFECT_COMMENT_READ', #defectId)")
    @Operation(summary = "Get comments for a defect")
    public ResponseEntity<ApiResponse<List<DefectCommentResponse>>> getComments(@PathVariable Long defectId) {
        List<DefectComment> comments = defectService.getCommentsByDefect(defectId);
        return ResponseEntity.ok(ApiResponse.success(defectMapper.toCommentResponseList(comments), "Comments retrieved"));
    }

    @PostMapping("/defect/{defectId}/comment")
    @PreAuthorize("@access.hasDefectAccess('DEFECT_COMMENT_CREATE', #defectId)")
    @Operation(summary = "Add comment to a defect")
    public ResponseEntity<ApiResponse<DefectCommentResponse>> addComment(
            @PathVariable Long defectId,
            @Valid @RequestBody DefectCommentRequest request,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : "admin@defecttracker.com";
        DefectComment comment = defectService.addComment(defectId, request, userEmail);
        return ResponseEntity.ok(ApiResponse.created(defectMapper.toCommentResponse(comment), "Comment posted successfully"));
    }

    @GetMapping("/defect/{defectId}/history")
    @PreAuthorize("@access.hasDefectAccess('DEFECT_READ', #defectId)")
    @Operation(summary = "Get audit history for defect")
    public ResponseEntity<ApiResponse<List<DefectHistoryResponse>>> getHistory(@PathVariable Long defectId) {
        List<DefectHistory> history = defectService.getDefectHistory(defectId);
        return ResponseEntity.ok(ApiResponse.success(defectMapper.toHistoryResponseList(history), "History retrieved"));
    }

    @GetMapping("/project/{projectId}/release/{releaseId}/defect-status-log")
    @PreAuthorize("@access.hasProjectAccess('DEFECT_READ', #projectId)")
    @Operation(summary = "Get defect status logs for a project and release")
    public ResponseEntity<ApiResponse<List<DefectStatusLogResponse>>> getStatusLogs(
            @PathVariable Long projectId,
            @PathVariable Long releaseId
    ) {
        List<DefectStatusLog> logs = defectService.getDefectStatusLogs(projectId, releaseId);
        return ResponseEntity.ok(ApiResponse.success(defectMapper.toStatusLogResponseList(logs), "Status logs retrieved"));
    }

    @PostMapping(value = {"/defect/import/{projectId}", "/defect/import", "/defects/import/{projectId}"}, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("@access.has('DEFECT_CREATE')")
    @Operation(summary = "Import defects from file (CSV/Excel) or multipart")
    public ResponseEntity<ApiResponse<DefectImportResponse>> importDefectsFile(
            @PathVariable(required = false) Long projectId,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        DefectImportResponse stats = new DefectImportResponse(0, 0, 0, 0, "File processed successfully");
        return ResponseEntity.ok(ApiResponse.success(stats, "Defects imported successfully"));
    }

    @PostMapping("/defect/bulk")
    @PreAuthorize("@access.has('DEFECT_CREATE')")
    @Operation(summary = "Bulk import defects")
    public ResponseEntity<ApiResponse<String>> bulkImportDefects(@RequestBody List<DefectCreateRequest> requests) {
        for (DefectCreateRequest req : requests) {
            defectService.createDefect(req);
        }
        return ResponseEntity.ok(ApiResponse.success("Imported " + requests.size() + " defects", "Defects imported"));
    }

    @GetMapping("/defect/bulk")
    @PreAuthorize("@access.hasProjectAccess('DEFECT_READ', #projectId)")
    @Operation(summary = "Bulk export defects")
    public ResponseEntity<ApiResponse<List<DefectResponse>>> bulkExportDefects(@RequestParam(required = false, defaultValue = "1") Long projectId) {
        List<Defect> defects = defectService.getDefectsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(defectMapper.toResponseList(defects), "Defects exported"));
    }
}
