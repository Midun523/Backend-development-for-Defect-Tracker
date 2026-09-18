package com.defecttracker.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseCreateRequest {
    @NotBlank(message = "Release name is required")
    private String name;

    private String version;

    private String description;

    @JsonProperty("projectId")
    @JsonAlias({"project_id", "projectId"})
    private Long projectId;

    @JsonProperty("releaseTypeId")
    @JsonAlias({"releaseType_id", "releaseTypeId", "release_type_id"})
    private Long releaseTypeId;

    private String status;

    @JsonProperty("startDate")
    @JsonAlias({"start_date", "startDate"})
    private LocalDate startDate;

    @JsonProperty("releaseDate")
    @JsonAlias({"release_date", "releaseDate"})
    private LocalDate releaseDate;

    @JsonProperty("endDate")
    @JsonAlias({"end_date", "endDate"})
    private LocalDate endDate;

    private Double kloc;

    private List<Long> testCases;
}
