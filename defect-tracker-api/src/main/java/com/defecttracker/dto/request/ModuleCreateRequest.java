package com.defecttracker.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleCreateRequest {
    @NotBlank(message = "Module name is required")
    @JsonProperty("name")
    @JsonAlias({"moduleName", "module_name", "name"})
    private String name;

    private String description;

    @JsonProperty("projectId")
    @JsonAlias({"project_id", "projectId"})
    private Long projectId;

    @JsonProperty("leaderId")
    @JsonAlias({"leader_id", "leaderId"})
    private Long leaderId;
}
