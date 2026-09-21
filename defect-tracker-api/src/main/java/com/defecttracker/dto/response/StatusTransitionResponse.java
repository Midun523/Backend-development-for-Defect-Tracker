package com.defecttracker.dto.response;

public record StatusTransitionResponse(
    Long id,
    DefectStatusSummary fromStatus,
    DefectStatusSummary toStatus
) {}
