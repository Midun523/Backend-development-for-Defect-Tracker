package com.defecttracker.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EmployeeSummary(
    Long id,
    UserSummary user,
    String firstName,
    String lastName,
    String email,
    String phone,
    String gender,
    DesignationSummary designation,
    Double experience,
    LocalDate joinedDate,
    String skills,
    Integer availability,
    String status,
    String department,
    String manager,
    LocalDate startDate,
    LocalDate endDate,
    String whatsappNumber,
    Integer resetCount,
    String address,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
