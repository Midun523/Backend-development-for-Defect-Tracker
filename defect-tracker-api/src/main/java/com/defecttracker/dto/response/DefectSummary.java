package com.defecttracker.dto.response;

import java.time.LocalDateTime;

public record DefectSummary(
    Long id,
    String defectId,
    String title,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
