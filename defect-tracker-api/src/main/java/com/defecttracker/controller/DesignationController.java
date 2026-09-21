package com.defecttracker.controller;

import com.defecttracker.dto.request.DesignationRequest;
import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.dto.response.DesignationSummary;
import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.entity.Designation;
import com.defecttracker.mapper.DesignationMapper;
import com.defecttracker.service.DesignationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/designation")
@RequiredArgsConstructor
@Tag(name = "Designation Management", description = "Endpoints for employee designations")
public class DesignationController {

    private final DesignationService designationService;
    private final DesignationMapper designationMapper;

    @PostMapping
    @PreAuthorize("@access.has('DESIGNATION_CREATE')")
    @Operation(summary = "Create designation")
    public ResponseEntity<ApiResponse<DesignationSummary>> createDesignation(@jakarta.validation.Valid @RequestBody DesignationRequest request) {
        Designation designation = new Designation();
        designation.setDesignationName(request.resolvedName());
        designation.setDescription(request.getDescription());
        Designation created = designationService.createDesignation(designation);
        return ResponseEntity.ok(ApiResponse.created(designationMapper.toSummary(created), "Designation created successfully"));
    }

    @GetMapping
    @PreAuthorize("@access.has('DESIGNATION_READ')")
    @Operation(summary = "Get all designations or paginated list")
    public ResponseEntity<ApiResponse<PaginatedResponse<DesignationSummary>>> getDesignations(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null && size != null) {
            PaginatedResponse<Designation> entityPage = designationService.getDesignationsPaginated(page, size);
            PaginatedResponse<DesignationSummary> p = PaginatedResponse.<DesignationSummary>builder()
                    .content(designationMapper.toSummaryList(entityPage.getContent()))
                    .pageNumber(entityPage.getPageNumber())
                    .pageSize(entityPage.getPageSize())
                    .totalElements(entityPage.getTotalElements())
                    .totalPages(entityPage.getTotalPages())
                    .first(entityPage.isFirst())
                    .last(entityPage.isLast())
                    .build();
            return ResponseEntity.ok(ApiResponse.success(p, "Designations retrieved"));
        }
        List<DesignationSummary> all = designationMapper.toSummaryList(designationService.getAllDesignations());
        PaginatedResponse<DesignationSummary> p = PaginatedResponse.<DesignationSummary>builder()
                .content(all)
                .pageNumber(0)
                .pageSize(all.size())
                .totalElements((long) all.size())
                .totalPages(1)
                .first(true)
                .last(true)
                .build();
        return ResponseEntity.ok(ApiResponse.success(p, "Designations retrieved"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@access.has('DESIGNATION_READ')")
    @Operation(summary = "Get designation by ID")
    public ResponseEntity<ApiResponse<DesignationSummary>> getDesignationById(@PathVariable Long id) {
        Designation designation = designationService.getDesignationById(id);
        return ResponseEntity.ok(ApiResponse.success(designationMapper.toSummary(designation), "Designation found"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@access.has('DESIGNATION_UPDATE')")
    @Operation(summary = "Update designation")
    public ResponseEntity<ApiResponse<DesignationSummary>> updateDesignation(@PathVariable Long id, @jakarta.validation.Valid @RequestBody DesignationRequest request) {
        Designation designation = new Designation();
        designation.setDesignationName(request.resolvedName());
        designation.setDescription(request.getDescription());
        Designation updated = designationService.updateDesignation(id, designation);
        return ResponseEntity.ok(ApiResponse.success(designationMapper.toSummary(updated), "Designation updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@access.has('DESIGNATION_DELETE')")
    @Operation(summary = "Delete designation")
    public ResponseEntity<ApiResponse<Void>> deleteDesignation(@PathVariable Long id) {
        designationService.deleteDesignation(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Designation deleted successfully"));
    }
}
