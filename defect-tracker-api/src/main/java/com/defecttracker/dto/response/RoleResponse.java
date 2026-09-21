package com.defecttracker.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RoleResponse(
    Long id,
    String roleName,
    String type,
    String description,
    @JsonProperty("name") String name
) {
    public RoleResponse(Long id, String roleName, String type, String description) {
        this(id, roleName, type, description, roleName);
    }
}
