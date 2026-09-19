package com.defecttracker.modules.defect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefectDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DefectCreateRequest {
        @NotNull(message = "Project ID is required")
        private Long projectId;

        @NotBlank(message = "Title is required")
        private String title;

        @NotBlank(message = "Description is required")
        private String description;

        @NotBlank(message = "Reproduction steps are required")
        private String stepsToReproduce;

        private String attachmentUrl;
        private Long moduleId;
        private Long subModuleId;
        private Long defectTypeId;
        private Long severityId;
        private Long priorityId;
        private Long statusId; // If null, defaults to initial status
        private Long assignedToId;
        private Long releaseId;
        private Long testCaseId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DefectUpdateRequest {
        @NotBlank(message = "Title is required")
        private String title;

        @NotBlank(message = "Description is required")
        private String description;

        @NotBlank(message = "Reproduction steps are required")
        private String stepsToReproduce;

        private String attachmentUrl;
        private Long moduleId;
        private Long subModuleId;
        private Long defectTypeId;
        private Long severityId;
        private Long priorityId;
        private Long assignedToId;
        private Long releaseId;
        private Long testCaseId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusChangeRequest {
        @NotNull(message = "New status ID is required")
        private Long newStatusId;
        private String note;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DefectResponse {
        private Long id;
        private Long projectId;
        private String projectName;
        private Long projectSeqNum;
        private String defectCode;
        private String title;
        private String description;
        private String stepsToReproduce;
        private String attachmentUrl;

        private Long moduleId;
        private String moduleName;
        private Long subModuleId;
        private String subModuleName;

        private Long defectTypeId;
        private String defectTypeName;
        private Long severityId;
        private String severityName;
        private String severityColor;
        private Long priorityId;
        private String priorityName;
        private String priorityColor;

        private Long statusId;
        private String statusName;
        private String statusColor;
        private String statusCategory;

        private Long assignedToId;
        private String assignedToName;
        private Long reporterId;
        private String reporterName;

        private Long releaseId;
        private String releaseVersion;
        private Long testCaseId;
        private String testCaseCode;

        private Integer reopenCounter;
        private LocalDateTime firstAssignedAt;
        private LocalDateTime resolvedAt;
        private boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DefectHistoryDto {
        private Long id;
        private Long fromStatusId;
        private String fromStatusName;
        private Long toStatusId;
        private String toStatusName;
        private Long changedById;
        private String changedByName;
        private String note;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentCreateRequest {
        @NotBlank(message = "Comment text is required")
        private String commentText;
        private Long parentCommentId; // null if top-level
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentResponse {
        private Long id;
        private Long defectId;
        private Long authorId;
        private String authorName;
        private String commentText;
        private Long parentCommentId;
        @Builder.Default
        private List<CommentResponse> replies = new ArrayList<>();
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkReassignRequest {
        @NotNull(message = "Defect IDs list cannot be null")
        private List<Long> defectIds;
        @NotNull(message = "New assigned developer ID is required")
        private Long newAssignedToId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DefectAnalyticsReport {
        private Long projectId;
        private String projectName;
        private long totalDefects;
        private Map<String, Long> severityBreakdown;
        private Map<String, Long> moduleBreakdown;
        private Map<String, Long> defectTypeBreakdown;
        private long createdInWindow;
        private long fixedInWindow;
        private Double avgTimeToFindHours;
        private Double avgTimeToFixHours;
        private Double kloc;
        private Double defectDensity; // totalDefects / kloc
    }
}
