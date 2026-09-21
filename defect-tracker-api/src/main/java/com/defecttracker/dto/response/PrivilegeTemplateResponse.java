package com.defecttracker.dto.response;

import java.time.LocalDateTime;

public record PrivilegeTemplateResponse(
    Long id,
    String privilegesType,
    String subject,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
