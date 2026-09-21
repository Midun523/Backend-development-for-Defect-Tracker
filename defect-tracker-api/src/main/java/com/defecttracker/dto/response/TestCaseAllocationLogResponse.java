package com.defecttracker.dto.response;

import java.time.LocalDateTime;

public record TestCaseAllocationLogResponse(
    Long id,
    ReleaseResponse release,
    TestCaseSummary testCase,
    EmployeeResponse employee,
    String action,
    LocalDateTime timestamp
) {}
