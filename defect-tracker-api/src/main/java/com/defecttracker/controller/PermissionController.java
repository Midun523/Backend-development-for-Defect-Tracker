package com.defecttracker.controller;

import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.entity.Permission;
import com.defecttracker.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Permissions Management", description = "Endpoints for viewing and managing system permissions")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/permission")
    @Operation(summary = "Get all system permissions")
    public ResponseEntity<ApiResponse<List<Permission>>> getAllPermissions() {
        List<Permission> list = permissionService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success(list, "Permissions retrieved"));
    }

    @GetMapping("/permission/{id}")
    @Operation(summary = "Get permission by ID")
    public ResponseEntity<ApiResponse<Permission>> getPermissionById(@PathVariable Long id) {
        Permission permission = permissionService.getPermissionById(id);
        return ResponseEntity.ok(ApiResponse.success(permission, "Permission found"));
    }

    @PostMapping("/permission")
    @Operation(summary = "Create permission")
    public ResponseEntity<ApiResponse<Permission>> createPermission(@RequestBody Permission permission) {
        Permission created = permissionService.createPermission(permission);
        return ResponseEntity.ok(ApiResponse.created(created, "Permission created successfully"));
    }

    @PutMapping("/permission/{id}")
    @Operation(summary = "Update permission")
    public ResponseEntity<ApiResponse<Permission>> updatePermission(@PathVariable Long id, @RequestBody Permission permission) {
        Permission updated = permissionService.updatePermission(id, permission);
        return ResponseEntity.ok(ApiResponse.success(updated, "Permission updated successfully"));
    }

    @DeleteMapping("/permission/{id}")
    @Operation(summary = "Delete permission")
    public ResponseEntity<ApiResponse<Void>> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Permission deleted successfully"));
    }

    @GetMapping("/employee/{employeeId}/permission")
    @Operation(summary = "Get permissions for an employee")
    public ResponseEntity<ApiResponse<List<String>>> getEmployeePermissions(@PathVariable Long employeeId) {
        List<String> permissions = permissionService.getEmployeePermissions(employeeId);
        return ResponseEntity.ok(ApiResponse.success(permissions, "Employee permissions retrieved"));
    }
}
