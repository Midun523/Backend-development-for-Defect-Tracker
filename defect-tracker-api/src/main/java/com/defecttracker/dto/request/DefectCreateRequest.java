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
    private Long projectId;
    private Long severityId;
    private Long priorityId;
    private Long defectStatusId;
    private Long typeId;
    private Integer reOpenCount;
    private String attachment;
    private Long assignbyId;
    private Long assigntoId;
    private Long modulesId;
    private Long moduleId;
    private Long subModuleId;
    private Long releasesId;
    private Long releaseId;
    private Long testCaseId;
    private Boolean testCaseRequired;

    public Long getEffectiveModuleId() {
        return moduleId != null ? moduleId : modulesId;
    }

    public Long getEffectiveReleaseId() {
        return releaseId != null ? releaseId : releasesId;
    }

    public Long getEffectiveAssignedToId() {
        return assigntoId;
    }

    public Long getEffectiveAssignedById() {
        return assignbyId;
    }

    public String getEffectiveTitle() {
        if (title != null && !title.trim().isEmpty()) {
            return title.trim();
        }
        return description != null ? description.trim() : "Defect";
    }
}
