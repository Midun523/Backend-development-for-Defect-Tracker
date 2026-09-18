package com.defecttracker.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowSaveRequest {

    private List<WorkflowNodeRequest> nodes;
    private List<WorkflowConnectionRequest> connections;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowNodeRequest {
        private Long id;
        @JsonProperty("statusTypeId")
        private Long statusTypeId;
        private Double positionX;
        private Double positionY;

        public Long getResolvedStatusTypeId() {
            if (statusTypeId != null) {
                return statusTypeId;
            }
            return id;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowConnectionRequest {
        private Long fromStatusId;
        private Long toStatusId;
    }
}
