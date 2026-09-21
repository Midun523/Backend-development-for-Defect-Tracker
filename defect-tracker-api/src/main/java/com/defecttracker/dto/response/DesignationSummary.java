package com.defecttracker.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DesignationSummary(
    Long id,
    String designationName,
    String description,
    int totalEmployees,
    @JsonProperty("name") String name
) {
    public DesignationSummary(Long id, String designationName, String description, int totalEmployees) {
        this(id, designationName, description, totalEmployees, designationName);
    }
}
