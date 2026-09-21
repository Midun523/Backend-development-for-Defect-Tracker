package com.defecttracker.dto.response;

import java.time.LocalDateTime;

public record RolePrivilegePreferenceResponse(
    Long id,
    RoleSummary role,
    PrivilegeTemplateResponse privilegeTemplate,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
