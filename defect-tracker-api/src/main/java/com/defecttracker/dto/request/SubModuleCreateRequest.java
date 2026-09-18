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
public class SubModuleCreateRequest {
    @NotBlank(message = "Submodule name is required")
    @JsonProperty("name")
    @JsonAlias({"subModuleName", "sub_module_name", "submoduleName", "subModule_name", "name"})
    private String name;

    private String description;

    @JsonProperty("moduleId")
    @JsonAlias({"module_id", "moduleId"})
    private Long moduleId;
}
