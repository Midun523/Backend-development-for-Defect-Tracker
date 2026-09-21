package com.defecttracker.dto.response;

import java.time.LocalDateTime;

public record SubModuleSummary(
    Long id,
    String name,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long moduleId,
    String submoduleName,
    String subModuleName
) {
    public SubModuleSummary(Long id, String name, String description, LocalDateTime createdAt, LocalDateTime updatedAt, Long moduleId) {
        this(id, name, description, createdAt, updatedAt, moduleId, name, name);
    }
}
