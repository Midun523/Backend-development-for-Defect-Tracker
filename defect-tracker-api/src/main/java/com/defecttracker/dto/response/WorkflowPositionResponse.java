package com.defecttracker.dto.response;

import java.time.LocalDateTime;

public record WorkflowPositionResponse(
    Long id,
    DefectStatusSummary statusType,
    ProjectSummary project,
    Double positionX,
    Double positionY,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
