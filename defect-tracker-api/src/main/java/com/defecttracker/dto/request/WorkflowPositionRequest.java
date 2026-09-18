package com.defecttracker.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowPositionRequest {
    @NotNull(message = "StatusType ID is required")
    private Long statusTypeId;
    private Long projectId;
    @NotNull(message = "Position X is required")
    private Double positionX;
    @NotNull(message = "Position Y is required")
    private Double positionY;
}
