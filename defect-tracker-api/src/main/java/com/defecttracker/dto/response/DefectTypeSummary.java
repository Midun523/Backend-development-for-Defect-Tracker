package com.defecttracker.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DefectTypeSummary(
    Long id,
    String name,
    String description,
    @JsonProperty("defectTypeName") String defectTypeName
) {
    public DefectTypeSummary(Long id, String name, String description) {
        this(id, name, description, name);
    }
}
