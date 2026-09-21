package com.defecttracker.dto.response;

public record DefectImportResponse(
    int imported,
    int success,
    int failed,
    int total,
    String message
) {}
