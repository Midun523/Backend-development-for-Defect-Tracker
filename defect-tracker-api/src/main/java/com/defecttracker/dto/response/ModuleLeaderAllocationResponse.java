package com.defecttracker.dto.response;

import java.time.LocalDateTime;

public record ModuleLeaderAllocationResponse(
    Long id,
    ModuleSummary module,
    EmployeeResponse employee,
    LocalDateTime allocatedDate
) {}
