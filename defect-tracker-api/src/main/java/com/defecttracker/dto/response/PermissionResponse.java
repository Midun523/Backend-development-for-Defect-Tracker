package com.defecttracker.dto.response;

public record PermissionResponse(
    Long permissionId,
    String action,
    String description
) {}
