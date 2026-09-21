package com.defecttracker.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

public record UserSummary(
    Long id,
    String userId,
    String firstName,
    String lastName,
    String email,
    String phone,
    String gender,
    String userStatus,
    String userType,
    DesignationSummary designation,
    Set<RoleSummary> roles,
    Integer resetCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
