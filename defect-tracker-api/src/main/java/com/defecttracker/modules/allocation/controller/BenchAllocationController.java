package com.defecttracker.modules.allocation.controller;

import com.defecttracker.common.dto.ApiResponse;
import com.defecttracker.common.dto.PageResponse;
import com.defecttracker.modules.allocation.dto.AllocationDto;
import com.defecttracker.modules.allocation.service.BenchAllocationService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Resource & Bench Allocation", description = "Endpoints for bench capacity tracking, project allocations, and immutable allocation history")
public class BenchAllocationController {

    private final BenchAllocationService allocationService;

    @GetMapping("/bench")
    @PreAuthorize("hasAuthority('BENCH:READ') or hasRole('ADMIN')")
    @Operation(summary = "Get employees on bench with available capacity")
    public ResponseEntity<ApiResponse<List<AllocationDto.BenchEmployeeResponse>>> getBenchEmployees() {
        return ResponseEntity.ok(ApiResponse.success(allocationService.getBenchEmployees()));
    }

    @PostMapping("/allocations")
    @PreAuthorize("hasAuthority('BENCH:ALLOCATE') or hasRole('ADMIN')")
    @Operation(summary = "Allocate employee capacity percentage to a project")
    public ResponseEntity<ApiResponse<AllocationDto.ProjectAllocationResponse>> allocateEmployee(
            @Valid @RequestBody AllocationDto.AllocationRequest request) {
        AllocationDto.ProjectAllocationResponse response = allocationService.allocateEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee allocated successfully", response));
    }

    @DeleteMapping("/allocations/{id}")
    @PreAuthorize("hasAuthority('BENCH:ALLOCATE') or hasRole('ADMIN')")
    @Operation(summary = "Deallocate employee from project and restore available capacity")
    public ResponseEntity<ApiResponse<Void>> deallocateEmployee(
            @PathVariable Long id,
            @RequestParam(required = false) String note) {
        allocationService.deallocateEmployee(id, note);
        return ResponseEntity.ok(ApiResponse.success("Employee deallocated and capacity restored", null));
    }

    @GetMapping("/allocations/employee/{employeeId}")
    @PreAuthorize("hasAuthority('BENCH:READ') or hasRole('ADMIN')")
    @Operation(summary = "Get current active allocations for an employee")
    public ResponseEntity<ApiResponse<List<AllocationDto.ProjectAllocationResponse>>> getEmployeeActiveAllocations(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.success(allocationService.getEmployeeActiveAllocations(employeeId)));
    }

    @GetMapping("/allocations/project/{projectId}")
    @PreAuthorize("hasAuthority('BENCH:READ') or hasRole('ADMIN')")
    @Operation(summary = "Get current active allocations for a project")
    public ResponseEntity<ApiResponse<List<AllocationDto.ProjectAllocationResponse>>> getProjectActiveAllocations(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.success(allocationService.getProjectActiveAllocations(projectId)));
    }

    @GetMapping("/allocations/history/employee/{employeeId}")
    @PreAuthorize("hasAuthority('BENCH:READ') or hasRole('ADMIN')")
    @Operation(summary = "Get paginated immutable allocation history for an employee")
    public ResponseEntity<ApiResponse<PageResponse<AllocationDto.AllocationHistoryResponse>>> getEmployeeAllocationHistory(
            @PathVariable Long employeeId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(allocationService.getEmployeeAllocationHistory(employeeId, pageable)));
    }

    @GetMapping("/allocations/history/project/{projectId}")
    @PreAuthorize("hasAuthority('BENCH:READ') or hasRole('ADMIN')")
    @Operation(summary = "Get paginated immutable allocation history for a project")
    public ResponseEntity<ApiResponse<PageResponse<AllocationDto.AllocationHistoryResponse>>> getProjectAllocationHistory(
            @PathVariable Long projectId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(allocationService.getProjectAllocationHistory(projectId, pageable)));
    }
}
