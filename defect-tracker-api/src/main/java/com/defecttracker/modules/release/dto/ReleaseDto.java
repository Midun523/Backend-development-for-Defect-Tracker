package com.defecttracker.modules.release.dto;

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

public class ReleaseDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReleaseCreateRequest {
        @NotBlank(message = "Release name is required")
        private String name;

        @NotBlank(message = "Version is required")
        private String version;

        private LocalDate releaseDate;
        private Long releaseTypeId;

        @NotNull(message = "Project ID is required")
        private Long projectId;

        private String status; // ACTIVE, CLOSED, PLANNED, CANCELLED
        private BigDecimal kloc;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReleaseUpdateRequest {
        @NotBlank(message = "Release name is required")
        private String name;

        private LocalDate releaseDate;
        private Long releaseTypeId;
        private String status;
        private BigDecimal kloc;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReleaseResponse {
        private Long id;
        private String name;
        private String version;
        private LocalDate releaseDate;
        private Long releaseTypeId;
        private String releaseTypeName;
        private Long projectId;
        private String projectName;
        private String status;
        private BigDecimal kloc;
        private String description;
        private long totalTestCases;
        private long passedTestCases;
        private long failedTestCases;
        private long pendingTestCases;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkTestCasesRequest {
        @NotNull(message = "Test case IDs list cannot be null")
        private List<Long> testCaseIds;
        private Long defaultAssignedToId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecordExecutionRequest {
        @NotBlank(message = "Execution status is required (PASSED, FAILED, BLOCKED, SKIPPED)")
        private String executionStatus;
        private String executionNotes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QAAllocationRequest {
        @NotNull(message = "Assigned to employee ID is required")
        private Long assignedToId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReleaseTestCaseResponse {
        private Long id;
        private Long releaseId;
        private String releaseVersion;
        private Long testCaseId;
        private String testCaseCode;
        private String testCaseTitle;
        private Long assignedToId;
        private String assignedToName;
        private String executionStatus;
        private Long executedById;
        private String executedByName;
        private LocalDateTime executedAt;
        private String executionNotes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReleaseSummaryReport {
        private Long projectId;
        private String projectName;
        private long totalReleases;
        private long activeReleases;
        private long closedReleases;
        private long totalTestCasesExecuted;
        private long totalTestCasesPassed;
        private long totalTestCasesFailed;
        private double passRatePercentage;
    }
}
