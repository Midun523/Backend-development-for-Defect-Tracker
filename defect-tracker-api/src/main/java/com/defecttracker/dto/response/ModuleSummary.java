package com.defecttracker.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ModuleSummary(
    Long id,
    String name,
    String description,
    ProjectResponse project,
    EmployeeResponse leader,
    List<SubModuleSummary> submodules,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long leaderId,
    String moduleName,
    Long projectId
) {
    public ModuleSummary(
        Long id,
        String name,
        String description,
        ProjectResponse project,
        EmployeeResponse leader,
        List<SubModuleSummary> submodules,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this(
            id,
            name,
            description,
            project,
            leader,
            submodules,
            createdAt,
            updatedAt,
            leader != null ? leader.id() : null,
            name,
            project != null ? project.id() : null
        );
    }
}
