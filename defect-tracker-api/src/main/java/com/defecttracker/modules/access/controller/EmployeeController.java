package com.defecttracker.modules.access.controller;

import com.defecttracker.common.dto.ApiResponse;
import com.defecttracker.common.dto.PageResponse;
import com.defecttracker.modules.access.dto.EffectivePermissionResponse;
import com.defecttracker.modules.access.dto.EmployeeCreateRequest;
import com.defecttracker.modules.access.dto.EmployeeResponse;
import com.defecttracker.modules.access.dto.EmployeeUpdateRequest;
import com.defecttracker.modules.access.dto.PermissionOverrideRequest;
import com.defecttracker.modules.access.service.EmployeeService;
import com.defecttracker.modules.access.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "Endpoints for employee CRUD, search, capacity tracking, and permission overrides")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final PermissionService permissionService;

    @GetMapping
    @PreAuthorize("hasAuthority('EMPLOYEE:READ') or hasRole('ADMIN')")
    @Operation(summary = "Get paginated employees with optional search query")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> getEmployees(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PageResponse<EmployeeResponse> response = (query != null && !query.trim().isEmpty()) ?
                employeeService.searchEmployees(query.trim(), pageable) :
                employeeService.getAllEmployees(pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE:READ') or hasRole('ADMIN')")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(@PathVariable Long id) {
        EmployeeResponse response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('EMPLOYEE:CREATE') or hasRole('ADMIN')")
    @Operation(summary = "Register a new employee")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(@Valid @RequestBody EmployeeCreateRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Update employee details")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateRequest request) {
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE:DELETE') or hasRole('ADMIN')")
    @Operation(summary = "Deactivate employee account")
    public ResponseEntity<ApiResponse<Void>> deactivateEmployee(@PathVariable Long id) {
        employeeService.deactivateEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deactivated successfully", null));
    }

    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('EMPLOYEE:READ') or hasRole('ADMIN')")
    @Operation(summary = "Get employee's effective permissions and overrides")
    public ResponseEntity<ApiResponse<EffectivePermissionResponse>> getEmployeePermissions(@PathVariable Long id) {
        EffectivePermissionResponse response = permissionService.getEffectivePermissionResponse(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('EMPLOYEE:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Update permission overrides (GRANT / REVOKE) for an individual employee")
    public ResponseEntity<ApiResponse<EffectivePermissionResponse>> updatePermissionOverrides(
            @PathVariable Long id,
            @Valid @RequestBody List<PermissionOverrideRequest> requests) {
        EffectivePermissionResponse response = employeeService.updatePermissionOverrides(id, requests);
        return ResponseEntity.ok(ApiResponse.success("Permission overrides updated successfully", response));
    }
}
