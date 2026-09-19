package com.defecttracker.modules.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class WorkflowDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusTypeDto {
        private Long id;
        @NotBlank(message = "Status name is required")
        private String name;
        private String category;
        private String displayColor;
        private boolean defaultInitial;
        private boolean openStage;
        private boolean resolvedStage;
        private Double positionX;
        private Double positionY;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowTransitionDto {
        private Long id;
        private Long fromStatusId;
        private String fromStatusName;
        private Long toStatusId;
        private String toStatusName;
        private String name;
        private String description;
        private boolean requiresComment;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowGraphResponse {
        private List<StatusTypeDto> nodes;
        private List<WorkflowTransitionDto> edges;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowTransitionRequest {
        @NotNull(message = "fromStatusId is required")
        private Long fromStatusId;
        @NotNull(message = "toStatusId is required")
        private Long toStatusId;
        private String name;
        private String description;
        private boolean requiresComment;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodePositionItem {
        @NotNull(message = "statusId is required")
        private Long statusId;
        @NotNull(message = "positionX is required")
        private Double positionX;
        @NotNull(message = "positionY is required")
        private Double positionY;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateNodePositionsRequest {
        @NotNull(message = "positions list cannot be null")
        private List<NodePositionItem> positions;
    }
}
