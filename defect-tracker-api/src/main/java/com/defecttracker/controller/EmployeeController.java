package com.defecttracker.controller;

import com.defecttracker.dto.request.EmployeeCreateRequest;
import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.dto.response.EmployeeResponse;
import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.entity.Employee;
import com.defecttracker.mapper.EmployeeMapper;
import com.defecttracker.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Employee & Bench Management", description = "Endpoints for employee CRUD, status updates, bench management, and manager allocations")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeMapper employeeMapper;

    @PostMapping("/employee")
    @PreAuthorize("@access.has('EMPLOYEE_CREATE')")
    @Operation(summary = "Create a new employee with user credentials")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(@Valid @RequestBody EmployeeCreateRequest request) {
        Employee employee = employeeService.createEmployee(request);
        return ResponseEntity.ok(ApiResponse.created(employeeMapper.toResponse(employee), "Employee created successfully"));
    }

    @GetMapping("/employee")
    @PreAuthorize("@access.has('EMPLOYEE_READ')")
    @Operation(summary = "Get employees with pagination and optional search query")
    public ResponseEntity<ApiResponse<PaginatedResponse<EmployeeResponse>>> getEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query
    ) {
        PaginatedResponse<Employee> response = employeeService.getAllEmployees(page, size, query);
        PaginatedResponse<EmployeeResponse> dtoResponse = PaginatedResponse.<EmployeeResponse>builder()
                .content(employeeMapper.toResponseList(response.getContent()))
                .pageNumber(response.getPageNumber())
                .pageSize(response.getPageSize())
                .totalElements(response.getTotalElements())
                .totalPages(response.getTotalPages())
                .first(response.isFirst())
                .last(response.isLast())
                .build();
        return ResponseEntity.ok(ApiResponse.success(dtoResponse, "Employees retrieved"));
    }

    @GetMapping("/employee/{id}")
    @PreAuthorize("@access.has('EMPLOYEE_READ')")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(@PathVariable Long id) {
        Employee employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success(employeeMapper.toResponse(employee), "Employee found"));
    }

    @PutMapping("/employee/{id}")
    @PreAuthorize("@access.has('EMPLOYEE_UPDATE')")
    @Operation(summary = "Update employee details")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeCreateRequest request
    ) {
        Employee employee = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success(employeeMapper.toResponse(employee), "Employee updated successfully"));
    }

    @DeleteMapping("/employee/{id}")
    @PreAuthorize("@access.has('EMPLOYEE_DELETE')")
    @Operation(summary = "Delete employee")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Employee deleted successfully"));
    }

    @PatchMapping("/employee/{id}/status")
    @PreAuthorize("@access.has('EMPLOYEE_UPDATE')")
    @Operation(summary = "Update employee status")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployeeStatus(
            @PathVariable Long id,
            @Valid @RequestBody Map<String, String> statusBody
    ) {
        String status = statusBody.getOrDefault("status", "active");
        Employee employee = employeeService.updateEmployeeStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(employeeMapper.toResponse(employee), "Employee status updated"));
    }

    @GetMapping("/bench")
    @PreAuthorize("@access.has('BENCH_READ')")
    @Operation(summary = "Get all available bench employees")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getBenchEmployees() {
        List<Employee> bench = employeeService.getBenchEmployees();
        return ResponseEntity.ok(ApiResponse.success(employeeMapper.toResponseList(bench), "Bench employees retrieved"));
    }

    @GetMapping("/designation/{designationId}/employee")
    @PreAuthorize("@access.has('EMPLOYEE_READ')")
    @Operation(summary = "Get employees by designation")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getEmployeesByDesignation(@PathVariable Long designationId) {
        List<Employee> employees = employeeService.getEmployeesByDesignation(designationId);
        return ResponseEntity.ok(ApiResponse.success(employeeMapper.toResponseList(employees), "Employees retrieved"));
    }

    @GetMapping("/designation/{designationId}/available-managers")
    @PreAuthorize("@access.has('PROJECT_CREATE') or @access.has('EMPLOYEE_READ')")
    @Operation(summary = "Get available managers for a designation")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getAvailableManagers(@PathVariable Long designationId) {
        List<Employee> managers = employeeService.getAvailableManagers(designationId, null);
        return ResponseEntity.ok(ApiResponse.success(employeeMapper.toResponseList(managers), "Available managers retrieved"));
    }

    @GetMapping("/designation/{designationId}/available-managers/project/{projectId}")
    @PreAuthorize("@access.hasProjectAccess('PROJECT_UPDATE', #projectId)")
    @Operation(summary = "Get available managers for update on a project")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getAvailableManagersForUpdate(
            @PathVariable Long designationId,
            @PathVariable Long projectId
    ) {
        List<Employee> managers = employeeService.getAvailableManagers(designationId, projectId);
        return ResponseEntity.ok(ApiResponse.success(employeeMapper.toResponseList(managers), "Available managers retrieved"));
    }
}
