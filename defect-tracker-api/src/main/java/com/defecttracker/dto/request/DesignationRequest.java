package com.defecttracker.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DesignationRequest {
    private String designationName;
    private String name;
    private String description;

    /** Resolve the canonical designation name from whichever alias the frontend sent. */
    public String resolvedName() {
        if (designationName != null && !designationName.trim().isEmpty()) return designationName.trim();
        if (name != null && !name.trim().isEmpty()) return name.trim();
        return null;
    }
}
