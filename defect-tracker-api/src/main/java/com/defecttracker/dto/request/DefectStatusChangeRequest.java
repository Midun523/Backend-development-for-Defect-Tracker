package com.defecttracker.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectStatusChangeRequest {
    private Long defectStatusId;
    private String statusName;
    private String status;
    private String comment;
    private Long changedByUserId;

    public String getEffectiveStatus() {
        if (statusName != null && !statusName.trim().isEmpty()) {
            return statusName.trim();
        }
        return status != null ? status.trim() : "";
    }
}
