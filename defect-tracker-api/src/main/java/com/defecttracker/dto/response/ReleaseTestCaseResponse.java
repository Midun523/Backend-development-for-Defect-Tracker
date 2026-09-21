package com.defecttracker.dto.response;

import java.time.LocalDateTime;

public record ReleaseTestCaseResponse(
    Long id,
    ReleaseResponse release,
    TestCaseSummary testCase,
    EmployeeResponse assignedQa,
    DefectResponse linkedDefect,
    String executionStatus,
    String executionComment,
    LocalDateTime executedAt,
    LocalDateTime createdAt,
    String moduleName,
    String description,
    String defectTypeName,
    String testcaseNo,
    String expectedResult,
    String steps,
    String defectNo,
    String assignedTo,
    String priorityName,
    String severityName,
    String subModuleName
) {
    public ReleaseTestCaseResponse(
        Long id,
        ReleaseResponse release,
        TestCaseSummary testCase,
        EmployeeResponse assignedQa,
        DefectResponse linkedDefect,
        String executionStatus,
        String executionComment,
        LocalDateTime executedAt,
        LocalDateTime createdAt
    ) {
        this(
            id,
            release,
            testCase,
            assignedQa,
            linkedDefect,
            executionStatus,
            executionComment,
            executedAt,
            createdAt,
            testCase != null ? testCase.moduleName() : null,
            testCase != null ? testCase.description() : null,
            testCase != null ? testCase.defectTypeName() : null,
            testCase != null ? testCase.testcaseNo() : null,
            testCase != null ? testCase.expectedResult() : null,
            testCase != null ? testCase.steps() : null,
            linkedDefect != null ? linkedDefect.defectId() : null,
            (linkedDefect != null && linkedDefect.assignedTo() != null)
                ? (linkedDefect.assignedTo().firstName() + " " + (linkedDefect.assignedTo().lastName() != null ? linkedDefect.assignedTo().lastName() : "")).trim()
                : null,
            (linkedDefect != null && linkedDefect.priority() != null) ? linkedDefect.priority().name() : null,
            testCase != null ? testCase.severityName() : null,
            testCase != null ? testCase.subModuleName() : null
        );
    }
}
