package com.defecttracker.controller;

import com.defecttracker.dto.request.ModuleCreateRequest;
import com.defecttracker.dto.request.ModuleLeaderAllocationRequest;
import com.defecttracker.dto.request.SubModuleCreateRequest;
import com.defecttracker.dto.request.SubModuleDevAllocationRequest;
import com.defecttracker.dto.response.*;
import com.defecttracker.entity.Module;
import com.defecttracker.entity.ModuleLeaderAllocation;
import com.defecttracker.entity.SubModule;
import com.defecttracker.entity.SubModuleDevAllocation;
import com.defecttracker.exception.BadRequestException;
import com.defecttracker.mapper.ModuleMapper;
import com.defecttracker.service.ModuleService;
import com.defecttracker.service.SubModuleService;
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
@Tag(name = "Modules & Submodules", description = "Endpoints for managing project modules, submodules, leaders, and dev allocations")
public class ModuleController {

    private final ModuleService moduleService;
    private final SubModuleService subModuleService;
    private final ModuleMapper moduleMapper;

    @PostMapping("/project/{projectId}/module")
    @PreAuthorize("@access.hasProjectAccess('MODULE_CREATE', #projectId)")
    @Operation(summary = "Create module for a project")
    public ResponseEntity<ApiResponse<ModuleSummary>> createModule(
            @PathVariable Long projectId,
            @Valid @RequestBody ModuleCreateRequest request
    ) {
        Module module = moduleService.createModule(projectId, request);
        return ResponseEntity.ok(ApiResponse.created(moduleMapper.toModuleSummary(module), "Module created successfully"));
    }

    @PostMapping("/module")
    @PreAuthorize("@access.has('MODULE_CREATE')")
    @Operation(summary = "Create module with body projectId")
    public ResponseEntity<ApiResponse<ModuleSummary>> createModuleWithoutPath(
            @Valid @RequestBody ModuleCreateRequest request
    ) {
        Long projectId = request.getProjectId();
        if (projectId == null) {
            throw new BadRequestException("Project ID is required to create a module");
        }
        Module module = moduleService.createModule(projectId, request);
        return ResponseEntity.ok(ApiResponse.created(moduleMapper.toModuleSummary(module), "Module created successfully"));
    }

    @GetMapping("/project/{projectId}/module")
    @PreAuthorize("@access.hasProjectAccess('MODULE_READ', #projectId)")
    @Operation(summary = "Get all modules for a project")
    public ResponseEntity<ApiResponse<List<ModuleSummary>>> getModulesByProject(@PathVariable Long projectId) {
        List<Module> list = moduleService.getModulesByProjectId(projectId);
        return ResponseEntity.ok(ApiResponse.success(moduleMapper.toModuleSummaryList(list), "Modules retrieved successfully"));
    }

    @GetMapping("/project/{projectId}/module/{id}")
    @PreAuthorize("@access.hasProjectAccess('MODULE_READ', #projectId)")
    @Operation(summary = "Get module by ID")
    public ResponseEntity<ApiResponse<ModuleSummary>> getModuleById(
            @PathVariable Long projectId,
            @PathVariable Long id
    ) {
        Module module = moduleService.getModuleById(id);
        return ResponseEntity.ok(ApiResponse.success(moduleMapper.toModuleSummary(module), "Module found"));
    }

    @PutMapping("/project/{projectId}/module/{id}")
    @PreAuthorize("@access.hasProjectAccess('MODULE_UPDATE', #projectId)")
    @Operation(summary = "Update module")
    public ResponseEntity<ApiResponse<ModuleSummary>> updateModule(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @Valid @RequestBody ModuleCreateRequest request
    ) {
        Module module = moduleService.updateModule(projectId, id, request);
        return ResponseEntity.ok(ApiResponse.success(moduleMapper.toModuleSummary(module), "Module updated successfully"));
    }

    @DeleteMapping("/project/{projectId}/module/{id}")
    @PreAuthorize("@access.hasProjectAccess('MODULE_DELETE', #projectId)")
    @Operation(summary = "Delete module")
    public ResponseEntity<ApiResponse<Void>> deleteModule(
            @PathVariable Long projectId,
            @PathVariable Long id
    ) {
        moduleService.deleteModule(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Module deleted successfully"));
    }

    @PostMapping("/module/{moduleId}/sub-module")
    @PreAuthorize("@access.hasModuleAccess('MODULE_CREATE', #moduleId)")
    @Operation(summary = "Create submodule")
    public ResponseEntity<ApiResponse<SubModuleSummary>> createSubModule(
            @PathVariable Long moduleId,
            @Valid @RequestBody SubModuleCreateRequest request
    ) {
        SubModule subModule = subModuleService.createSubModule(moduleId, request);
        return ResponseEntity.ok(ApiResponse.created(moduleMapper.toSubModuleSummary(subModule), "Submodule created successfully"));
    }

    @PostMapping("/sub-module")
    @PreAuthorize("@access.has('MODULE_CREATE')")
    @Operation(summary = "Create submodule with body moduleId")
    public ResponseEntity<ApiResponse<SubModuleSummary>> createSubModuleWithoutPath(
            @Valid @RequestBody SubModuleCreateRequest request
    ) {
        Long moduleId = request.getModuleId();
        if (moduleId == null) {
            throw new BadRequestException("Module ID is required to create a submodule");
        }
        SubModule subModule = subModuleService.createSubModule(moduleId, request);
        return ResponseEntity.ok(ApiResponse.created(moduleMapper.toSubModuleSummary(subModule), "Submodule created successfully"));
    }

    @GetMapping("/module/{moduleId}/sub-module")
    @PreAuthorize("@access.hasModuleAccess('MODULE_READ', #moduleId)")
    @Operation(summary = "Get submodules for a module")
    public ResponseEntity<ApiResponse<List<SubModuleSummary>>> getSubModulesByModule(@PathVariable Long moduleId) {
        List<SubModule> list = subModuleService.getSubModulesByModuleId(moduleId);
        return ResponseEntity.ok(ApiResponse.success(moduleMapper.toSubModuleSummaryList(list), "Submodules retrieved"));
    }

    @GetMapping("/module/{moduleId}/sub-module/{id}")
    @PreAuthorize("@access.hasModuleAccess('MODULE_READ', #moduleId)")
    @Operation(summary = "Get submodule by ID")
    public ResponseEntity<ApiResponse<SubModuleSummary>> getSubModuleById(
            @PathVariable Long moduleId,
            @PathVariable Long id
    ) {
        SubModule subModule = subModuleService.getSubModuleById(id);
        return ResponseEntity.ok(ApiResponse.success(moduleMapper.toSubModuleSummary(subModule), "Submodule found"));
    }

    @PutMapping("/module/{moduleId}/sub-module/{id}")
    @PreAuthorize("@access.hasModuleAccess('MODULE_UPDATE', #moduleId)")
    @Operation(summary = "Update submodule")
    public ResponseEntity<ApiResponse<SubModuleSummary>> updateSubModule(
            @PathVariable Long moduleId,
            @PathVariable Long id,
            @Valid @RequestBody SubModuleCreateRequest request
    ) {
        SubModule subModule = subModuleService.updateSubModule(moduleId, id, request);
        return ResponseEntity.ok(ApiResponse.success(moduleMapper.toSubModuleSummary(subModule), "Submodule updated"));
    }

    @DeleteMapping("/module/{moduleId}/sub-module/{id}")
    @PreAuthorize("@access.hasModuleAccess('MODULE_DELETE', #moduleId)")
    @Operation(summary = "Delete submodule")
    public ResponseEntity<ApiResponse<Void>> deleteSubModule(
            @PathVariable Long moduleId,
            @PathVariable Long id
    ) {
        subModuleService.deleteSubModule(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Submodule deleted"));
    }

    @DeleteMapping("/sub-module/{id}")
    @PreAuthorize("@access.hasSubModuleAccess('MODULE_DELETE', #id)")
    @Operation(summary = "Delete submodule by ID directly")
    public ResponseEntity<ApiResponse<Void>> deleteSubModuleDirect(@PathVariable Long id) {
        subModuleService.deleteSubModule(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Submodule deleted"));
    }

    @PostMapping("/subModule/bulk-by-modules")
    @PreAuthorize("@access.has('MODULE_READ')")
    @Operation(summary = "Get submodules in bulk by module IDs list")
    public ResponseEntity<ApiResponse<List<SubModuleSummary>>> getSubModulesBulk(@jakarta.validation.Valid @RequestBody(required = false) List<Long> moduleIds) {
        List<SubModule> list = subModuleService.getSubModulesBulk(moduleIds);
        return ResponseEntity.ok(ApiResponse.success(moduleMapper.toSubModuleSummaryList(list), "Bulk submodules retrieved"));
    }

    @GetMapping("/sub-module/{id}/employee")
    @PreAuthorize("@access.hasSubModuleAccess('MODULE_READ', #id)")
    @Operation(summary = "Get developers assigned to submodule")
    public ResponseEntity<ApiResponse<List<SubModuleDevAllocationResponse>>> getSubModuleDevs(@PathVariable Long id) {
        List<SubModuleDevAllocation> list = subModuleService.getSubModuleDevs(id);
        return ResponseEntity.ok(ApiResponse.success(moduleMapper.toSubModuleDevAllocationResponseList(list), "Submodule developers retrieved"));
    }

    @PostMapping("/sub-module/{id}/employee")
    @PreAuthorize("@access.hasSubModuleAccess('MODULE_UPDATE', #id)")
    @Operation(summary = "Assign developer to submodule")
    public ResponseEntity<ApiResponse<SubModuleDevAllocationResponse>> assignDevToSubModule(
            @PathVariable Long id,
            @jakarta.validation.Valid @RequestBody SubModuleDevAllocationRequest request
    ) {
        Long employeeId = request.getEmployeeId();
        if (employeeId == null) {
            employeeId = request.getUserId();
        }
        SubModuleDevAllocation allocation = subModuleService.assignDevToSubModule(id, employeeId);
        return ResponseEntity.ok(ApiResponse.success(moduleMapper.toSubModuleDevAllocationResponse(allocation), "Developer assigned to submodule"));
    }

    @DeleteMapping("/sub-module/{moduleId}/employee/{employeeId}")
    @PreAuthorize("@access.hasModuleAccess('MODULE_UPDATE', #moduleId)")
    @Operation(summary = "Remove developer from submodule")
    public ResponseEntity<ApiResponse<Void>> removeDevFromSubModule(
            @PathVariable Long moduleId,
            @PathVariable Long employeeId
    ) {
        subModuleService.removeDevFromSubModule(moduleId, employeeId);
        return ResponseEntity.ok(ApiResponse.success(null, "Developer removed from submodule"));
    }

    @PostMapping("/allocate-module-leader")
    @PreAuthorize("@access.has('MODULE_UPDATE')")
    @Operation(summary = "Allocate module leader")
    public ResponseEntity<ApiResponse<ModuleLeaderAllocationResponse>> allocateModuleLeader(@jakarta.validation.Valid @RequestBody ModuleLeaderAllocationRequest request) {
        Long employeeId = request.getEmployeeId();
        if (employeeId == null) {
            employeeId = request.getUserId();
        }
        ModuleLeaderAllocation allocation = moduleService.allocateModuleLeader(request.getModuleId(), employeeId);
        return ResponseEntity.ok(ApiResponse.success(moduleMapper.toModuleLeaderAllocationResponse(allocation), "Module leader allocated"));
    }

    @DeleteMapping("/allocate-module-leader/{allocateModuleId}")
    @PreAuthorize("@access.has('MODULE_UPDATE')")
    @Operation(summary = "Deallocate module leader")
    public ResponseEntity<ApiResponse<Void>> deallocateModuleLeader(@PathVariable Long allocateModuleId) {
        moduleService.deallocateModuleLeader(allocateModuleId);
        return ResponseEntity.ok(ApiResponse.success(null, "Module leader deallocated"));
    }

    @GetMapping("/module/{moduleId}/allocated-leader")
    @PreAuthorize("@access.hasModuleAccess('MODULE_READ', #moduleId)")
    @Operation(summary = "Get allocated leader for a module")
    public ResponseEntity<ApiResponse<ModuleLeaderAllocationResponse>> getAllocatedLeader(@PathVariable Long moduleId) {
        ModuleLeaderAllocation alloc = moduleService.getAllocatedLeader(moduleId);
        return ResponseEntity.ok(ApiResponse.success(moduleMapper.toModuleLeaderAllocationResponse(alloc), "Allocated leader retrieved"));
    }
}
