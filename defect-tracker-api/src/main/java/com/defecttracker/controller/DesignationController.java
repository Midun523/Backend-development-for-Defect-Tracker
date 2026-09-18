package com.defecttracker.controller;

import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.entity.Designation;
import com.defecttracker.service.DesignationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/designation")
@RequiredArgsConstructor
@Tag(name = "Designation Management", description = "Endpoints for employee designations")
public class DesignationController {

    private final DesignationService designationService;

    @PostMapping
    @Operation(summary = "Create designation")
    public ResponseEntity<ApiResponse<Designation>> createDesignation(@RequestBody Designation designation) {
        Designation created = designationService.createDesignation(designation);
        return ResponseEntity.ok(ApiResponse.created(created, "Designation created successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all designations or paginated list")
    public ResponseEntity<ApiResponse<Object>> getDesignations(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null && size != null) {
            PaginatedResponse<Designation> p = designationService.getDesignationsPaginated(page, size);
            return ResponseEntity.ok(ApiResponse.success(p, "Designations retrieved"));
        }
        List<Designation> all = designationService.getAllDesignations();
        PaginatedResponse<Designation> p = PaginatedResponse.<Designation>builder()
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
    @Operation(summary = "Get designation by ID")
    public ResponseEntity<ApiResponse<Designation>> getDesignationById(@PathVariable Long id) {
        Designation designation = designationService.getDesignationById(id);
        return ResponseEntity.ok(ApiResponse.success(designation, "Designation found"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update designation")
    public ResponseEntity<ApiResponse<Designation>> updateDesignation(@PathVariable Long id, @RequestBody Designation designation) {
        Designation updated = designationService.updateDesignation(id, designation);
        return ResponseEntity.ok(ApiResponse.success(updated, "Designation updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete designation")
    public ResponseEntity<ApiResponse<Void>> deleteDesignation(@PathVariable Long id) {
        designationService.deleteDesignation(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Designation deleted successfully"));
    }
}
