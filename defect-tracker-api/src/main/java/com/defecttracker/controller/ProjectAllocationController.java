package com.defecttracker.controller;

import com.defecttracker.dto.request.ProjectAllocationRequest;
import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.dto.response.EmployeeResponse;
import com.defecttracker.dto.response.ProjectAllocationResponse;
import com.defecttracker.entity.Employee;
import com.defecttracker.entity.ProjectAllocation;
import com.defecttracker.mapper.EmployeeMapper;
import com.defecttracker.mapper.ProjectAllocationMapper;
import com.defecttracker.service.ProjectAllocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Project Resource Allocation", description = "Endpoints for assigning and managing employee allocations to projects")
public class ProjectAllocationController {

    private final ProjectAllocationService projectAllocationService;
    private final ProjectAllocationMapper projectAllocationMapper;
    private final EmployeeMapper employeeMapper;

    @PostMapping("/project-allocation")
    @PreAuthorize("@access.hasProjectAccess('BENCH_ALLOCATE', #request.projectId)")
    @Operation(summary = "Allocate employee to project")
    public ResponseEntity<ApiResponse<ProjectAllocationResponse>> allocateEmployee(@jakarta.validation.Valid @RequestBody ProjectAllocationRequest request) {
        ProjectAllocation allocation = projectAllocationService.allocateEmployee(request);
        return ResponseEntity.ok(ApiResponse.created(projectAllocationMapper.toResponse(allocation), "Employee allocated successfully"));
    }

    @GetMapping("/project-allocation")
    @PreAuthorize("@access.has('BENCH_READ')")
    @Operation(summary = "Get all project allocations")
    public ResponseEntity<ApiResponse<List<ProjectAllocationResponse>>> getAllAllocations() {
        List<ProjectAllocation> list = projectAllocationService.getAllocations();
        return ResponseEntity.ok(ApiResponse.success(projectAllocationMapper.toResponseList(list), "All project allocations retrieved"));
    }

    @GetMapping("/project-allocation/{projectId}")
    @PreAuthorize("@access.hasProjectAccess('BENCH_READ', #projectId)")
    @Operation(summary = "Get allocations for a project")
    public ResponseEntity<ApiResponse<List<ProjectAllocationResponse>>> getAllocationsByProject(@PathVariable Long projectId) {
        List<ProjectAllocation> list = projectAllocationService.getAllocationsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(projectAllocationMapper.toResponseList(list), "Project allocations retrieved"));
    }

    @PutMapping("/project-allocation/{id}")
    @PreAuthorize("@access.hasAllocationAccess('BENCH_ALLOCATE', #id)")
    @Operation(summary = "Update project allocation")
    public ResponseEntity<ApiResponse<ProjectAllocationResponse>> updateProjectAllocation(
            @PathVariable Long id,
            @jakarta.validation.Valid @RequestBody ProjectAllocationRequest request
    ) {
        ProjectAllocation updated = projectAllocationService.updateAllocation(id, request);
        return ResponseEntity.ok(ApiResponse.success(projectAllocationMapper.toResponse(updated), "Project allocation updated successfully"));
    }

    @DeleteMapping("/project-allocation/{id}")
    @PreAuthorize("@access.hasAllocationAccess('BENCH_ALLOCATE', #id)")
    @Operation(summary = "Deallocate employee / delete project allocation by ID")
    public ResponseEntity<ApiResponse<Void>> deleteProjectAllocation(@PathVariable Long id) {
        projectAllocationService.deallocateEmployee(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Project allocation deleted successfully"));
    }

    @GetMapping("/project-allocation/{projectId}/employee")
    @PreAuthorize("@access.hasProjectAccess('PROJECT_READ', #projectId)")
    @Operation(summary = "Get employees allocated to project")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getEmployeesByProject(@PathVariable Long projectId) {
        List<Employee> list = projectAllocationService.getEmployeesByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(employeeMapper.toResponseList(list), "Allocated employees retrieved"));
    }

    @GetMapping("/project-allocation/{projectId}/employee_history")
    @PreAuthorize("@access.hasProjectAccess('PROJECT_READ', #projectId)")
    @Operation(summary = "Get project employee allocation history")
    public ResponseEntity<ApiResponse<List<ProjectAllocationResponse>>> getEmployeeAllocationHistory(@PathVariable Long projectId) {
        List<ProjectAllocation> list = projectAllocationService.getEmployeeAllocationHistory(projectId);
        return ResponseEntity.ok(ApiResponse.success(projectAllocationMapper.toResponseList(list), "Allocation history retrieved"));
    }

    @GetMapping("/project-allocation/employee/{userId}")
    @PreAuthorize("@access.has('BENCH_READ')")
    @Operation(summary = "Get project allocations by employee/user ID")
    public ResponseEntity<ApiResponse<List<ProjectAllocationResponse>>> getAllocationsByEmployee(@PathVariable Long userId) {
        List<ProjectAllocation> list = projectAllocationService.getAllocationsByEmployee(userId);
        return ResponseEntity.ok(ApiResponse.success(projectAllocationMapper.toResponseList(list), "Employee allocations retrieved"));
    }

    @DeleteMapping("/project-allocation/employee/{id}")
    @PreAuthorize("@access.hasAllocationAccess('BENCH_ALLOCATE', #id)")
    @Operation(summary = "Deallocate employee from project")
    public ResponseEntity<ApiResponse<Void>> deallocateEmployee(@PathVariable Long id) {
        projectAllocationService.deallocateEmployee(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Employee deallocated successfully"));
    }

    @PatchMapping("/project-allocation/employee/{id}/extend")
    @PreAuthorize("@access.hasAllocationAccess('BENCH_ALLOCATE', #id)")
    @Operation(summary = "Extend employee project allocation end date")
    public ResponseEntity<ApiResponse<ProjectAllocationResponse>> extendAllocation(
            @PathVariable Long id,
            @jakarta.validation.Valid @RequestBody Map<String, String> body
    ) {
        LocalDate newEndDate = LocalDate.parse(body.get("endDate"));
        ProjectAllocation allocation = projectAllocationService.extendAllocation(id, newEndDate);
        return ResponseEntity.ok(ApiResponse.success(projectAllocationMapper.toResponse(allocation), "Allocation extended successfully"));
    }
}
