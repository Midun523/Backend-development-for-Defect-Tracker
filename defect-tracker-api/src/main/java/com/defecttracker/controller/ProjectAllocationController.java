package com.defecttracker.controller;

import com.defecttracker.dto.request.ProjectAllocationRequest;
import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.entity.Employee;
import com.defecttracker.entity.ProjectAllocation;
import com.defecttracker.service.ProjectAllocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/project-allocation")
    @Operation(summary = "Allocate employee to project")
    public ResponseEntity<ApiResponse<ProjectAllocation>> allocateEmployee(@RequestBody ProjectAllocationRequest request) {
        ProjectAllocation allocation = projectAllocationService.allocateEmployee(request);
        return ResponseEntity.ok(ApiResponse.created(allocation, "Employee allocated successfully"));
    }

    @GetMapping("/project-allocation")
    @Operation(summary = "Get all project allocations")
    public ResponseEntity<ApiResponse<List<ProjectAllocation>>> getAllAllocations() {
        List<ProjectAllocation> list = projectAllocationService.getAllocations();
        return ResponseEntity.ok(ApiResponse.success(list, "All project allocations retrieved"));
    }

    @GetMapping("/project-allocation/{projectId}")
    @Operation(summary = "Get allocations for a project")
    public ResponseEntity<ApiResponse<List<ProjectAllocation>>> getAllocationsByProject(@PathVariable Long projectId) {
        List<ProjectAllocation> list = projectAllocationService.getAllocationsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(list, "Project allocations retrieved"));
    }

    @PutMapping("/project-allocation/{id}")
    @Operation(summary = "Update project allocation")
    public ResponseEntity<ApiResponse<ProjectAllocation>> updateProjectAllocation(
            @PathVariable Long id,
            @RequestBody ProjectAllocationRequest request
    ) {
        ProjectAllocation updated = projectAllocationService.updateAllocation(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Project allocation updated successfully"));
    }

    @DeleteMapping("/project-allocation/{id}")
    @Operation(summary = "Deallocate employee / delete project allocation by ID")
    public ResponseEntity<ApiResponse<Void>> deleteProjectAllocation(@PathVariable Long id) {
        projectAllocationService.deallocateEmployee(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Project allocation deleted successfully"));
    }

    @GetMapping("/project-allocation/{projectId}/employee")
    @Operation(summary = "Get employees allocated to project")
    public ResponseEntity<ApiResponse<List<Employee>>> getEmployeesByProject(@PathVariable Long projectId) {
        List<Employee> list = projectAllocationService.getEmployeesByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(list, "Allocated employees retrieved"));
    }

    @GetMapping("/project-allocation/{projectId}/employees")
    @Operation(summary = "Get employees allocated to project (alias)")
    public ResponseEntity<ApiResponse<List<Employee>>> getEmployeesByProjectAlias(@PathVariable Long projectId) {
        return getEmployeesByProject(projectId);
    }

    @GetMapping("/project-allocation/{projectId}/employee_history")
    @Operation(summary = "Get project employee allocation history")
    public ResponseEntity<ApiResponse<List<ProjectAllocation>>> getEmployeeAllocationHistory(@PathVariable Long projectId) {
        List<ProjectAllocation> list = projectAllocationService.getEmployeeAllocationHistory(projectId);
        return ResponseEntity.ok(ApiResponse.success(list, "Allocation history retrieved"));
    }

    @GetMapping("/project-allocation/employee/{userId}")
    @Operation(summary = "Get project allocations by employee/user ID")
    public ResponseEntity<ApiResponse<List<ProjectAllocation>>> getAllocationsByEmployee(@PathVariable Long userId) {
        List<ProjectAllocation> list = projectAllocationService.getAllocationsByEmployee(userId);
        return ResponseEntity.ok(ApiResponse.success(list, "Employee allocations retrieved"));
    }

    @DeleteMapping("/project-allocation/employee/{id}")
    @Operation(summary = "Deallocate employee from project")
    public ResponseEntity<ApiResponse<Void>> deallocateEmployee(@PathVariable Long id) {
        projectAllocationService.deallocateEmployee(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Employee deallocated successfully"));
    }

    @PatchMapping("/project-allocation/employee/{id}/extend")
    @Operation(summary = "Extend employee project allocation end date")
    public ResponseEntity<ApiResponse<ProjectAllocation>> extendAllocation(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        LocalDate newEndDate = LocalDate.parse(body.get("endDate"));
        ProjectAllocation allocation = projectAllocationService.extendAllocation(id, newEndDate);
        return ResponseEntity.ok(ApiResponse.success(allocation, "Allocation extended successfully"));
    }
}
