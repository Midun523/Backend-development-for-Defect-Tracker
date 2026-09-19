package com.defecttracker.modules.masterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class MasterDataDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeverityDto {
        private Long id;
        @NotBlank(message = "Name is required")
        private String name;
        @NotNull(message = "Weight is required")
        private Integer weight;
        private String description;
        private String colorCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriorityDto {
        private Long id;
        @NotBlank(message = "Name is required")
        private String name;
        @NotNull(message = "Weight is required")
        private Integer weight;
        private String description;
        private String colorCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DefectTypeDto {
        private Long id;
        @NotBlank(message = "Name is required")
        private String name;
        private String description;
        private String colorCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReleaseTypeDto {
        private Long id;
        @NotBlank(message = "Name is required")
        private String name;
        private String description;
        private String colorCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DesignationDto {
        private Long id;
        @NotBlank(message = "Name is required")
        private String name;
        private String description;
        private boolean projectManagerEligible;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleDto {
        private Long id;
        @NotBlank(message = "Name is required")
        private String name;
        private String description;
        private boolean admin;
    }
}
