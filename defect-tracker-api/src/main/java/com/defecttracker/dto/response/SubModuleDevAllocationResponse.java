package com.defecttracker.dto.response;

import java.time.LocalDateTime;

public record SubModuleDevAllocationResponse(
    Long id,
    SubModuleSummary subModule,
    EmployeeResponse employee,
    LocalDateTime assignedDate,
    Long employeeId,
    String employeeName,
    Long submoduleId
) {
    public SubModuleDevAllocationResponse(
        Long id,
        SubModuleSummary subModule,
        EmployeeResponse employee,
        LocalDateTime assignedDate
    ) {
        this(
            id,
            subModule,
            employee,
            assignedDate,
            employee != null ? employee.id() : null,
            employee != null ? ((employee.firstName() != null ? employee.firstName() : "") + " " + (employee.lastName() != null ? employee.lastName() : "")).trim() : "Developer",
            subModule != null ? subModule.id() : null
        );
    }
}
