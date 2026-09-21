package com.defecttracker.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleLeaderAllocationRequest {
    private Long moduleId;
    private Long employeeId;
    private Long userId; // frontend alias for employeeId
}
