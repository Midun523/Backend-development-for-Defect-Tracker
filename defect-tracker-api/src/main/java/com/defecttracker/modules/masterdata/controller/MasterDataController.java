package com.defecttracker.modules.masterdata.controller;

import com.defecttracker.common.dto.ApiResponse;
import com.defecttracker.modules.masterdata.dto.MasterDataDto;
import com.defecttracker.modules.masterdata.service.MasterDataService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/masterdata")
@RequiredArgsConstructor
@Tag(name = "Master & Reference Data", description = "Admin-manageable master lists for Designations, Roles, Severities, Priorities, Defect Types, and Release Types")
public class MasterDataController {

    private final MasterDataService masterDataService;

    // --- Severities ---
    @GetMapping("/severities")
    @Operation(summary = "Get all severities sorted by weight")
    public ResponseEntity<ApiResponse<List<MasterDataDto.SeverityDto>>> getSeverities() {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllSeverities()));
    }

    @PostMapping("/severities")
    @PreAuthorize("hasAuthority('CONFIG:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Create a severity")
    public ResponseEntity<ApiResponse<MasterDataDto.SeverityDto>> createSeverity(@Valid @RequestBody MasterDataDto.SeverityDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(masterDataService.createSeverity(dto)));
    }

    @PutMapping("/severities/{id}")
    @PreAuthorize("hasAuthority('CONFIG:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Update a severity")
    public ResponseEntity<ApiResponse<MasterDataDto.SeverityDto>> updateSeverity(
            @PathVariable Long id, @Valid @RequestBody MasterDataDto.SeverityDto dto) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.updateSeverity(id, dto)));
    }

    @DeleteMapping("/severities/{id}")
    @PreAuthorize("hasAuthority('CONFIG:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Delete a severity")
    public ResponseEntity<ApiResponse<Void>> deleteSeverity(@PathVariable Long id) {
        masterDataService.deleteSeverity(id);
        return ResponseEntity.ok(ApiResponse.success("Severity deleted", null));
    }

    // --- Priorities ---
    @GetMapping("/priorities")
    @Operation(summary = "Get all priorities sorted by weight")
    public ResponseEntity<ApiResponse<List<MasterDataDto.PriorityDto>>> getPriorities() {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllPriorities()));
    }

    @PostMapping("/priorities")
    @PreAuthorize("hasAuthority('CONFIG:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Create a priority")
    public ResponseEntity<ApiResponse<MasterDataDto.PriorityDto>> createPriority(@Valid @RequestBody MasterDataDto.PriorityDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(masterDataService.createPriority(dto)));
    }

    @PutMapping("/priorities/{id}")
    @PreAuthorize("hasAuthority('CONFIG:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Update a priority")
    public ResponseEntity<ApiResponse<MasterDataDto.PriorityDto>> updatePriority(
            @PathVariable Long id, @Valid @RequestBody MasterDataDto.PriorityDto dto) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.updatePriority(id, dto)));
    }

    @DeleteMapping("/priorities/{id}")
    @PreAuthorize("hasAuthority('CONFIG:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Delete a priority")
    public ResponseEntity<ApiResponse<Void>> deletePriority(@PathVariable Long id) {
        masterDataService.deletePriority(id);
        return ResponseEntity.ok(ApiResponse.success("Priority deleted", null));
    }

    // --- Defect Types ---
    @GetMapping("/defect-types")
    @Operation(summary = "Get all defect types")
    public ResponseEntity<ApiResponse<List<MasterDataDto.DefectTypeDto>>> getDefectTypes() {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllDefectTypes()));
    }

    @PostMapping("/defect-types")
    @PreAuthorize("hasAuthority('CONFIG:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Create a defect type")
    public ResponseEntity<ApiResponse<MasterDataDto.DefectTypeDto>> createDefectType(@Valid @RequestBody MasterDataDto.DefectTypeDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(masterDataService.createDefectType(dto)));
    }

    @PutMapping("/defect-types/{id}")
    @PreAuthorize("hasAuthority('CONFIG:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Update a defect type")
    public ResponseEntity<ApiResponse<MasterDataDto.DefectTypeDto>> updateDefectType(
            @PathVariable Long id, @Valid @RequestBody MasterDataDto.DefectTypeDto dto) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.updateDefectType(id, dto)));
    }

    @DeleteMapping("/defect-types/{id}")
    @PreAuthorize("hasAuthority('CONFIG:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Delete a defect type")
    public ResponseEntity<ApiResponse<Void>> deleteDefectType(@PathVariable Long id) {
        masterDataService.deleteDefectType(id);
        return ResponseEntity.ok(ApiResponse.success("Defect type deleted", null));
    }

    // --- Release Types ---
    @GetMapping("/release-types")
    @Operation(summary = "Get all release types")
    public ResponseEntity<ApiResponse<List<MasterDataDto.ReleaseTypeDto>>> getReleaseTypes() {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllReleaseTypes()));
    }

    @PostMapping("/release-types")
    @PreAuthorize("hasAuthority('CONFIG:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Create a release type")
    public ResponseEntity<ApiResponse<MasterDataDto.ReleaseTypeDto>> createReleaseType(@Valid @RequestBody MasterDataDto.ReleaseTypeDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(masterDataService.createReleaseType(dto)));
    }

    @PutMapping("/release-types/{id}")
    @PreAuthorize("hasAuthority('CONFIG:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Update a release type")
    public ResponseEntity<ApiResponse<MasterDataDto.ReleaseTypeDto>> updateReleaseType(
            @PathVariable Long id, @Valid @RequestBody MasterDataDto.ReleaseTypeDto dto) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.updateReleaseType(id, dto)));
    }

    @DeleteMapping("/release-types/{id}")
    @PreAuthorize("hasAuthority('CONFIG:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Delete a release type")
    public ResponseEntity<ApiResponse<Void>> deleteReleaseType(@PathVariable Long id) {
        masterDataService.deleteReleaseType(id);
        return ResponseEntity.ok(ApiResponse.success("Release type deleted", null));
    }

    // --- Designations ---
    @GetMapping("/designations")
    @Operation(summary = "Get all designations")
    public ResponseEntity<ApiResponse<List<MasterDataDto.DesignationDto>>> getDesignations() {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllDesignations()));
    }

    @PostMapping("/designations")
    @PreAuthorize("hasAuthority('CONFIG:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Create a designation")
    public ResponseEntity<ApiResponse<MasterDataDto.DesignationDto>> createDesignation(@Valid @RequestBody MasterDataDto.DesignationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(masterDataService.createDesignation(dto)));
    }

    @PutMapping("/designations/{id}")
    @PreAuthorize("hasAuthority('CONFIG:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Update a designation")
    public ResponseEntity<ApiResponse<MasterDataDto.DesignationDto>> updateDesignation(
            @PathVariable Long id, @Valid @RequestBody MasterDataDto.DesignationDto dto) {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.updateDesignation(id, dto)));
    }

    @DeleteMapping("/designations/{id}")
    @PreAuthorize("hasAuthority('CONFIG:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Delete a designation")
    public ResponseEntity<ApiResponse<Void>> deleteDesignation(@PathVariable Long id) {
        masterDataService.deleteDesignation(id);
        return ResponseEntity.ok(ApiResponse.success("Designation deleted", null));
    }

    // --- Roles ---
    @GetMapping("/roles")
    @Operation(summary = "Get all system roles")
    public ResponseEntity<ApiResponse<List<MasterDataDto.RoleDto>>> getRoles() {
        return ResponseEntity.ok(ApiResponse.success(masterDataService.getAllRoles()));
    }
}
