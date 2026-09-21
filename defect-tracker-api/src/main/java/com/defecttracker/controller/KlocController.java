package com.defecttracker.controller;

import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.dto.response.KlocMetricResponse;
import com.defecttracker.entity.KlocMetric;
import com.defecttracker.entity.Project;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.mapper.KlocMetricMapper;
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
    private final KlocMetricMapper klocMetricMapper;

    @GetMapping("/project/{projectId}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @PreAuthorize("@access.hasProjectAccess('PROJECT_READ', #projectId)")
    @Operation(summary = "Get KLOC metrics for a project")
    public ResponseEntity<ApiResponse<KlocMetricResponse>> getProjectKloc(@PathVariable Long projectId) {
        KlocMetric metric = klocMetricRepo.findFirstByProjectIdOrderByCreatedAtDesc(projectId)
                .orElse(null);
        return ResponseEntity.ok(ApiResponse.success(klocMetricMapper.toResponse(metric), "KLOC metrics retrieved"));
    }

    @PostMapping("/project/{projectId}")
    @org.springframework.transaction.annotation.Transactional
    @PreAuthorize("@access.hasProjectAccess('PROJECT_UPDATE', #projectId)")
    @Operation(summary = "Save or update KLOC metrics for a project")
    public ResponseEntity<ApiResponse<KlocMetricResponse>> saveProjectKloc(
            @PathVariable Long projectId,
            @RequestBody com.defecttracker.dto.request.KlocMetricRequest request
    ) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        String backendRepoUrl = request.getBackendRepoUrl();
        String frontendRepoUrl = request.getFrontendRepoUrl();
        String githubToken = request.getGithubToken();
        String githubUsername = request.getGithubUsername();
        Double calculatedKloc = request.getCalculatedKloc() != null
                ? request.getCalculatedKloc()
                : 0.0;
        Long totalLoc = request.getTotalLinesOfCode() != null
                ? request.getTotalLinesOfCode()
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

        return ResponseEntity.ok(ApiResponse.success(klocMetricMapper.toResponse(saved), "KLOC metrics updated successfully"));
    }

    @GetMapping
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @PreAuthorize("@access.has('PROJECT_READ')")
    @Operation(summary = "Get all KLOC records")
    public ResponseEntity<ApiResponse<List<KlocMetricResponse>>> getAllKlocRecords() {
        return ResponseEntity.ok(ApiResponse.success(klocMetricMapper.toResponseList(klocMetricRepo.findAll()), "All KLOC records retrieved"));
    }
}
