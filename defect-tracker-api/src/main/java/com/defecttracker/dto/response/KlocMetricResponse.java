package com.defecttracker.dto.response;

import java.time.LocalDateTime;

public record KlocMetricResponse(
    Long id,
    ProjectSummary project,
    DefectSummary defect,
    String backendRepoUrl,
    String frontendRepoUrl,
    String githubUsername,
    Double calculatedKloc,
    Long totalLinesOfCode,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
