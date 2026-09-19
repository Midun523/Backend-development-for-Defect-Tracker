package com.defecttracker.modules.testcase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class TestCaseDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestCaseCreateRequest {
        private String testCaseCode; // optional: auto-generated if null

        @NotBlank(message = "Title is required")
        private String title;

        private String description;

        @NotBlank(message = "Execution steps are required")
        private String steps;

        @NotBlank(message = "Expected result is required")
        private String expectedResult;

        @NotNull(message = "SubModule ID is required")
        private Long subModuleId;

        private Long severityId;
        private Long defectTypeId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestCaseUpdateRequest {
        @NotBlank(message = "Title is required")
        private String title;

        private String description;

        @NotBlank(message = "Execution steps are required")
        private String steps;

        @NotBlank(message = "Expected result is required")
        private String expectedResult;

        private Long severityId;
        private Long defectTypeId;
        private Boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestCaseResponse {
        private Long id;
        private String testCaseCode;
        private String title;
        private String description;
        private String steps;
        private String expectedResult;
        private Long subModuleId;
        private String subModuleName;
        private Long moduleId;
        private String moduleName;
        private Long projectId;
        private String projectName;
        private Long severityId;
        private String severityName;
        private Long defectTypeId;
        private String defectTypeName;
        private boolean active;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkImportResult {
        private int totalProcessed;
        private int successCount;
        private int failureCount;
        private List<String> errors;
    }
}
