package com.defecttracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseCreateRequest {
    private String testcaseNo;
    @NotBlank(message = "Description is required")
    private String description;
    private String detailsSteps;
    private String steps; // alias
    private String expectedResult;
    private Long subModuleId;
    private Long severityId;
    private Long defectTypeId;
    private Long assignedQaId;
    private String createdBy;

    public String getEffectiveSteps() {
        return (detailsSteps != null && !detailsSteps.trim().isEmpty()) ? detailsSteps : steps;
    }
}
