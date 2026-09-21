package com.defecttracker.dto.response;

import java.time.LocalDateTime;

public record DefectResponse(
    Long id,
    String defectId,
    String title,
    String description,
    String steps,
    PrioritySummary priority,
    SeveritySummary severity,
    DefectStatusSummary defectStatus,
    DefectTypeSummary defectType,
    ProjectResponse project,
    ReleaseResponse release,
    ModuleSummary module,
    SubModuleSummary subModule,
    TestCaseSummary testCase,
    Integer reOpenCount,
    Boolean testCaseRequired,
    String attachment,
    String reportedBy,
    EmployeeResponse assignedTo,
    EmployeeResponse assignedBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
