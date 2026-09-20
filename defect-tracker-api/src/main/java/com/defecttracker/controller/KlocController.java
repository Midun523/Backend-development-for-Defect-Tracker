package com.defecttracker.controller;

import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.entity.KlocMetric;
import com.defecttracker.entity.Project;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.repository.KlocMetricRepository;
import com.defecttracker.repository.ProjectRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/kloc")
@RequiredArgsConstructor
@Tag(name = "KLOC & Repository Metrics", description = "Endpoints matching ERD KLOC entity for tracking code metrics and GitHub repository links")
public class KlocController {

    private final KlocMetricRepository klocMetricRepo;
    private final ProjectRepository projectRepo;

    @GetMapping("/project/{projectId}")
    @PreAuthorize("@access.hasProjectAccess('PROJECT_READ', #projectId)")
    @Operation(summary = "Get KLOC metrics for a project")
    public ResponseEntity<ApiResponse<KlocMetric>> getProjectKloc(@PathVariable Long projectId) {
        KlocMetric metric = klocMetricRepo.findFirstByProjectIdOrderByCreatedAtDesc(projectId)
                .orElse(null);
        return ResponseEntity.ok(ApiResponse.success(metric, "KLOC metrics retrieved"));
    }

    @PostMapping("/project/{projectId}")
    @PreAuthorize("@access.hasProjectAccess('PROJECT_UPDATE', #projectId)")
    @Operation(summary = "Save or update KLOC metrics for a project")
    public ResponseEntity<ApiResponse<KlocMetric>> saveProjectKloc(
            @PathVariable Long projectId,
            @RequestBody Map<String, Object> body
    ) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        String backendRepoUrl = (String) body.get("backendRepoUrl");
        String frontendRepoUrl = (String) body.get("frontendRepoUrl");
        String githubToken = (String) body.get("githubToken");
        String githubUsername = (String) body.get("githubUsername");
        Double calculatedKloc = body.get("calculatedKloc") != null
                ? Double.valueOf(body.get("calculatedKloc").toString())
                : 0.0;
        Long totalLoc = body.get("totalLinesOfCode") != null
                ? Long.valueOf(body.get("totalLinesOfCode").toString())
                : (long) (calculatedKloc * 1000);

        KlocMetric metric = klocMetricRepo.findFirstByProjectIdOrderByCreatedAtDesc(projectId)
                .orElse(KlocMetric.builder().project(project).build());

        metric.setBackendRepoUrl(backendRepoUrl);
        metric.setFrontendRepoUrl(frontendRepoUrl);
        metric.setGithubToken(githubToken);
        metric.setGithubUsername(githubUsername);
        metric.setCalculatedKloc(calculatedKloc);
        metric.setTotalLinesOfCode(totalLoc);

        KlocMetric saved = klocMetricRepo.save(metric);

        // Also update KLOC on project
        project.setKloc(calculatedKloc);
        projectRepo.save(project);

        return ResponseEntity.ok(ApiResponse.success(saved, "KLOC metrics updated successfully"));
    }

    @GetMapping
    @PreAuthorize("@access.has('PROJECT_READ')")
    @Operation(summary = "Get all KLOC records")
    public ResponseEntity<ApiResponse<List<KlocMetric>>> getAllKlocRecords() {
        return ResponseEntity.ok(ApiResponse.success(klocMetricRepo.findAll(), "All KLOC records retrieved"));
    }
}
