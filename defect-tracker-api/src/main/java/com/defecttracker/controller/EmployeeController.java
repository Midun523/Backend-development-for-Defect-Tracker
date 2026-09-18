package com.defecttracker.controller;

import com.defecttracker.dto.request.EmployeeCreateRequest;
import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.entity.Employee;
import com.defecttracker.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Employee & Bench Management", description = "Endpoints for employee CRUD, status updates, bench management, and manager allocations")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping("/employee")
    @Operation(summary = "Create a new employee with user credentials")
    public ResponseEntity<ApiResponse<Employee>> createEmployee(@Valid @RequestBody EmployeeCreateRequest request) {
        Employee employee = employeeService.createEmployee(request);
        return ResponseEntity.ok(ApiResponse.created(employee, "Employee created successfully"));
    }

    @GetMapping("/employee")
    @Operation(summary = "Get employees with pagination and optional search query")
    public ResponseEntity<ApiResponse<Object>> getEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query
    ) {
        if (size >= 1000) {
            return ResponseEntity.ok(ApiResponse.success(employeeService.getAllEmployeesList(), "Employees retrieved"));
        }
        PaginatedResponse<Employee> response = employeeService.getAllEmployees(page, size, query);
        return ResponseEntity.ok(ApiResponse.success(response, "Employees retrieved"));
    }

    @GetMapping("/employee/{id}")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<ApiResponse<Employee>> getEmployeeById(@PathVariable Long id) {
        Employee employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success(employee, "Employee found"));
    }

    @PutMapping("/employee/{id}")
    @Operation(summary = "Update employee details")
    public ResponseEntity<ApiResponse<Employee>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeCreateRequest request
    ) {
        Employee employee = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success(employee, "Employee updated successfully"));
    }

    @DeleteMapping("/employee/{id}")
    @Operation(summary = "Delete employee")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Employee deleted successfully"));
    }

    @PatchMapping("/employee/{id}/status")
    @Operation(summary = "Update employee status")
    public ResponseEntity<ApiResponse<Employee>> updateEmployeeStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusBody
    ) {
        String status = statusBody.getOrDefault("status", "active");
        Employee employee = employeeService.updateEmployeeStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(employee, "Employee status updated"));
    }

    @GetMapping("/bench")
    @Operation(summary = "Get all available bench employees")
    public ResponseEntity<ApiResponse<List<Employee>>> getBenchEmployees() {
        List<Employee> bench = employeeService.getBenchEmployees();
        return ResponseEntity.ok(ApiResponse.success(bench, "Bench employees retrieved"));
    }

    @GetMapping("/designation/{designationId}/employee")
    @Operation(summary = "Get employees by designation")
    public ResponseEntity<ApiResponse<List<Employee>>> getEmployeesByDesignation(@PathVariable Long designationId) {
        List<Employee> employees = employeeService.getEmployeesByDesignation(designationId);
        return ResponseEntity.ok(ApiResponse.success(employees, "Employees retrieved"));
    }

    @GetMapping("/designation/{designationId}/available-managers")
    @Operation(summary = "Get available managers for a designation")
    public ResponseEntity<ApiResponse<List<Employee>>> getAvailableManagers(@PathVariable Long designationId) {
        List<Employee> managers = employeeService.getAvailableManagers(designationId, null);
        return ResponseEntity.ok(ApiResponse.success(managers, "Available managers retrieved"));
    }

    @GetMapping("/designation/{designationId}/available-managers/project/{projectId}")
    @Operation(summary = "Get available managers for update on a project")
    public ResponseEntity<ApiResponse<List<Employee>>> getAvailableManagersForUpdate(
            @PathVariable Long designationId,
            @PathVariable Long projectId
    ) {
        List<Employee> managers = employeeService.getAvailableManagers(designationId, projectId);
        return ResponseEntity.ok(ApiResponse.success(managers, "Available managers retrieved"));
    }
}
