package com.defecttracker.dto.response;

import java.time.LocalDateTime;

public record DefectHistoryResponse(
    Long id,
    DefectResponse defect,
    String fromStatus,
    String toStatus,
    String changedBy,
    String comment,
    LocalDateTime changedAt
) {}
