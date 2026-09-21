package com.defecttracker.controller;

import com.defecttracker.dto.request.RolePermissionAssignRequest;
import com.defecttracker.dto.request.RoleRequest;
import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.dto.response.RolePermissionMatrixResponse;
import com.defecttracker.dto.response.RoleResponse;
import com.defecttracker.entity.Role;
import com.defecttracker.mapper.RoleMapper;
import com.defecttracker.service.RoleService;
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
@Tag(name = "Role & Permission Matrix", description = "Endpoints for roles and role-permission matrix assignments")
public class RoleController {

    private final RoleService roleService;
    private final RoleMapper roleMapper;

    @PostMapping("/role")
    @PreAuthorize("@access.has('ROLE_CREATE')")
    @Operation(summary = "Create role")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest request) {
        Role role = roleMapper.toEntity(request);
        Role created = roleService.createRole(role);
        return ResponseEntity.ok(ApiResponse.created(roleMapper.toResponse(created), "Role created successfully"));
    }

    @GetMapping("/role")
    @PreAuthorize("@access.has('ROLE_READ')")
    @Operation(summary = "Get roles paginated or all")
    public ResponseEntity<ApiResponse<PaginatedResponse<RoleResponse>>> getRoles(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "ASC") String direction
    ) {
        if (page != null && size != null) {
            PaginatedResponse<Role> p = roleService.getRolesPaginated(page, size, sort, direction);
            PaginatedResponse<RoleResponse> dtoPage = PaginatedResponse.<RoleResponse>builder()
                    .content(roleMapper.toResponseList(p.getContent()))
                    .pageNumber(p.getPageNumber())
                    .pageSize(p.getPageSize())
                    .totalElements(p.getTotalElements())
                    .totalPages(p.getTotalPages())
                    .first(p.isFirst())
                    .last(p.isLast())
                    .build();
            return ResponseEntity.ok(ApiResponse.success(dtoPage, "Roles retrieved"));
        }
        List<Role> all = roleService.getAllRoles();
        PaginatedResponse<RoleResponse> p = PaginatedResponse.<RoleResponse>builder()
                .content(roleMapper.toResponseList(all))
                .pageNumber(0)
                .pageSize(all.size())
                .totalElements((long) all.size())
                .totalPages(1)
                .first(true)
                .last(true)
                .build();
        return ResponseEntity.ok(ApiResponse.success(p, "Roles retrieved"));
    }

    @GetMapping("/role/{id}")
    @PreAuthorize("@access.has('ROLE_READ')")
    @Operation(summary = "Get role by ID")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Long id) {
        Role role = roleService.getRoleById(id);
        return ResponseEntity.ok(ApiResponse.success(roleMapper.toResponse(role), "Role found"));
    }

    @PutMapping("/role/{id}")
    @PreAuthorize("@access.has('ROLE_UPDATE')")
    @Operation(summary = "Update role")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        Role role = roleMapper.toEntity(request);
        Role updated = roleService.updateRole(id, role);
        return ResponseEntity.ok(ApiResponse.success(roleMapper.toResponse(updated), "Role updated successfully"));
    }

    @DeleteMapping("/role/{id}")
    @PreAuthorize("@access.has('ROLE_DELETE')")
    @Operation(summary = "Delete role")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Role deleted successfully"));
    }

    @GetMapping("/assign-permission/matrix")
    @PreAuthorize("@access.has('ROLE_PERMISSION_READ') or @access.has('ROLE_READ')")
    @Operation(summary = "Get complete role-permission matrix")
    public ResponseEntity<ApiResponse<List<RolePermissionMatrixResponse>>> getRolePermissionMatrix() {
        List<RolePermissionMatrixResponse> matrix = roleService.getRolePermissionMatrix();
        return ResponseEntity.ok(ApiResponse.success(matrix, "Role permission matrix retrieved"));
    }

    @GetMapping("/assign-permission/matrix/{roleId}")
    @PreAuthorize("@access.has('ROLE_PERMISSION_READ') or @access.has('ROLE_READ')")
    @Operation(summary = "Get role-permission matrix by role ID")
    public ResponseEntity<ApiResponse<RolePermissionMatrixResponse>> getRolePermissionMatrixByRoleId(@PathVariable Long roleId) {
        RolePermissionMatrixResponse matrix = roleService.getRolePermissionMatrixByRoleId(roleId);
        return ResponseEntity.ok(ApiResponse.success(matrix, "Role permissions retrieved"));
    }

    @PostMapping("/assign-permission/matrix")
    @PreAuthorize("@access.has('ROLE_PERMISSION_ASSIGN')")
    @Operation(summary = "Assign permissions to role")
    public ResponseEntity<ApiResponse<Void>> assignPermissions(@Valid @RequestBody RolePermissionAssignRequest request) {
        roleService.assignPermissionsToRole(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Permissions assigned to role successfully"));
    }

    @GetMapping("/role/{roleId}/assigned-points")
    @PreAuthorize("@access.has('ROLE_PERMISSION_READ') or @access.has('ROLE_READ')")
    @Operation(summary = "Get assigned points for role")
    public ResponseEntity<ApiResponse<RolePermissionMatrixResponse>> getRoleAssignedPoints(@PathVariable Long roleId) {
        RolePermissionMatrixResponse matrix = roleService.getRolePermissionMatrixByRoleId(roleId);
        return ResponseEntity.ok(ApiResponse.success(matrix, "Role points retrieved"));
    }
}
