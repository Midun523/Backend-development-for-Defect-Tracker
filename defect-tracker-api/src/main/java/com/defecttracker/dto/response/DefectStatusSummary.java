package com.defecttracker.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DefectStatusSummary(
    Long id,
    String name,
    String color,
    String type,
    String description,
    int orderIndex,
    Double positionX,
    Double positionY,
    @JsonProperty("default") boolean isDefault,
    String defectStatusName,
    String statusName,
    String statusType,
    String colorCode
) {
    public DefectStatusSummary(
        Long id,
        String name,
        String color,
        String type,
        String description,
        int orderIndex,
        Double positionX,
        Double positionY,
        boolean isDefault
    ) {
        this(id, name, color, type, description, orderIndex, positionX, positionY, isDefault, name, name, type, color);
    }
}
