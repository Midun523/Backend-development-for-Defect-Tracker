package com.defecttracker.modules.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ProjectDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientDto {
        private Long id;
        @NotBlank(message = "Client name is required")
        private String name;
        private String email;
        private String phone;
        private String address;
        private String contactPerson;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectCreateRequest {
        @NotBlank(message = "Project name is required")
        private String name;
        private String description;
        private String status;
        private LocalDate startDate;
        private LocalDate endDate;
        private Long clientId;
        @NotNull(message = "Project manager ID is required")
        private Long projectManagerId;
        private BigDecimal kloc;
        private String sourceControlUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectUpdateRequest {
        @NotBlank(message = "Project name is required")
        private String name;
        private String description;
        private String status;
        private LocalDate startDate;
        private LocalDate endDate;
        private Long clientId;
        @NotNull(message = "Project manager ID is required")
        private Long projectManagerId;
        private BigDecimal kloc;
        private String sourceControlUrl;
        private Boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectResponse {
        private Long id;
        private String name;
        private String description;
        private String status;
        private LocalDate startDate;
        private LocalDate endDate;
        private Long clientId;
        private String clientName;
        private Long projectManagerId;
        private String projectManagerName;
        private BigDecimal kloc;
        private String sourceControlUrl;
        private boolean active;
        private int moduleCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModuleCreateRequest {
        @NotBlank(message = "Module name is required")
        private String name;
        private String description;
        @NotNull(message = "Project ID is required")
        private Long projectId;
        private Long moduleLeaderId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModuleUpdateRequest {
        @NotBlank(message = "Module name is required")
        private String name;
        private String description;
        private Long moduleLeaderId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModuleResponse {
        private Long id;
        private String name;
        private String description;
        private Long projectId;
        private String projectName;
        private Long moduleLeaderId;
        private String moduleLeaderName;
        private int subModuleCount;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubModuleCreateRequest {
        @NotBlank(message = "SubModule name is required")
        private String name;
        private String description;
        @NotNull(message = "Module ID is required")
        private Long moduleId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubModuleUpdateRequest {
        @NotBlank(message = "SubModule name is required")
        private String name;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubModuleResponse {
        private Long id;
        private String name;
        private String description;
        private Long moduleId;
        private String moduleName;
        private Long projectId;
        private String projectName;
        private List<DeveloperSummary> assignedDevelopers;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeveloperSummary {
        private Long employeeId;
        private String employeeCode;
        private String fullName;
        private String email;
        private String designation;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkDeveloperAssignmentRequest {
        @NotNull(message = "Submodule ID is required")
        private Long subModuleId;
        @NotNull(message = "Developer employee IDs list cannot be null")
        private List<Long> employeeIds;
        // "ASSIGN" or "REMOVE"
        private String action;
    }
}
