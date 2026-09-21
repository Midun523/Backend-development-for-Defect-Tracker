package com.defecttracker.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReleaseResponse(
    Long id,
    String releaseNo,
    String name,
    String version,
    String description,
    ProjectResponse project,
    ReleaseTypeSummary releaseType,
    String status,
    LocalDate startDate,
    LocalDate releaseDate,
    LocalDate endDate,
    Double kloc,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long releaseId,
    String releaseName,
    String projectName,
    Long projectId,
    Long releaseTypeId,
    String releaseTypeName
) {
    public ReleaseResponse(
        Long id,
        String releaseNo,
        String name,
        String version,
        String description,
        ProjectResponse project,
        ReleaseTypeSummary releaseType,
        String status,
        LocalDate startDate,
        LocalDate releaseDate,
        LocalDate endDate,
        Double kloc,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this(
            id,
            releaseNo,
            name,
            version,
            description,
            project,
            releaseType,
            status,
            startDate,
            releaseDate,
            endDate,
            kloc,
            createdAt,
            updatedAt,
            id,
            name,
            project != null ? project.name() : null,
            project != null ? project.id() : null,
            releaseType != null ? releaseType.id() : null,
            releaseType != null ? releaseType.name() : null
        );
    }
}
