package com.defecttracker.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectCreateRequest {
    private String title;
    private String description;
    private String steps;
    private String stepsToRecreation; // Frontend alias for steps
    private Long projectId;
    private Long severityId;
    private Long priorityId;
    private Long defectStatusId;
    private Long statusId; // Frontend alias for defectStatusId
    private Long typeId;
    private Long defectTypeId; // Frontend alias for typeId
    private Integer reOpenCount;
    private String attachment;
    private Long assignbyId;
    private Long assigntoId;
    private Long assignedTo; // Frontend alias for assigntoId
    private Long assignedBy; // Frontend alias for assignbyId
    private Long modulesId;
    private Long moduleId;
    private Long subModuleId;
    private Long releasesId;
    private Long releaseId;
    private Long testCaseId;
    private Boolean testCaseRequired;
    private Boolean isAddTestCase; // Frontend alias for testCaseRequired

    public Long getEffectiveModuleId() {
        return moduleId != null ? moduleId : modulesId;
    }

    public Long getEffectiveReleaseId() {
        return releaseId != null ? releaseId : releasesId;
    }

    public Long getEffectiveAssignedToId() {
        if (assigntoId != null) return assigntoId;
        return assignedTo;
    }

    public Long getEffectiveAssignedById() {
        if (assignbyId != null) return assignbyId;
        return assignedBy;
    }

    public Long getEffectiveStatusId() {
        if (defectStatusId != null) return defectStatusId;
        return statusId;
    }

    public Long getEffectiveTypeId() {
        if (typeId != null) return typeId;
        return defectTypeId;
    }

    public String getEffectiveSteps() {
        if (steps != null && !steps.trim().isEmpty()) return steps;
        return stepsToRecreation;
    }

    public Boolean getEffectiveTestCaseRequired() {
        if (testCaseRequired != null) return testCaseRequired;
        return isAddTestCase;
    }

    public String getEffectiveTitle() {
        if (title != null && !title.trim().isEmpty()) {
            return title.trim();
        }
        return description != null ? description.trim() : "Defect";
    }
}
