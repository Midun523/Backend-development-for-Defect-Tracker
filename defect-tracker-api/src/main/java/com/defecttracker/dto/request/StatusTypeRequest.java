package com.defecttracker.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusTypeRequest {
    private String name;
    private String defectStatusName;
    private String statusName;
    private String color;
    private String colorCode;
    private String type;
    private String statusType;
    private String description;
    @JsonProperty("isDefault")
    private boolean isDefault;
    private int orderIndex;

    /** Resolve the canonical name from whichever alias the frontend sent. */
    public String resolvedName() {
        if (name != null && !name.trim().isEmpty()) return name.trim();
        if (defectStatusName != null && !defectStatusName.trim().isEmpty()) return defectStatusName.trim();
        if (statusName != null && !statusName.trim().isEmpty()) return statusName.trim();
        return null;
    }

    /** Resolve the canonical color from whichever alias the frontend sent. */
    public String resolvedColor() {
        if (color != null && !color.trim().isEmpty()) return color.trim();
        if (colorCode != null && !colorCode.trim().isEmpty()) return colorCode.trim();
        return null;
    }

    /** Resolve the canonical type from whichever alias the frontend sent. */
    public String resolvedType() {
        if (type != null && !type.trim().isEmpty()) return type.trim();
        if (statusType != null && !statusType.trim().isEmpty()) return statusType.trim();
        return null;
    }
}
