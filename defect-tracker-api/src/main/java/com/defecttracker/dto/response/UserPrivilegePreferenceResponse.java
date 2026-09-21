package com.defecttracker.dto.response;

import java.time.LocalDateTime;

public record UserPrivilegePreferenceResponse(
    Long id,
    EmployeeResponse employee,
    PrivilegeTemplateResponse privilegeTemplate,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
