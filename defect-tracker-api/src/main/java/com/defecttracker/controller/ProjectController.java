package com.defecttracker.controller;

import com.defecttracker.dto.request.ProjectCreateRequest;
import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.entity.Project;
import com.defecttracker.service.ProjectService;
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
@RequestMapping("/api/v1/project")
@RequiredArgsConstructor
@Tag(name = "Project Management", description = "Endpoints for project lifecycle, search, metadata, and KLOC")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("@access.has('PROJECT_CREATE')")
    @Operation(summary = "Create project")
    public ResponseEntity<ApiResponse<Project>> createProject(@Valid @RequestBody ProjectCreateRequest request) {
        Project project = projectService.createProject(request);
        return ResponseEntity.ok(ApiResponse.created(project, "Project created successfully"));
    }

    @GetMapping
    @PreAuthorize("@access.has('PROJECT_READ')")
    @Operation(summary = "Get all projects or search with pagination")
    public ResponseEntity<ApiResponse<Object>> getProjects(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null && size != null) {
            PaginatedResponse<Project> p = projectService.searchProjects(query, page, size);
            return ResponseEntity.ok(ApiResponse.success(p, "Projects retrieved"));
        }
        List<Project> all = projectService.getAllProjects();
        return ResponseEntity.ok(ApiResponse.success(all, "Projects retrieved"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@access.hasProjectAccess('PROJECT_READ', #id)")
    @Operation(summary = "Get project by ID")
    public ResponseEntity<ApiResponse<Project>> getProjectById(@PathVariable Long id) {
        Project project = projectService.getProjectById(id);
        return ResponseEntity.ok(ApiResponse.success(project, "Project found"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@access.hasProjectAccess('PROJECT_UPDATE', #id)")
    @Operation(summary = "Update project")
    public ResponseEntity<ApiResponse<Project>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectCreateRequest request
    ) {
        Project project = projectService.updateProject(id, request);
        return ResponseEntity.ok(ApiResponse.success(project, "Project updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@access.hasProjectAccess('PROJECT_DELETE', #id)")
    @Operation(summary = "Delete project")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Project deleted successfully"));
    }

    @PatchMapping("/{projectId}/project-kilo-of-code")
    @PreAuthorize("@access.hasProjectAccess('PROJECT_UPDATE', #projectId)")
    @Operation(summary = "Update KLOC metric for project")
    public ResponseEntity<ApiResponse<Project>> updateProjectKloc(
            @PathVariable Long projectId,
            @RequestBody Map<String, Double> body
    ) {
        Double kloc = body.getOrDefault("kloc", 0.0);
        Project project = projectService.updateKloc(projectId, kloc);
        return ResponseEntity.ok(ApiResponse.success(project, "Project KLOC updated"));
    }
}
