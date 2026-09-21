package com.defecttracker.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProjectAllocationResponse(
    Long id,
    ProjectResponse project,
    EmployeeResponse employee,
    LocalDate startDate,
    LocalDate endDate,
    Integer allocationPercentage,
    String role,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String email,
    Long projectId,
    String lastName,
    String firstName,
    String roleName,
    Long employeeId,
    Long userId,
    Long roleId,
    Object status,
    String projectName,
    String userFullName,
    Integer allocationPercent
) {
    public ProjectAllocationResponse(
        Long id,
        ProjectResponse project,
        EmployeeResponse employee,
        LocalDate startDate,
        LocalDate endDate,
        Integer allocationPercentage,
        String role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String rawStatus
    ) {
        this(
            id,
            project,
            employee,
            startDate,
            endDate,
            allocationPercentage,
            role,
            createdAt,
            updatedAt,
            employee != null ? employee.email() : null,
            project != null ? project.id() : null,
            employee != null ? employee.lastName() : null,
            employee != null ? employee.firstName() : null,
            role != null ? role : "Developer",
            employee != null ? employee.id() : null,
            employee != null ? employee.id() : null,
            (employee != null && employee.user() != null && employee.user().roles() != null && !employee.user().roles().isEmpty())
                ? employee.user().roles().iterator().next().id()
                : ("Project Manager".equalsIgnoreCase(role) ? 2L : 1L),
            "ACTIVE".equalsIgnoreCase(rawStatus) ? Boolean.TRUE : ("DEALLOCATED".equalsIgnoreCase(rawStatus) ? Boolean.FALSE : rawStatus),
            project != null ? project.name() : "",
            employee != null ? ((employee.firstName() != null ? employee.firstName() : "") + " " + (employee.lastName() != null ? employee.lastName() : "")).trim() : "Unknown",
            allocationPercentage
        );
    }
}
