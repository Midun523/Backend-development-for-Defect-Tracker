package com.defecttracker.modules.access.dto;

import com.defecttracker.modules.access.entity.OverrideType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionOverrideRequest {

    @NotNull(message = "Permission ID is required")
    private Long permissionId;

    @NotNull(message = "Override type is required (GRANT or REVOKE)")
    private OverrideType overrideType;
}
