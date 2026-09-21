package com.defecttracker.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProjectResponse(
    Long id,
    String projectId,
    String name,
    String prefix,
    String projectType,
    String status,
    LocalDate startDate,
    LocalDate endDate,
    EmployeeResponse manager,
    ClientSummary client,
    String clientName,
    String clientCountry,
    String clientState,
    String clientEmail,
    String clientPhone,
    String address,
    String description,
    Double progress,
    Double kloc,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
