package com.defecttracker.modules.project.controller;

import com.defecttracker.common.dto.ApiResponse;
import com.defecttracker.common.dto.PageResponse;
import com.defecttracker.modules.project.dto.ProjectDto;
import com.defecttracker.modules.project.service.ProjectService;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Project & Structure Management", description = "Endpoints for Clients, Projects, Modules, SubModules, and Developer Assignments")
public class ProjectController {

    private final ProjectService projectService;

    // --- Clients ---
    @GetMapping("/clients")
    @Operation(summary = "Get all clients")
    public ResponseEntity<ApiResponse<List<ProjectDto.ClientDto>>> getClients() {
        return ResponseEntity.ok(ApiResponse.success(projectService.getAllClients()));
    }

    @PostMapping("/clients")
    @PreAuthorize("hasAuthority('PROJECT:CREATE') or hasRole('ADMIN')")
    @Operation(summary = "Create client")
    public ResponseEntity<ApiResponse<ProjectDto.ClientDto>> createClient(@Valid @RequestBody ProjectDto.ClientDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(projectService.createClient(dto)));
    }

    @PutMapping("/clients/{id}")
    @PreAuthorize("hasAuthority('PROJECT:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Update client")
    public ResponseEntity<ApiResponse<ProjectDto.ClientDto>> updateClient(@PathVariable Long id, @Valid @RequestBody ProjectDto.ClientDto dto) {
        return ResponseEntity.ok(ApiResponse.success(projectService.updateClient(id, dto)));
    }

    @DeleteMapping("/clients/{id}")
    @PreAuthorize("hasAuthority('PROJECT:DELETE') or hasRole('ADMIN')")
    @Operation(summary = "Delete client")
    public ResponseEntity<ApiResponse<Void>> deleteClient(@PathVariable Long id) {
        projectService.deleteClient(id);
        return ResponseEntity.ok(ApiResponse.success("Client deleted", null));
    }

    // --- Projects ---
    @GetMapping("/projects")
    @Operation(summary = "Get paginated projects with optional search query")
    public ResponseEntity<ApiResponse<PageResponse<ProjectDto.ProjectResponse>>> getProjects(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PageResponse<ProjectDto.ProjectResponse> response = (query != null && !query.trim().isEmpty()) ?
                projectService.searchProjects(query.trim(), pageable) :
                projectService.getAllProjects(pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/projects/{id}")
    @Operation(summary = "Get project by ID")
    public ResponseEntity<ApiResponse<ProjectDto.ProjectResponse>> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getProjectById(id)));
    }

    @PostMapping("/projects")
    @PreAuthorize("hasAuthority('PROJECT:CREATE') or hasRole('ADMIN')")
    @Operation(summary = "Create a project with PM eligibility check and KLOC")
    public ResponseEntity<ApiResponse<ProjectDto.ProjectResponse>> createProject(
            @Valid @RequestBody ProjectDto.ProjectCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(projectService.createProject(request)));
    }

    @PutMapping("/projects/{id}")
    @PreAuthorize("hasAuthority('PROJECT:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Update project details, PM, or KLOC")
    public ResponseEntity<ApiResponse<ProjectDto.ProjectResponse>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectDto.ProjectUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(projectService.updateProject(id, request)));
    }

    @DeleteMapping("/projects/{id}")
    @PreAuthorize("hasAuthority('PROJECT:DELETE') or hasRole('ADMIN')")
    @Operation(summary = "Deactivate project")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success("Project deactivated", null));
    }

    // --- Modules ---
    @GetMapping("/projects/{projectId}/modules")
    @Operation(summary = "Get modules under a project")
    public ResponseEntity<ApiResponse<List<ProjectDto.ModuleResponse>>> getModulesByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getModulesByProject(projectId)));
    }

    @PostMapping("/projects/{projectId}/modules")
    @PreAuthorize("hasAuthority('MODULE:CREATE') or hasRole('ADMIN')")
    @Operation(summary = "Create a module under a project")
    public ResponseEntity<ApiResponse<ProjectDto.ModuleResponse>> createModule(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectDto.ModuleCreateRequest request) {
        request.setProjectId(projectId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(projectService.createModule(request)));
    }

    @PutMapping("/modules/{id}")
    @PreAuthorize("hasAuthority('MODULE:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Update a module or module leader")
    public ResponseEntity<ApiResponse<ProjectDto.ModuleResponse>> updateModule(
            @PathVariable Long id,
            @Valid @RequestBody ProjectDto.ModuleUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(projectService.updateModule(id, request)));
    }

    @DeleteMapping("/modules/{id}")
    @PreAuthorize("hasAuthority('MODULE:DELETE') or hasRole('ADMIN')")
    @Operation(summary = "Delete a module")
    public ResponseEntity<ApiResponse<Void>> deleteModule(@PathVariable Long id) {
        projectService.deleteModule(id);
        return ResponseEntity.ok(ApiResponse.success("Module deleted", null));
    }

    // --- SubModules ---
    @GetMapping("/modules/{moduleId}/submodules")
    @Operation(summary = "Get submodules under a module")
    public ResponseEntity<ApiResponse<List<ProjectDto.SubModuleResponse>>> getSubModulesByModule(@PathVariable Long moduleId) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getSubModulesByModule(moduleId)));
    }

    @PostMapping("/modules/{moduleId}/submodules")
    @PreAuthorize("hasAuthority('MODULE:CREATE') or hasRole('ADMIN')")
    @Operation(summary = "Create a submodule")
    public ResponseEntity<ApiResponse<ProjectDto.SubModuleResponse>> createSubModule(
            @PathVariable Long moduleId,
            @Valid @RequestBody ProjectDto.SubModuleCreateRequest request) {
        request.setModuleId(moduleId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(projectService.createSubModule(request)));
    }

    @PutMapping("/submodules/{id}")
    @PreAuthorize("hasAuthority('MODULE:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Update a submodule")
    public ResponseEntity<ApiResponse<ProjectDto.SubModuleResponse>> updateSubModule(
            @PathVariable Long id,
            @Valid @RequestBody ProjectDto.SubModuleUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(projectService.updateSubModule(id, request)));
    }

    @DeleteMapping("/submodules/{id}")
    @PreAuthorize("hasAuthority('MODULE:DELETE') or hasRole('ADMIN')")
    @Operation(summary = "Delete a submodule")
    public ResponseEntity<ApiResponse<Void>> deleteSubModule(@PathVariable Long id) {
        projectService.deleteSubModule(id);
        return ResponseEntity.ok(ApiResponse.success("SubModule deleted", null));
    }

    // --- Developer Assignments ---
    @PostMapping("/submodules/assignments")
    @PreAuthorize("hasAuthority('MODULE:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Bulk assign or remove developers from a submodule")
    public ResponseEntity<ApiResponse<ProjectDto.SubModuleResponse>> bulkAssignDevelopers(
            @Valid @RequestBody ProjectDto.BulkDeveloperAssignmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Developer assignments updated", projectService.bulkAssignDevelopers(request)));
    }
}
