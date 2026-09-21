package com.defecttracker.controller;

import com.defecttracker.dto.request.PermissionRequest;
import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.dto.response.PermissionResponse;
import com.defecttracker.entity.Permission;
import com.defecttracker.mapper.PermissionMapper;
import com.defecttracker.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Permissions Management", description = "Endpoints for viewing and managing system permissions")
public class PermissionController {

    private final PermissionService permissionService;
    private final PermissionMapper permissionMapper;

    @GetMapping("/permission")
    @PreAuthorize("@access.has('PERMISSION_READ') or @access.has('CONFIG_READ')")
    @Operation(summary = "Get all system permissions")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        List<Permission> list = permissionService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success(permissionMapper.toResponseList(list), "Permissions retrieved"));
    }

    @GetMapping("/permission/{id}")
    @PreAuthorize("@access.has('PERMISSION_READ') or @access.has('CONFIG_READ')")
    @Operation(summary = "Get permission by ID")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionById(@PathVariable Long id) {
        Permission permission = permissionService.getPermissionById(id);
        return ResponseEntity.ok(ApiResponse.success(permissionMapper.toResponse(permission), "Permission found"));
    }

    @PostMapping("/permission")
    @PreAuthorize("@access.has('ROLE_PERMISSION_ASSIGN') or @access.has('CONFIG_UPDATE')")
    @Operation(summary = "Create permission")
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(@Valid @RequestBody PermissionRequest request) {
        Permission permission = permissionMapper.toEntity(request);
        Permission created = permissionService.createPermission(permission);
        return ResponseEntity.ok(ApiResponse.created(permissionMapper.toResponse(created), "Permission created successfully"));
    }

    @PutMapping("/permission/{id}")
    @PreAuthorize("@access.has('ROLE_PERMISSION_ASSIGN') or @access.has('CONFIG_UPDATE')")
    @Operation(summary = "Update permission")
    public ResponseEntity<ApiResponse<PermissionResponse>> updatePermission(@PathVariable Long id, @Valid @RequestBody PermissionRequest request) {
        Permission permission = permissionMapper.toEntity(request);
        Permission updated = permissionService.updatePermission(id, permission);
        return ResponseEntity.ok(ApiResponse.success(permissionMapper.toResponse(updated), "Permission updated successfully"));
    }

    @DeleteMapping("/permission/{id}")
    @PreAuthorize("@access.has('ROLE_PERMISSION_ASSIGN') or @access.has('CONFIG_UPDATE')")
    @Operation(summary = "Delete permission")
    public ResponseEntity<ApiResponse<Void>> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Permission deleted successfully"));
    }

    @GetMapping("/employee/{employeeId}/permission")
    @PreAuthorize("@access.has('EMPLOYEE_READ') or @access.has('ROLE_PERMISSION_READ')")
    @Operation(summary = "Get permissions for an employee")
    public ResponseEntity<ApiResponse<List<String>>> getEmployeePermissions(@PathVariable Long employeeId) {
        List<String> permissions = permissionService.getEmployeePermissions(employeeId);
        return ResponseEntity.ok(ApiResponse.success(permissions, "Employee permissions retrieved"));
    }
}
