package com.defecttracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCreateRequest {
    @NotBlank(message = "Project name is required")
    private String name;
    private String projectName; // alias
    private String prefix;
    private String projectType;
    private String status;
    private String projectStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long manager; // manager employee ID
    private Long designationId;
    private Long clientId;
    private String clientName;
    private String clientCountry;
    private String clientState;
    private String clientEmail;
    private String clientPhone;
    private String address;
    private String description;
    private Double kloc;

    public String getEffectiveName() {
        return (name != null && !name.trim().isEmpty()) ? name.trim() : projectName;
    }
}
