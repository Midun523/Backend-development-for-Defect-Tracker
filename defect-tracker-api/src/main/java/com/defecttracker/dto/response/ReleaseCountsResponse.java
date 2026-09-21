package com.defecttracker.dto.response;

public record ReleaseCountsResponse(
    long total,
    int inProgress,
    int planned,
    int completed
) {}
